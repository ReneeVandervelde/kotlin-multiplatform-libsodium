package com.ionspin.kotlin.crypto.hash.blake2b
import com.ionspin.kotlin.crypto.util.toHexString
import kotlinx.cinterop.*
import libsodium.*
/**
 * Created by Ugljesa Jovanovic
 * ugljesa.jovanovic@ionspin.com
 * on 14-Jul-2019
 */

@ExperimentalUnsignedTypes
actual class Blake2bDelegated actual constructor(key: UByteArray?, hashLength: Int) : Blake2b {
    override val MAX_HASH_BYTES: Int = 64

    override fun update(data: UByteArray) {
        TODO("not implemented yet")
    }

    override fun update(data: String) {
        TODO("not implemented yet")
    }

    override fun digest(): UByteArray {
        val inputString = "test"
        val hashLength = 64
        val key : String? = null
        val result2 = allocEverything(inputString, key, hashLength)
        val result2String = result2.toHexString()
        println(result2String)
        return ubyteArrayOf(0U)
    }

    fun allocEverything(inputString: String, key: String?, hashLength: Int) : UByteArray {
        val res = memScoped {
            val result = allocArray<UByteVar>(hashLength)
            println("Alloced: $result")
            crypto_generichash(
                result,
                hashLength.toULong(),
                inputString.encodeToByteArray().toUByteArray().toCValues(),
                inputString.length.toULong(),
                key?.run { this.encodeToByteArray().toUByteArray().toCValues() },
                key?.length?.toULong() ?: 0UL
            )
            println("Result: $result")
            UByteArray(hashLength) {
                result[it]
            }
        }
        return res
    }

    override fun digestString(): String {
        TODO("not implemented yet")
    }
}

@Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")
actual object Blake2bStateless : Blake2bStatelessInterface {
    override fun digest(inputString: String, key: String?, hashLength: Int): UByteArray {
//        return allocEverything(inputString, key, hashLength)
        println("Input $inputString, ${key ?: "null"}, $hashLength")
        val hashResult = UByteArray(MAX_HASH_BYTES)
        val hashResultPinned = hashResult.pin()
        crypto_generichash(
            hashResultPinned.addressOf(0),
            hashLength.toULong(),
            inputString.encodeToByteArray().toUByteArray().toCValues(),
            inputString.length.toULong(),
            key?.run { this.encodeToByteArray().toUByteArray().toCValues() },
            key?.length?.toULong() ?: 0UL
        )
        println("HashPointer: ${hashResult.toHexString()}")
        println(hashResult.toHexString())
        return hashResult
    }




    override fun digest(inputMessage: UByteArray, key: UByteArray, hashLength: Int): UByteArray {
        val hashResult = UByteArray(MAX_HASH_BYTES)

        crypto_generichash(
            StableRef.create(hashResult).asCPointer().reinterpret(),
            hashLength.toULong(),
            inputMessage.toCValues(),
            inputMessage.size.toULong(),
            key.toCValues(),
            key.size.toULong() ?: 0UL
        )
        println("HashPointer: ${hashResult.toHexString()}")
        println(hashResult.toHexString())
        return hashResult

    }


}