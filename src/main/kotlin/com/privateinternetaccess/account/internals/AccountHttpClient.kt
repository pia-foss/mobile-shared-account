package com.privateinternetaccess.account.internals

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.HttpTimeout
import okhttp3.OkHttpClient
import org.spongycastle.asn1.x500.X500Name
import org.spongycastle.asn1.x500.style.BCStyle
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.*
import javax.security.auth.x500.X500Principal

actual object AccountHttpClient {
    actual fun client(pinnedEndpoint: Pair<String, String>?) = HttpClient(OkHttp) {
        expectSuccess = false
        install(HttpTimeout) {
            requestTimeoutMillis = Account.REQUEST_TIMEOUT_MS
        }
        pinnedEndpoint?.let {
            engine {
                preconfigured = AccountCertificatePinner.getOkHttpClient(pinnedEndpoint.first, pinnedEndpoint.second)
            }
        }
    }
}

private class AccountCertificatePinner {

    companion object {
        fun getOkHttpClient(requestHostname: String, commonName: String): OkHttpClient {
            var trustManager: X509TrustManager? = null
            var sslSocketFactory: SSLSocketFactory? = null
            val builder = OkHttpClient.Builder()
            try {
                val keyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)
                val inputStream = Account.certificate.toByteArray().inputStream()
                val certificateFactory = CertificateFactory.getInstance("X.509")
                val certificate = certificateFactory.generateCertificate(inputStream)
                keyStore.setCertificateEntry("pia", certificate)
                inputStream.close()
                val trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                trustManagerFactory.init(keyStore)
                val trustManagers = trustManagerFactory.trustManagers
                check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
                    "Unexpected default trust managers:" + Arrays.toString(trustManagers)
                }
                trustManager = trustManagers[0] as X509TrustManager
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustManagers, SecureRandom())
                sslSocketFactory = sslContext.socketFactory
            } catch (e: KeyStoreException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: CertificateException) {
                e.printStackTrace()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: KeyManagementException) {
                e.printStackTrace()
            }
            builder.connectTimeout(Account.REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)

            if (trustManager != null && sslSocketFactory != null) {
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