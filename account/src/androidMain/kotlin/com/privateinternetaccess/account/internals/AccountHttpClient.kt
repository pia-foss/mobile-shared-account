/*
 *  Copyright (c) 2020 Private Internet Access, Inc.
 *
 *  This file is part of the Private Internet Access Mobile Client.
 *
 *  The Private Internet Access Mobile Client is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  The Private Internet Access Mobile Client is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License along with the Private
 *  Internet Access Mobile Client.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.privateinternetaccess.account.internals

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.*
import okhttp3.OkHttpClient
import org.spongycastle.asn1.x500.X500Name
import org.spongycastle.asn1.x500.style.BCStyle
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.IllegalStateException
import java.security.*
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.*
import javax.security.auth.x500.X500Principal


internal actual object AccountHttpClient {

    actual fun client(
        certificate: String?,
        pinnedEndpoint: Pair<String, String>?
    ) : Pair<HttpClient?, Exception?> {
        var httpClient: HttpClient? = null
        var exception: Exception? = null
        try {
            httpClient = HttpClient(OkHttp) {
                expectSuccess = false
                install(HttpTimeout) {
                    requestTimeoutMillis = Account.REQUEST_TIMEOUT_MS
                }

                if (certificate != null && pinnedEndpoint != null) {
                    engine {
                        preconfigured = AccountCertificatePinner.getOkHttpClient(
                            certificate,
                            pinnedEndpoint.first,
                            pinnedEndpoint.second
                        )
                    }
                }
            }
        } catch (e: KeyStoreException) {
            exception = e
        } catch (e: IOException) {
            exception = e
        } catch (e: CertificateException) {
            exception = e
        } catch (e: NoSuchAlgorithmException) {
            exception = e
        } catch (e: KeyManagementException) {
            exception = e
        } catch (e: IllegalStateException) {
            exception = e
        } catch (e: Throwable) {
            val exceptionName = e::class.simpleName ?: "Unknown Exception Name"
            exception = Exception(exceptionName)
        }
        return Pair(httpClient, exception)
    }
}

private class AccountCertificatePinner {

    companion object {

        @Throws(
            KeyStoreException::class,
            IOException::class,
            CertificateException::class,
            NoSuchAlgorithmException::class,
            KeyManagementException::class,
            IllegalStateException::class
        )
        fun getOkHttpClient(certificate: String, requestHostname: String, commonName: String): OkHttpClient {
            val builder = OkHttpClient.Builder()
            val keyStore = KeyStore.getInstance("BKS")
            keyStore.load(null)
            val inputStream = certificate.toByteArray().inputStream()
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val certificateObject = certificateFactory.generateCertificate(inputStream)
            keyStore.setCertificateEntry("account", certificateObject)
            inputStream.close()
            val trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(keyStore)
            val trustManagers = trustManagerFactory.trustManagers
            check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
                "Unexpected default trust managers:" + Arrays.toString(trustManagers)
            }
            val trustManager = trustManagers[0] as X509TrustManager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustManagers, SecureRandom())
            val sslSocketFactory = sslContext.socketFactory
            builder.connectTimeout(Account.REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            if (sslSocketFactory != null) {
                builder.sslSocketFactory(sslSocketFactory, trustManager)
            }
            builder.hostnameVerifier(AccountHostnameVerifier(trustManager, requestHostname, commonName))
            return builder.build()
        }
    }

    private class AccountHostnameVerifier(
            private val trustManager: X509TrustManager?,
            private val requestHostname: String,
            private val commonName: String
    ) : HostnameVerifier {

        override fun verify(hostname: String?, session: SSLSession?): Boolean {
            var verified = false
            try {
                val x509CertificateChain = session?.peerCertificates as Array<out X509Certificate>
                trustManager?.checkServerTrusted(x509CertificateChain, "RSA")
                val sessionCertificate = session.peerCertificates.first()
                verified = verifyCommonName(hostname, sessionCertificate as X509Certificate)
            } catch (e: SSLPeerUnverifiedException) {
                e.printStackTrace()
            } catch (e: CertificateException) {
                e.printStackTrace()
            } catch (e: InvalidKeyException) {
                e.printStackTrace()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: NoSuchProviderException) {
                e.printStackTrace()
            } catch (e: SignatureException) {
                e.printStackTrace()
            }
            return verified
        }

        private fun verifyCommonName(hostname: String?, certificate: X509Certificate): Boolean {
            var verified = false
            val principal = certificate.subjectDN as X500Principal
            certificateCommonName(X500Name.getInstance(principal.encoded))?.let { certCommonName ->
                verified = hostname?.let {
                    isEqual(it.toByteArray(), requestHostname.toByteArray()) &&
                            isEqual(commonName.toByteArray(), certCommonName.toByteArray())
                } ?: isEqual(commonName.toByteArray(), certCommonName.toByteArray())
            }
            return verified
        }

        private fun certificateCommonName(name: X500Name): String? {
            val rdns = name.getRDNs(BCStyle.CN)
            return if (rdns.isEmpty()) {
                null
            } else rdns.first().first.value.toString()
        }

        private fun isEqual(a: ByteArray, b: ByteArray): Boolean {
            val messageDigest = MessageDigest.getInstance("SHA-256")
            val random = SecureRandom()
            val randomBytes = ByteArray(20)
            random.nextBytes(randomBytes)

            val concatA = ByteArrayOutputStream()
            concatA.write(randomBytes)
            concatA.write(a)
            val digestA = messageDigest.digest(concatA.toByteArray())

            val concatB = ByteArrayOutputStream()
            concatB.write(randomBytes)
            concatB.write(b)
            val digestB = messageDigest.digest(concatB.toByteArray())

            return MessageDigest.isEqual(digestA, digestB)
        }
    }
}