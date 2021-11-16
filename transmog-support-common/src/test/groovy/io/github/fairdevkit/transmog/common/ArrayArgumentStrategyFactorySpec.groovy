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

import io.github.fairdevkit.transmog.spi.analyzer.FieldPropertyAnalysis
import io.github.fairdevkit.transmog.test.Beans
import spock.lang.Specification

class ArrayArgumentStrategyFactorySpec extends Specification {
    /** System under test */
    def factory = new ArrayArgumentStrategy.Factory()

    def "test for array argument candidates"() {
        expect:
        factory.supports(type) == result

        where:
        type                     || result
        int                      || false
        Integer                  || false
        Object                   || false
        Collection               || false
        List                     || false
        ArrayList                || false
        Set                      || false
        HashSet                  || false
        Beans.StringPropertyBean || false
        int[]                    || true
        int[][]                  || true
        Integer[]                || true
        String[]                 || true
        Object[]                 || true
    }

    def "strategy creation does not return null"() {
        setup:
        def property = Mock(FieldPropertyAnalysis) {
            getType() >> Object
        }

        expect:
        factory.create(property, 1) != null
    }
}
