/*
 * MIT License
 *
 * Copyright (c) 2021 fairdevkit
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.fairdevkit.transmog.common

import io.github.fairdevkit.transmog.test.Beans
import spock.lang.Specification

class ParameterizedIntrinsicTypeResolverSpec extends Specification {
    /** System under test */
    def resolver = new ParameterizedIntrinsicTypeResolver()

    // convenience closure
    def findField = { type, name = "value" -> type.declaredFields.find { it.name == name } }

    def "test for parameterized type candidates"() {
        given:
        def field = findField type

        expect:
        resolver.supports(field) == result

        where:
        type                                             || result
        Beans.IntPropertyBean                            || false
        Beans.IntegerPropertyBean                        || false
        Beans.StringPropertyBean                         || false
        Beans.StringCollectionPropertyBean               || true
        Beans.ParameterizedCollectionPropertyBean        || true
        Beans.ParameterizedNumberCollectionPropertyBean  || true
        Beans.StringUpperBoundCollectionPropertyBean     || true
        Beans.StringLowerBoundCollectionPropertyBean     || true
        Beans.WildcardCollectionPropertyBean             || true
        Beans.RawCollectionPropertyBean                  || true
//        Beans.ParameterizedArrayPropertyBean             || true
    }

    def "resolve a parameterized type to the expected type"() {
        given:
        def field = findField type

        expect:
        resolver.resolve(field) == result

        where:
        type                                            || result
        Beans.StringCollectionPropertyBean              || String
        Beans.ParameterizedCollectionPropertyBean       || Object
        Beans.ParameterizedNumberCollectionPropertyBean || Number
        Beans.StringUpperBoundCollectionPropertyBean    || String
        Beans.StringLowerBoundCollectionPropertyBean    || String
        Beans.WildcardCollectionPropertyBean            || Object
        Beans.RawCollectionPropertyBean                 || Object
//        Beans.ParameterizedArrayPropertyBean            || Object
    }
}
