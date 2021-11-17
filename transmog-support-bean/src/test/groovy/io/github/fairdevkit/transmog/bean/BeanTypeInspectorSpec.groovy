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
import io.github.fairdevkit.transmog.spi.analyzer.TransmogAnalyzerException
import io.github.fairdevkit.transmog.test.Beans
import io.github.fairdevkit.transmog.test.Constants
import spock.lang.Specification

class BeanTypeInspectorSpec extends Specification {
    /** System under test */
    def inspector = new BeanTypeInspector()

    def "inspecting a bean type with an absent getter method"() {
        when:
        inspector.inspect Beans.Invalid.AbsentGetter, Predicate, { bldr, fld -> }

        then:
        def ex = thrown TransmogAnalyzerException
        ex.message == "Could not find getter method for bean ${Beans.Invalid.AbsentGetter}"
    }

    def "inspecting a bean type with an absent setter method"() {
        when:
        inspector.inspect Beans.Invalid.AbsentSetter, Predicate, { bldr, fld -> }

        then:
        def ex = thrown TransmogAnalyzerException
        ex.message == "Could not find setter method for bean ${Beans.Invalid.AbsentSetter}"
    }

    def "inspecting a bean type containing a string property"() {
        expect:
        inspector.inspect Beans.StringPropertyBean, Predicate, { bldr, fld ->
            assert bldr.annotation.value() == Constants.PREDICATE_VALUE
            assert bldr.name == "value"
            assert bldr.type == String
            // TODO assert accessor value
            // TODO assert mutator value
        }
    }

    def "inspecting a bean type containing a boolean property"() {
        expect:
        inspector.inspect Beans.BooleanPropertyBean, Predicate, { bldr, fld ->
            assert bldr.annotation.value() == Constants.PREDICATE_VALUE
            assert bldr.name == "value"
            assert bldr.type == boolean
        }
    }
}
