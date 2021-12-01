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
package io.github.fairdevkit.transmog.record

import io.github.fairdevkit.transmog.annotations.Predicate
import io.github.fairdevkit.transmog.spi.reader.ArgumentStrategy
import io.github.fairdevkit.transmog.spi.reader.TransmogReaderException
import io.github.fairdevkit.transmog.spi.reader.ValueConverter
import io.github.fairdevkit.transmog.test.Constants
import io.github.fairdevkit.transmog.test.Records
import spock.lang.Specification
import java.lang.invoke.MethodHandle

class RecordInstanceStrategySpec extends Specification {
    // convenience closures
    def findMethod = { type, name = "value" -> type.recordComponents.find { it.name == name}.accessor }
    def prop = { type, name = "value" ->
        RecordPropertyAnalysis.builder()
                .name(name)
                .type(String)
                .intrinsicType(String)
                .accessor(Mock(MethodHandle))
                .factory(Mock(ArgumentStrategy.Factory))
                .valueConverter(Mock(ValueConverter))
                .annotation(Mock(Predicate))
                .build()
    }

    def "set a record component value for a wrong component name"() {
        given:
        def strategy = new RecordInstanceStrategy(Records.StringPropertyRecord)
        def property = prop Records.StringPropertyRecord, "foo"

        when:
        strategy.add(property, Constants.LITERAL_BAR)

        then:
        def ex = thrown TransmogReaderException
        ex.message == "Could not recognise record property foo"
    }

    def "create a record instance for a default-value-providing constructor"() {
        given:
        def strategy = new RecordInstanceStrategy(Records.DefaultValueConstructorRecord)

        when:
        def record = strategy.create()

        then:
        with (record) {
            value == Constants.LITERAL_FOO
        }
    }

    def "create a record instance with a string property"() {
        given:
        def strategy = new RecordInstanceStrategy(Records.StringPropertyRecord)
        def property = prop Records.StringPropertyRecord

        when:
        strategy.add(property, Constants.LITERAL_FOO)

        then:
        with (strategy.create()) {
            value == Constants.LITERAL_FOO
        }
    }
}
