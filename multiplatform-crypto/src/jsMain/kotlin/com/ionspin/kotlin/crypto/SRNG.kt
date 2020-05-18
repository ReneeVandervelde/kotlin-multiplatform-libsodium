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

package com.ionspin.kotlin.crypto

import kotlin.browser.window

/**
 * Created by Ugljesa Jovanovic
 * ugljesa.jovanovic@ionspin.com
 * on 21-Sep-2019
 */
actual object SRNG {
    var counter = 0
    @ExperimentalUnsignedTypes
    actual fun getRandomBytes(amount: Int): Array<UByte> {
        val runningOnNode = jsTypeOf(window) == "undefined"
        val randomBytes = if (runningOnNode) {
            js("require('crypto')").randomBytes(amount).toJSON().data
        } else {
            js(
            """
                var randomArray = new Uint8Array(amount);
                var crypto = (self.crypto || self.msCrypto);        
                crypto.getRandomValues(randomArray);
            """
            )
            var randomArrayResult = js("Array.prototype.slice.call(randomArray);")
            randomArrayResult
        }

        return randomBytes as Array<UByte>
    }
}