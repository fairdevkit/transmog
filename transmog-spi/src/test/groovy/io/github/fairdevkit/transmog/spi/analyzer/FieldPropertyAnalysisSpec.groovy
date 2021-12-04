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
package io.github.fairdevkit.transmog.spi.analyzer

import io.github.fairdevkit.transmog.annotations.Predicate
import io.github.fairdevkit.transmog.spi.reader.ArgumentStrategy
import io.github.fairdevkit.transmog.spi.reader.ValueConverter
import spock.lang.Specification
import java.lang.invoke.MethodHandle

class FieldPropertyAnalysisSpec extends Specification {
    // convenience closure
    def findField = { type, name = "value" -> type.declaredFields.find { it.name == name } }

    def "test"() {
        given:
        def builder = new FieldPropertyAnalysis.Builder() {
            FieldPropertyAnalysis build() {
                new FieldPropertyAnalysis(annotation, name, type, intrinsicType, accessor, factory, valueConverter, wrapperHandler, nested) {}
            }
        }

        when:
        builder.build()
        then:
        def ex1 = thrown NullPointerException
        ex1.message == "Annotation analysis property 'annotation' cannot be null"

        when:
        builder.annotation(Mock(Predicate)).build()
        then:
        def ex2 = thrown NullPointerException
        ex2.message == "Field analysis property 'name' cannot be null"

        when:
        builder.name("foo").build()
        then:
        def ex3 = thrown NullPointerException
        ex3.message == "Field analysis property 'type' cannot be null"

        when:
        builder.type(Object).build()
        then:
        def ex4 = thrown NullPointerException
        ex4.message == "Field analysis property 'intrinsicType' cannot be null"

        when:
        builder.intrinsicType(String).build()
        then:
        def ex5 = thrown NullPointerException
        ex5.message == "Field analysis property 'accessor' cannot be null"

        when:
        builder.accessor(Mock(MethodHandle)).build()
        then:
        def ex6 = thrown NullPointerException
        ex6.message == "Field analysis property 'factory' cannot be null"

        when:
        builder.factory(Mock(ArgumentStrategy.Factory)).build()
        then:
        def ex7 = thrown NullPointerException
        ex7.message == "Field analysis property 'valueConverter' cannot be null"

        when:
        def property = builder.valueConverter(Mock(ValueConverter)).build()
        then:
        notThrown NullPointerException
    }
}
