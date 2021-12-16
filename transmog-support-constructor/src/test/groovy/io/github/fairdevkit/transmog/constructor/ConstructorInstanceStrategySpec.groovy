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
package io.github.fairdevkit.transmog.constructor

import io.github.fairdevkit.transmog.spi.reader.TransmogReaderException
import io.github.fairdevkit.transmog.test.Constants
import io.github.fairdevkit.transmog.test.Constructors
import spock.lang.Shared
import spock.lang.Specification
import java.beans.ConstructorProperties

class ConstructorInstanceStrategySpec extends Specification {
    @Shared factory = new ConstructorInstanceStrategy.Factory()

    // convenience closure
    def property = { index = 0 ->
        Mock(ConstructorPropertyAnalysis) {
            getIndex() >> index
        }
    }

    def "test"() {
        when:
        factory.create(Constructors.Invalid.MissingAnnotation)

        then:
        def ex = thrown TransmogReaderException
        ex.message == "Could not find a @ConstructorProperties annotated constructor"
    }

    def "check the handling of mismatched annotation attributes and constructor parameters"() {
        when:
        factory.create(type)

        then:
        def ex = thrown TransmogReaderException
        ex.message == "@ConstructorProperties attributes do not match the parameter count on ${type.constructors.find {it.isAnnotationPresent(ConstructorProperties)}}"

        where:
        type                                            || _
        Constructors.Invalid.MissingAnnotationAttribute || _
        Constructors.Invalid.SuperfluousAttributes      || _
    }

    def "create a constructor based string property"() {
        given:
        def strategy = factory.create(Constructors.StringPropertyConstructor)

        when:
        strategy.add(property(), Constants.LITERAL_FOO)

        then:
        with (strategy.create()) {
            value == Constants.LITERAL_FOO
        }
    }

    def "create a child type constructor"() {
        given:
        def strategy = factory.create Constructors.ChildConstructor

        when:
        strategy.add(property(), Constants.LITERAL_FOO)
        and:
        strategy.add(property(1), true)

        then:
        with (strategy.create()) {
            value == Constants.LITERAL_FOO
            flag == true
        }
    }
}
