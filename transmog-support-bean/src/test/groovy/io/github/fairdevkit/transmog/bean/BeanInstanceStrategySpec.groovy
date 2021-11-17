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
package io.github.fairdevkit.transmog.bean

import io.github.fairdevkit.transmog.annotations.Predicate
import io.github.fairdevkit.transmog.spi.analyzer.AnnotationPropertyAnalysis
import io.github.fairdevkit.transmog.spi.analyzer.FieldPropertyAnalysis
import io.github.fairdevkit.transmog.spi.reader.ArgumentStrategy
import io.github.fairdevkit.transmog.spi.reader.TransmogReaderException
import io.github.fairdevkit.transmog.spi.reader.ValueConverter
import io.github.fairdevkit.transmog.test.Beans
import spock.lang.Specification
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles

class BeanInstanceStrategySpec extends Specification {
    // convenience closures
    def findMethod = { type, name = "getValue" -> type.declaredMethods.find { it.name == name } }
    def prop = { type, name = "value" ->
        BeanPropertyAnalysis.builder()
            .mutator(MethodHandles.lookup().unreflect(findMethod(type, "setValue")))
            .name(name)
            .type(String)
            .accessor(Mock(MethodHandle))
            .factory(Mock(ArgumentStrategy.Factory))
            .valueConverter(Mock(ValueConverter))
            .annotation(Mock(Predicate))
            .build()
    }

    def "creating a bean instance for a private constructor class"() {
        when:
        new BeanInstanceStrategy(Beans.Invalid.PrivateConstructor)

        then:
        def ex = thrown TransmogReaderException
        ex.message == "Could not find bean constructor for ${Beans.Invalid.PrivateConstructor}"
    }

    def "creating a bean instance for a throwing constructor class"() {
        when:
        new BeanInstanceStrategy(Beans.Invalid.ThrowingConstructor)

        then:
        def ex = thrown TransmogReaderException
        ex.message == "Could not invoke bean constructor for ${Beans.Invalid.ThrowingConstructor}"
        with (ex.cause) {
            it instanceof IllegalStateException
            message == "for testing"
        }
    }

    def "setting a bean property with a property analysis other than the intended bean property analysis"() {
        given:
        def strategy = new BeanInstanceStrategy(Beans.StringPropertyBean)
        def property = Mock(FieldPropertyAnalysis) {
            getName() >> "mock"
        }

        when:
        strategy.add(property, "foo")

        then:
        def ex = thrown TransmogReaderException
        ex.message == "Could not find bean mutator method for property mock"
    }

    def "setting a bean property for a throwing setter method"() {
        given:
        def strategy = new BeanInstanceStrategy(Beans.Invalid.ThrowingMutator)
        def property = prop Beans.Invalid.ThrowingMutator

        when:
        strategy.add(property, "foo")

        then:
        def ex = thrown TransmogReaderException
        ex.message == "Could not invoke bean mutator method for property value"
    }

    def "creating a bean instance through the strategy"() {
        given:
        def strategy = new BeanInstanceStrategy(Beans.StringPropertyBean)
        and:
        def property = prop Beans.StringPropertyBean

        when:
        strategy.add(property, "foo")

        then:
        with (strategy.create()) {
            value == "foo"
        }
    }
}
