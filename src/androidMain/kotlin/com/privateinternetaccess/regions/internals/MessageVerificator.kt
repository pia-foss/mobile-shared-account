package com.privateinternetaccess.regions.internals

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

import com.privateinternetaccess.regions.internals.Regions.Companion.PUBLIC_KEY
import android.util.Base64
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec


actual object MessageVerificator {

    actual fun verifyMessage(message: String, key: String): Boolean {
        try {
            var pubKeyPEM = PUBLIC_KEY.replace("-----BEGIN PUBLIC KEY-----\n", "")
            pubKeyPEM = pubKeyPEM.replace("-----END PUBLIC KEY-----", "")
            val pubKeyEncoded = Base64.decode(pubKeyPEM, Base64.DEFAULT)
            val pubKeySpec = X509EncodedKeySpec(pubKeyEncoded)
            val kf = KeyFactory.getInstance("RSA")
            val pubKey = kf.generatePublic(pubKeySpec)
            val sig = Signature.getInstance("SHA256withRSA")
            sig.initVerify(pubKey)
            sig.update(message.toByteArray())
            return sig.verify(Base64.decode(key, Base64.DEFAULT))
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: SignatureException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        return false
    }
}