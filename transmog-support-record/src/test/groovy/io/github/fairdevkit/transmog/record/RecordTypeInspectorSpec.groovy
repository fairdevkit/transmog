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
import io.github.fairdevkit.transmog.spi.analyzer.IntrinsicTypeResolver
import io.github.fairdevkit.transmog.spi.writer.WrapperHandler
import io.github.fairdevkit.transmog.test.Beans
import io.github.fairdevkit.transmog.test.Builders
import io.github.fairdevkit.transmog.test.Constants
import io.github.fairdevkit.transmog.test.Records
import spock.lang.Specification
import java.util.stream.Stream

class RecordTypeInspectorSpec extends Specification {
    /** System under test */
    def inspector = new RecordTypeInspector()

    // convenience closure
    def resolver = { type ->
        [ Mock(IntrinsicTypeResolver) {
            supports(_) >> true
            resolve(_) >> type
        } ]
    }
    def handler = { ->
        [ Mock(WrapperHandler) ]
    }

    def "test for record type candidates"() {
        expect:
        inspector.supports(type) == result

        where:
        type                           || result
        Beans.StringPropertyBean       || false
        Builders.StringPropertyBuilder || false
        Records.StringPropertyRecord   || true
    }

    def "inspect a record type containing a string property"() {
        expect:
        inspector.inspect Records.StringPropertyRecord, Predicate, resolver(String), handler(), { bldr ->
            assert bldr.annotation.value() == Constants.PREDICATE_VALUE
            assert bldr.name == "value"
            assert bldr.type == String
            assert bldr.intrinsicType == String
            // TODO assert accessor value
        }
    }
}
