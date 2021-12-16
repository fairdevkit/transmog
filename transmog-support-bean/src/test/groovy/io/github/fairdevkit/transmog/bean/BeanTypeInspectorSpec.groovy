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
import io.github.fairdevkit.transmog.spi.analyzer.IntrinsicTypeResolver
import io.github.fairdevkit.transmog.spi.analyzer.TransmogAnalyzerException
import io.github.fairdevkit.transmog.spi.writer.WrapperHandler
import io.github.fairdevkit.transmog.test.Beans
import io.github.fairdevkit.transmog.test.Builders
import io.github.fairdevkit.transmog.test.Constants
import io.github.fairdevkit.transmog.test.Records
import spock.lang.Specification

class BeanTypeInspectorSpec extends Specification {
    /** System under test */
    def inspector = new BeanTypeInspector()

    // convenience closure
    def resolver = { type = Object ->
        [ Mock(IntrinsicTypeResolver) {
            supports(_) >> true
            resolve(_) >> type
        } ]
    }
    def handler = { type = Object ->
        [ Mock(WrapperHandler) ]
    }

    def "test for bean type candidates"() {
        expect:
        inspector.supports(type) == result

        where:
        type                           || result
        Records.StringPropertyRecord   || false
        Builders.StringPropertyBuilder || false
        Beans.StringPropertyBean       || true
    }

    def "inspect a bean type with an absent getter method"() {
        when:
        inspector.inspect Beans.Invalid.AbsentGetter, Predicate, resolver(), handler(), { bldr -> }

        then:
        def ex = thrown TransmogAnalyzerException
        ex.message == "Could not find getter method for bean ${Beans.Invalid.AbsentGetter}"
    }

    def "inspect a bean type with an absent setter method"() {
        when:
        inspector.inspect Beans.Invalid.AbsentSetter, Predicate, resolver(), handler(), { bldr -> }

        then:
        def ex = thrown TransmogAnalyzerException
        ex.message == "Could not find setter method for bean ${Beans.Invalid.AbsentSetter}"
    }

    def "inspect a bean type containing a string property"() {
        expect:
        inspector.inspect Beans.StringPropertyBean, Predicate, resolver(String), handler(), { bldr ->
            assert bldr.annotation.value() == Constants.PREDICATE_VALUE
            assert bldr.name == "value"
            assert bldr.type == String
            assert bldr.intrinsicType == String
            // TODO assert accessor value
            // TODO assert mutator value
        }
    }

    def "inspect a bean type containing a boolean property"() {
        expect:
        inspector.inspect Beans.BooleanPropertyBean, Predicate, resolver(boolean), handler(), { bldr ->
            assert bldr.annotation.value() == Constants.PREDICATE_VALUE
            assert bldr.name == "value"
            assert bldr.type == boolean
            assert bldr.intrinsicType == boolean
        }
    }
}
