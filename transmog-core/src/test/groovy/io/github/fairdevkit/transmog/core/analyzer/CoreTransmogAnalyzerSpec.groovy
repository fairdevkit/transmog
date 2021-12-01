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
package io.github.fairdevkit.transmog.core.analyzer

import io.github.fairdevkit.transmog.bean.BeanInstanceStrategy
import io.github.fairdevkit.transmog.common.SingleArgumentStrategy
import io.github.fairdevkit.transmog.core.SpiTransmogModule
import io.github.fairdevkit.transmog.spi.BaseTransmogModule
import io.github.fairdevkit.transmog.spi.reader.StringValueConverter
import io.github.fairdevkit.transmog.test.Beans
import io.github.fairdevkit.transmog.test.Constants
import org.eclipse.rdf4j.model.Value
import spock.lang.Specification

class CoreTransmogAnalyzerSpec extends Specification {
    /** System under test */
    def analyzer = new CoreTransmogAnalyzer()

    // convenience closures
    def analyze = { analyzer.analyze it }
    def setup = { ...modules -> modules.each { it.setup analyzer } }

    def "analyze a bean class"() {
        given:
        def m = new BaseTransmogModule()
        m.addValueConverter(String, Value.&stringValue as StringValueConverter)
        setup new SpiTransmogModule(), m

        when:
        def analysis = analyze Beans.StringPropertyBean

        then:
        with (analysis) {
            factory() instanceof BeanInstanceStrategy.Factory
            types().isEmpty()
            subject() == null
            predicates().size() == 1
        }
        and:
        with (analysis.predicates().first()) {
            with (annotation) {
                value() == Constants.PREDICATE_VALUE
                literal() == true
            }
            name == "value"
            factory instanceof SingleArgumentStrategy.Factory
            type == String
            intrinsicType == String
            nested == false
        }
    }
}
