/*
 *    Copyright 2019 Ugljesa Jovanovic
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.ionspin.kotlin.crypto.keyderivation.argon2

import com.ionspin.kotlin.crypto.hash.blake2b.Blake2b
import com.ionspin.kotlin.crypto.util.arrayChunked
import com.ionspin.kotlin.crypto.util.fromLittleEndianArrayToULong
import com.ionspin.kotlin.crypto.util.plus
import com.ionspin.kotlin.crypto.util.rotateRight
import com.ionspin.kotlin.crypto.util.toLittleEndianUByteArray
import com.ionspin.kotlin.crypto.util.xor

/**
 * Created by Ugljesa Jovanovic
 * ugljesa.jovanovic@ionspin.com
 * on 16-May-2020
 */
object Argon2Utils {

    const val R1 = 32
    const val R2 = 24
    const val R3 = 16
    const val R4 = 63

    //based on Blake2b mixRound
    private fun mixRound(input: UByteArray): Array<ULong> {
        var v = input.arrayChunked(8).map { it.fromLittleEndianArrayToULong() }.toTypedArray()
        v = mix(v, 0, 4, 8, 12)
        v = mix(v, 1, 5, 9, 13)
        v = mix(v, 2, 6, 10, 14)
        v = mix(v, 3, 7, 11, 15)
        v = mix(v, 0, 5, 10, 15)
        v = mix(v, 1, 6, 11, 12)
        v = mix(v, 2, 7, 8, 13)
        v = mix(v, 3, 4, 9, 14)
        return v
    }

    private fun inPlaceMix(v: UByteArray, a: Int, b: Int, c: Int, d: Int) {

    }

    //Based on Blake2b mix
    private fun mix(v: Array<ULong>, a: Int, b: Int, c: Int, d: Int): Array<ULong> {
        v[a] = (v[a] + v[b] + 2U * (v[a] and 0xFFFFFFFFUL) * (v[b] and 0xFFFFFFFFUL))
        v[d] = (v[d] xor v[a]) rotateRight R1
        v[c] = (v[c] + v[d] + 2U * (v[c] and 0xFFFFFFFFUL) * (v[d] and 0xFFFFFFFFUL))
        v[b] = (v[b] xor v[c]) rotateRight R2
        v[a] = (v[a] + v[b] + 2U * (v[a] and 0xFFFFFFFFUL) * (v[b] and 0xFFFFFFFFUL))
        v[d] = (v[d] xor v[a]) rotateRight R3
        v[c] = (v[c] + v[d] + 2U * (v[c] and 0xFFFFFFFFUL) * (v[d] and 0xFFFFFFFFUL))
        v[b] = (v[b] xor v[c]) rotateRight R4
        return v
    }

    private fun extractColumnFromGBlock(gBlock: UByteArray, columnPosition: Int): UByteArray {
        val result = UByteArray(128) { 0U }
        for (i in 0..7) {
            gBlock.copyOfRange(i * 128 + (columnPosition * 16), i * 128 + (columnPosition * 16) + 16)
                .copyInto(result, i * 16)
        }
        return result
    }

    private fun copyIntoGBlockColumn(gBlock: UByteArray, columnPosition: Int, columnData: UByteArray) {
        for (i in 0..7) {
            val column = columnData.copyOfRange(i * 16, i * 16 + 16)
            column.copyInto(gBlock, i * 128 + columnPosition * 16)
        }
    }

    internal fun compressionFunctionG(
        previousBlock: UByteArray,
        referenceBlock: UByteArray,
        currentBlock: UByteArray,
        xorWithCurrentBlock: Boolean
    ): UByteArray {
        val r = referenceBlock xor previousBlock
        val q = UByteArray(1024) { 0U }
        val z = UByteArray(1024) { 0U }
        // Do the argon/blake2b mixing on rows
        for (i in 0..7) {
            val startOfRow = (i * 8 * 16)
            val endOfRow = startOfRow + (8 * 16)
            val rowToMix = r.copyOfRange(startOfRow, endOfRow)
            mixRound(rowToMix)
                .map { it.toLittleEndianUByteArray() }
                .flatMap { it.asIterable() }
                .toUByteArray()
                .copyInto(q, startOfRow)

        }
        // Do the argon/blake2b mixing on columns
        for (i in 0..7) {
            copyIntoGBlockColumn(
                z,
                i,
                mixRound(extractColumnFromGBlock(q, i))
                    .map { it.toLittleEndianUByteArray() }
                    .flatMap { it.asIterable() }
                    .toUByteArray()
            )
        }
        val final = if (xorWithCurrentBlock) {
            (z xor r) xor currentBlock
        } else {
            z xor r
        }
        return final
    }

    internal fun argonBlake2bArbitraryLenghtHash(input: UByteArray, length: UInt): UByteArray {
        if (length <= 64U) {
            return Blake2b.digest(inputMessage = length + input, hashLength = length.toInt())
        }
        //We can cast to int because UInt even if MAX_VALUE divided by 32 is guaranteed not to overflow
        val numberOf64ByteBlocks = (1U + ((length - 1U) / 32U) - 2U).toInt() // equivalent  to ceil(length/32) - 2
        val v = Array<UByteArray>(numberOf64ByteBlocks) { ubyteArrayOf() }
        v[0] = Blake2b.digest(length + input)
        for (i in 1 until numberOf64ByteBlocks) {
            v[i] = Blake2b.digest(v[i - 1])
        }
        val remainingPartOfInput = length.toInt() - numberOf64ByteBlocks * 32
        val vLast = Blake2b.digest(v[numberOf64ByteBlocks - 1], hashLength = remainingPartOfInput)
        val concat =
            (v.map { it.copyOfRange(0, 32) })
                .plus(listOf(vLast))
                .foldRight(ubyteArrayOf()) { arrayOfUBytes, acc -> arrayOfUBytes + acc }

        return concat
    }

    /**
     * Validates the argon 2 parameters.
     * Since Kotlin arrays that we are currently using cannot have more than 2^31 bytes, we don't need to check
     * sizes for password, salt, key and associated data. Also since UInt is 32bit we cant set more than 2^32-1 of
     * tagLength, requested memory size and number of iterations, so no need to check for upper bound, just lower.
     */
    internal fun validateArgonParameters(
        password: UByteArray,
        salt: UByteArray,
        parallelism: Int ,
        tagLength: UInt,
        requestedMemorySize: UInt ,
        numberOfIterations: Int ,
        key: UByteArray,
        associatedData: UByteArray,
        argonType: ArgonType
    ) {

        //Parallelism
        if (parallelism > 0xFFFFFF) {
            throw Argon2LanesTooMany(parallelism)
        }
        if (parallelism <= 0) {
            throw Argon2LanesTooFew(parallelism)
        }
        //Tag length
        if (tagLength <= 0U) {
            throw Argon2TagTooShort(tagLength)
        }
        //Requested memory
        if (requestedMemorySize < 8U || requestedMemorySize < (8 * parallelism).toUInt()) {
            throw Argon2MemoryTooLitlle(requestedMemorySize)
        }
        //Number of iterations
        if (numberOfIterations <= 0) {
            throw Argon2TimeTooShort(numberOfIterations)
        }

    }
}