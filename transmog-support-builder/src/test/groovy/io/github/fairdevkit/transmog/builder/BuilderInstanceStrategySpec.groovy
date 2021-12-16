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
package io.github.fairdevkit.transmog.builder

import io.github.fairdevkit.transmog.spi.reader.TransmogReaderException
import io.github.fairdevkit.transmog.test.Builders
import io.github.fairdevkit.transmog.test.Constants
import spock.lang.Shared
import spock.lang.Specification
import java.lang.invoke.MethodHandles

class BuilderInstanceStrategySpec extends Specification {
    @Shared factory = new BuilderInstanceStrategy.Factory()

    // convenience closure
    def findMethod = { type, name -> type.methods.find { it.name == name } }
    def property = { type, name = "value" ->
        Mock(BuilderPropertyAnalysis) {
            getBuilderProperty() >> MethodHandles.lookup().unreflect(findMethod(type, name))
        }
    }

    def "set a builder property for a throwing property method"() {
        given:
        def strategy = factory.create Builders.Invalid.ThrowingBuilderProperty
        def property = property Builders.Invalid.ThrowingBuilderProperty.Builder

        when:
        strategy.add(property, Constants.LITERAL_FOO)

        then:
        def ex = thrown TransmogReaderException
        ex.message == ""//TODO
    }

    def "invoke the builder's build method without setting any values"() {
        given:
        def strategy = factory.create Builders.NullCheckingCtorTargetBuilder

        when:
        strategy.create()

        then:
        def ex = thrown TransmogReaderException
        ex.message == ""//TODO
        with (ex.cause) {
            it instanceof NullPointerException
            message == "value property cannot be null"
        }
    }

    def "create a builder instance through the strategy"() {
        given:
        def strategy = factory.create Builders.StringPropertyBuilder
        def property = property Builders.StringPropertyBuilder.Builder

        when:
        strategy.add(property, Constants.LITERAL_FOO)
        and:
        def instance = strategy.create()

        then:
        with (instance) {
            value == Constants.LITERAL_FOO
        }
    }

    def "create a child extending an abstract base class builder through the strategy"() {
        given:
        def strategy = factory.create Builders.ExtendingChildBuilder
        def valueProperty = property Builders.ExtendingChildBuilder.Builder
        def flagProperty = property Builders.ExtendingChildBuilder.Builder, "flag"

        when:
        strategy.add(valueProperty, Constants.LITERAL_FOO)
        and:
        strategy.add(flagProperty, true)

        then:
        with (strategy.create()) {
            value == Constants.LITERAL_FOO
            flag == true
        }
    }
}
