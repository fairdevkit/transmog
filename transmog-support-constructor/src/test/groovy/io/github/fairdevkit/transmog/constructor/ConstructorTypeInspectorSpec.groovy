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

import io.github.fairdevkit.transmog.annotations.Predicate
import io.github.fairdevkit.transmog.spi.analyzer.IntrinsicTypeResolver
import io.github.fairdevkit.transmog.spi.analyzer.TransmogAnalyzerException
import io.github.fairdevkit.transmog.spi.writer.WrapperHandler
import io.github.fairdevkit.transmog.test.Beans
import io.github.fairdevkit.transmog.test.Builders
import io.github.fairdevkit.transmog.test.Constants
import io.github.fairdevkit.transmog.test.Constructors
import io.github.fairdevkit.transmog.test.Records
import spock.lang.Specification
import java.beans.ConstructorProperties

class ConstructorTypeInspectorSpec extends Specification {
    /** System under test */
    def inspector = new ConstructorTypeInspector()

    // convenience closures
    def resolver = { type = Object ->
        [ Mock(IntrinsicTypeResolver) {
            supports(_) >> true
            resolve(_) >> type
        }]
    }
    def handler = { type = Object->
        [ Mock(WrapperHandler) ]
    }

    def "test for constructor type candidates"() {
        expect:
        inspector.supports(type) == result

        where:
        type                                   || result
        Beans.StringPropertyBean               || false
        Records.StringPropertyRecord           || false
        Builders.StringPropertyBuilder         || false
        Constructors.StringPropertyConstructor || true
    }

    def "inspect a constructor type without an annotation"() {
        when:
        inspector.inspect Constructors.Invalid.MissingAnnotation, Predicate, resolver(), handler(), { bldr -> }

        then:
        def ex = thrown TransmogAnalyzerException
        ex.message == "Could not find @ConstructorProperties annotated constructor"
    }

    def "inspect a constructor type with an absent getter method"() {
        when:
        inspector.inspect Constructors.Invalid.AbsentGetter, Predicate, resolver(), handler(), { bldr -> }

        then:
        def ex = thrown TransmogAnalyzerException
        ex.message == "Could not find getter method for property value"
    }

    def "inspect a constructor type containing a string property"() {
        expect:
        inspector.inspect Constructors.StringPropertyConstructor, Predicate, resolver(String), handler(), { bldr ->
            assert bldr.annotation.value() == Constants.PREDICATE_VALUE
            assert bldr.name == "value"
            assert bldr.index == 0
            assert bldr.type == String
            assert bldr.intrinsicType == String
        }
    }
}
