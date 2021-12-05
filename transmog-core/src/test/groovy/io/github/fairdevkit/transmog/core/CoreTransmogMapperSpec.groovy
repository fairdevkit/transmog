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
package io.github.fairdevkit.transmog.core

import io.github.fairdevkit.transmog.test.Beans
import io.github.fairdevkit.transmog.test.Constants
import org.eclipse.rdf4j.model.Value
import spock.lang.Specification

class CoreTransmogMapperSpec extends Specification {
    /** System under test */
    def mapper = new CoreTransmogMapper()

    def setup() {
        mapper.addModule(new SpiTransmogModule())

        def module = new CoreTransmogModule()
        module.addValueConverter(String, Value.&stringValue)
        module.addNamespace(Constants.NAMESPACE)
        mapper.addModule(module)
    }

    def "roundtrip bean to rdf to bean"() {
        given:
        def bean = [value: Constants.LITERAL_FOO] as Beans.StringPropertyBean
        def sink = new ByteArrayOutputStream()

        when:
        mapper.write(bean, sink, "http://example.com/1")
        and:
        def result = mapper.read(new ByteArrayInputStream(sink.toByteArray()), Beans.StringPropertyBean, "http://example.com/1")

        then:
        result.get() == bean
    }

    def "roundtrip rdf to bean to rdf"() {
        given:
        def rdf = """\
                @prefix ${Constants.PREFIX}: <${Constants.NS}> .
                
                ex:1 ex:value "${Constants.LITERAL_FOO}" .
                """.stripIndent()

        when:
        def bean = mapper.read(new ByteArrayInputStream(rdf.bytes), Beans.StringPropertyBean, "http://example.com/1")
        and:
        def sink = new ByteArrayOutputStream()
        mapper.write(bean.get(), sink, "http://example.com/1")

        then:
        sink.toString() == rdf
    }
}
