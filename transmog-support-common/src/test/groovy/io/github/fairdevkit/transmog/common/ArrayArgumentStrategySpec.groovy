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

import io.github.fairdevkit.transmog.spi.reader.TransmogReaderException
import spock.lang.Specification

class ArrayArgumentStrategySpec extends Specification {
    // convenience closure
    def newStrategy = { type, size -> new ArrayArgumentStrategy(type, size) }

    def "adding values beyond the allocated array size"() {
        setup:
        def strategy = newStrategy String, 1

        when:
        strategy.add "foo"
        and:
        strategy.add "bar"

        then:
        def ex = thrown TransmogReaderException
        ex.message == "Could not add more values to the array than the allocated size"
    }

    def "creating an array instance without populating it to the allocated size"() {
        setup:
        def strategy = newStrategy String, 2

        when:
        strategy.add "foo"
        and:
        strategy.create()

        then:
        def ex = thrown TransmogReaderException
        ex.message == "Array was not populated up to its allocated size"
    }

    def "populating an array instance to its allocated size"() {
        setup:
        def strategy = newStrategy String, 2

        when:
        strategy.add "foo"
        and:
        strategy.add "bar"

        then:
        strategy.create() == [ "foo", "bar" ]
    }
}
