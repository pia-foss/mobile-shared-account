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
import kotlinx.cinterop.*
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.CoreFoundation.*
import platform.Foundation.*
import platform.Security.*
import platform.posix.memcpy


actual object MessageVerificator {

    actual fun verifyMessage(message: String, key: String): Boolean {
        val publicKeyData = NSData.create(
            base64EncodedString =
            PUBLIC_KEY
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\n", ""),
            options = NSDataBase64Encoding64CharacterLineLength
        ) ?: return false

        val keyData = NSData.create(
            base64EncodedString = key,
            options = NSDataBase64Encoding64CharacterLineLength
        ) ?: return false
        val keyUByteArray = UByteArray(keyData.length.convert()).apply {
            usePinned { keyPinned ->
                memcpy(keyPinned.addressOf(0), keyData.bytes, keyData.length)
            }
        }

        val publicKeyDataRef = CFBridgingRetain(publicKeyData) as CFDataRef
        val sizeNumberRef = CFBridgingRetain(publicKeyData.length * 8u) as CFNumberRef
        val attrDictionaryRef = CFDictionaryCreateMutable(kCFAllocatorDefault, 3, null, null)
        CFDictionaryAddValue(attrDictionaryRef, kSecAttrKeyType, kSecAttrKeyTypeRSA)
        CFDictionaryAddValue(attrDictionaryRef, kSecAttrKeyClass, kSecAttrKeyClassPublic)
        CFDictionaryAddValue(attrDictionaryRef, kSecAttrKeySizeInBits, sizeNumberRef)

        var status = errSecBadReq
        memScoped {
            val error = alloc<CFErrorRefVar>()
            val secKeyRef = SecKeyCreateWithData(publicKeyDataRef, attrDictionaryRef, error.ptr)

            val messageByteArray = message.encodeToByteArray()
            val digestMessageUByteArray = UByteArray(CC_SHA256_DIGEST_LENGTH)
            messageByteArray.usePinned { messagePinned ->
                digestMessageUByteArray.usePinned { digestPinned ->
                    CC_SHA256(messagePinned.addressOf(0), messageByteArray.size.convert(), digestPinned.addressOf(0))
                }
            }

            status = SecKeyRawVerify(
                secKeyRef,
                32772, // PKCS1SHA256
                digestMessageUByteArray.toCValues(),
                digestMessageUByteArray.size.convert(),
                keyUByteArray.toCValues(),
                keyUByteArray.size.convert(),
            )

            CFRelease(publicKeyDataRef)
            CFRelease(sizeNumberRef)
            CFRelease(attrDictionaryRef)
            CFRelease(secKeyRef)
        }
        return status == errSecSuccess
    }
}