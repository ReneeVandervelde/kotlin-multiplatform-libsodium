package com.ionspin.kotlin.crypto.auth

import com.ionspin.kotlin.crypto.getSodium
import ext.libsodium.com.ionspin.kotlin.crypto.toUByteArray
import ext.libsodium.com.ionspin.kotlin.crypto.toUInt8Array
import org.khronos.webgl.Uint8Array

actual object Auth {
    actual fun authKeygen(): UByteArray {
        return getSodium().crypto_auth_keygen().toUByteArray()
    }

    actual fun auth(message: UByteArray, key: UByteArray): UByteArray {
        return getSodium().crypto_auth(
            message.toUInt8Array(),
            key.toUInt8Array()
        ).toUByteArray()

    }

    actual fun authVerify(mac: UByteArray, message: UByteArray, key: UByteArray): Boolean {
        return getSodium().crypto_auth_verify(
            mac.toUInt8Array(),
            message.toUInt8Array(),
            key.toUInt8Array()
        )
    }

    actual fun authHmacSha256Keygen(): UByteArray {
        return getSodium().crypto_auth_hmacsha256_keygen().toUByteArray()
    }

    actual fun authHmacSha256(message: UByteArray, key: UByteArray): UByteArray {
        return getSodium().crypto_auth_hmacsha256(
            message.toUInt8Array(),
            key.toUInt8Array()
        ).toUByteArray()
    }

    actual fun authHmacSha256Verify(
        mac: UByteArray,
        message: UByteArray,
        key: UByteArray
    ): Boolean {
        return getSodium().crypto_auth_hmacsha256_verify(
            mac.toUInt8Array(),
            message.toUInt8Array(),
            key.toUInt8Array()
        )
    }

    actual fun authHmacSha512Keygen(): UByteArray {
        return getSodium().crypto_auth_hmacsha512_keygen().toUByteArray()
    }

    actual fun authHmacSha512(message: UByteArray, key: UByteArray): UByteArray {
        return getSodium().crypto_auth_hmacsha512(
            message.toUInt8Array(),
            key.toUInt8Array()
        ).toUByteArray()
    }

    actual fun authHmacSha512Verify(
        mac: UByteArray,
        message: UByteArray,
        key: UByteArray
    ): Boolean {
        return getSodium().crypto_auth_hmacsha512_verify(
            mac.toUInt8Array(),
            message.toUInt8Array(),
            key.toUInt8Array()
        )
    }

}
