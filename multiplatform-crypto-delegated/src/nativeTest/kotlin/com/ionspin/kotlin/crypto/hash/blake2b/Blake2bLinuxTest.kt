package com.ionspin.kotlin.crypto.hash.blake2b

/**
 * Created by Ugljesa Jovanovic
 * ugljesa.jovanovic@ionspin.com
 * on 24-May-2020
 */

import com.ionspin.kotlin.crypto.Crypto
import com.ionspin.kotlin.crypto.hash.blake2b.Blake2bStateless
import com.ionspin.kotlin.crypto.util.testBlocking
import interop.*
import kotlinx.cinterop.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import libsodium.*

import kotlin.test.Test
import kotlin.test.assertTrue

class Blake2bLinuxTest {

    @Test
    fun testCinterop() {
        runBlocking {
            Crypto.initialize()
        }
//        val sodiumInitResult = sodium_init()
//        println("Sodium init $sodiumInitResult")
//        println("1")
    }

    @Test
    fun testBlake2bUpdateable() = testBlocking {
        val blake2b = Crypto.Blake2b.updateable()
        blake2b.update("test")
        val result = blake2b.digestString()
        println(result)
        assertTrue { result.length > 2 }
    }

    @Test
    fun testBlake2BStateless() = testBlocking {
        Blake2bStateless.digest("test")
    }
}