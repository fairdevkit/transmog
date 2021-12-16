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


import io.github.fairdevkit.transmog.spi.analyzer.FieldPropertyAnalysis
import io.github.fairdevkit.transmog.spi.reader.TransmogReaderException
import io.github.fairdevkit.transmog.test.Beans
import io.github.fairdevkit.transmog.test.Constants
import spock.lang.Shared
import spock.lang.Specification
import java.lang.invoke.MethodHandles

class BeanInstanceStrategySpec extends Specification {
    @Shared factory = new BeanInstanceStrategy.Factory()

    // convenience closures
    def findMethod = { type, name = "getValue" -> type.methods.find { it.name == name } }
    def property = { type, name = "value" ->
        Mock(BeanPropertyAnalysis) {
            getMutator() >> MethodHandles.lookup().unreflect(findMethod(type, "set${name.capitalize()}"))
            getName() >> name
        }
    }

    def "create a bean instance for a private constructor class"() {
        when:
        factory.create Beans.Invalid.PrivateConstructor

        then:
        def ex = thrown TransmogReaderException
        ex.message == "Could not find bean constructor for ${Beans.Invalid.PrivateConstructor}"
    }

    def "create a bean instance for a throwing constructor class"() {
        when:
        factory.create Beans.Invalid.ThrowingConstructor

        then:
        def ex = thrown TransmogReaderException
        ex.message == "Could not invoke bean constructor for ${Beans.Invalid.ThrowingConstructor}"
        with (ex.cause) {
            it instanceof IllegalStateException
            message == "for testing"
        }
    }

    def "set a bean property with a property analysis other than the intended bean property analysis"() {
        given:
        def strategy = factory.create Beans.StringPropertyBean
        def property = Mock(FieldPropertyAnalysis) {
            getName() >> "mock"
        }

        when:
        strategy.add(property, Constants.LITERAL_FOO)

        then:
        def ex = thrown TransmogReaderException
        ex.message == "Could not find bean mutator method for property mock"
    }

    def "set a bean property for a throwing setter method"() {
        given:
        def strategy = factory.create Beans.Invalid.ThrowingMutator
        def property = property Beans.Invalid.ThrowingMutator

        when:
        strategy.add(property, Constants.LITERAL_FOO)

        then:
        def ex = thrown TransmogReaderException
        ex.message == "Could not invoke bean mutator method for property value"
    }

    def "create a bean instance through the strategy"() {
        given:
        def strategy = factory.create Beans.StringPropertyBean
        def property = property Beans.StringPropertyBean

        when:
        strategy.add(property, Constants.LITERAL_FOO)

        then:
        with (strategy.create()) {
            value == Constants.LITERAL_FOO
        }
    }

    def "create a bean instance inheriting from an abstract parent"() {
        given:
        def strategy = factory.create Beans.ExtendingChildBean
        and:
        def childProperty = property Beans.ExtendingChildBean, "flag"
        def parentProperty = property Beans.ExtendingChildBean, "value"

        when:
        strategy.add(childProperty, true)
        and:
        strategy.add(parentProperty, Constants.LITERAL_FOO)

        then:
        with (strategy.create()) {
            flag == true
            value == Constants.LITERAL_FOO
        }
    }
}
