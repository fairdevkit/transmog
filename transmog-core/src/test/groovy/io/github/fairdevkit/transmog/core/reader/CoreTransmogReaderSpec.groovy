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
package io.github.fairdevkit.transmog.core.reader

import io.github.fairdevkit.transmog.core.SpiTransmogModule
import io.github.fairdevkit.transmog.core.analyzer.CoreTransmogAnalyzer
import io.github.fairdevkit.transmog.spi.BaseTransmogModule
import io.github.fairdevkit.transmog.spi.reader.StringValueConverter
import io.github.fairdevkit.transmog.spi.reader.TransmogReaderException
import io.github.fairdevkit.transmog.test.Beans
import io.github.fairdevkit.transmog.test.Constants
import io.github.fairdevkit.transmog.test.Nesting
import io.github.fairdevkit.transmog.test.Records
import io.github.fairdevkit.transmog.test.SemanticTypes
import io.github.fairdevkit.transmog.test.Subjects
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.vocabulary.RDF
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import java.util.function.Function

class CoreTransmogReaderSpec extends Specification {
    @Shared analyzer = new CoreTransmogAnalyzer()

    /** System under test */
    def reader = new CoreTransmogReader(analyzer)

    // convenience closures
    def read = { source, clazz, subject = Constants.NS ->
        reader.read new ByteArrayInputStream(source.bytes), clazz, subject
    }
    def triples = { ...items ->
        items.collate(3)
            .collect {
                if (it.last() in Collection) {
                    it[2] = it[2].join(", ")
                }
                it.join(" ")
            }
            .join("") + " ."
    }
    def iri = { "<$it>" }
    def literal = { "\"$it\"" }
    def langliteral = { literal, lang = "en" -> "\"${literal}\"@$lang" }

    def setupSpec() {
        new SpiTransmogModule().setup(analyzer)

        def module = new BaseTransmogModule()
        module.addValueConverter(String, Value.&stringValue)

        // module.addValueConverter(boolean, Boolean.&parseBoolean as StringValueConverter)
        module.addValueConverter(boolean, { Boolean.parseBoolean(it.stringValue()) })

        module.setup(analyzer)
    }

    def "read an invalid rdf source"() {
        when:
        read "this is not valid", Beans.StringPropertyBean

        then:
        def ex = thrown TransmogReaderException
        ex.message == "Could not read source RDF data"
    }

    def "read a missing property"() {
        given:
        def source = triples iri(Constants.NS), "a", iri(Constants.TYPE_EXAMPLE)

        when:
        read source, Beans.StringPropertyBean

        then:
        def ex = thrown TransmogReaderException
        ex.message == "Could not find required property ${Constants.PREDICATE_VALUE}"
    }

    def "read a readonly property"() {
        given:
        def source = triples iri(Constants.NS), iri(Constants.PREDICATE_VALUE), literal(Constants.LITERAL_FOO)

        when:
        read source, Beans.ReadonlyStringPropertyBean

        then:
        def ex = thrown TransmogReaderException
        ex.message == "Could not set a value for readonly property ${Constants.PREDICATE_VALUE}"
    }

    def "read a bean with a string property"() {
        given:
        def source = triples iri(Constants.NS), iri(Constants.PREDICATE_VALUE), literal(Constants.LITERAL_FOO)

        when:
        def bean = read source, Beans.StringPropertyBean

        then:
        with (bean.get()) {
            value == Constants.LITERAL_FOO
        }
    }

    def "read a bean with a boolean property"() {
        given:
        def source = triples iri(Constants.NS), iri(Constants.PREDICATE_VALUE), true

        when:
        def bean = read source, Beans.BooleanPropertyBean

        then:
        with (bean.get()) {
            value == true
        }
    }

    def "read a bean with a string array property"() {
        given:
        def source = triples iri(Constants.NS), iri(Constants.PREDICATE_VALUE), [literal(Constants.LITERAL_FOO), literal(Constants.LITERAL_BAR)]

        when:
        def bean = read source, Beans.StringArrayPropertyBean

        then:
        with (bean.get()) {
            value == [ Constants.LITERAL_FOO, Constants.LITERAL_BAR ]
        }
    }

    def "read a bean with a string typed collection"() {
        given:
        def source = triples iri(Constants.NS), iri(Constants.PREDICATE_VALUE), [literal(Constants.LITERAL_FOO), literal(Constants.LITERAL_BAR)]

        when:
        def bean = read source, Beans.StringCollectionPropertyBean

        then:
        with (bean.get()) {
            value == [ Constants.LITERAL_FOO, Constants.LITERAL_BAR ]
        }
    }

    def "read a record with a string types property"() {
        given:
        def source = triples iri(Constants.NS), iri(Constants.PREDICATE_VALUE), literal(Constants.LITERAL_FOO)

        when:
        def record = read source, Records.StringPropertyRecord

        then:
        with (record.get()) {
            value == Constants.LITERAL_FOO
        }
    }

    def "read a parent containing a child bean"() {
        given:
        def source = """\
                @prefix ${Constants.PREFIX}: <${Constants.NS}> .

                ex:1 ex:child ex:2 .
                ex:2 ex:value "${Constants.LITERAL_FOO}" .
                """.stripIndent()

        when:
        def parent = read source, Nesting.Parent, "http://example.com/1"

        then:
        with (parent.get()) {
            with (child) {
                it.value == Constants.LITERAL_FOO
            }
        }
    }

    def "read a self-referencing node bean"() {
        given:
        def source = """\
                @prefix ${Constants.PREFIX}: <${Constants.NS}> .
                
                ex:1 ex:node ex:2 .
                ex:2 ex:node ex:3 .
                ex:3 ex:node ex:4 .
                ex:4 ex:value "${Constants.LITERAL_FOO}" .
                """.stripIndent()

        when:
        def node = read source, Nesting.Node, "http://example.com/1"

        then:
        def ex1 = node.get()
        def ex2 = ex1.node
        def ex3 = ex2.node
        def ex4 = ex3.node
        ex4.value == Constants.LITERAL_FOO
    }

    def "confirm an absolute subject property is set"() {
        given:
        def source = triples iri(Constants.SUBJECT_1), iri(Constants.PREDICATE_VALUE), literal(Constants.LITERAL_FOO)

        when:
        def bean = read source, Subjects.PropertyAbsoluteSubject, Constants.SUBJECT_1

        then:
        with (bean.get()) {
            subject == Constants.SUBJECT_1
            value == Constants.LITERAL_FOO
        }
    }

    def "confirm a single semantic type is set for the annotated property"() {
        given:
        def source = triples iri(Constants.SUBJECT_1), "a", iri(Constants.TYPE_EXAMPLE)
        
        when:
        def bean = read source, SemanticTypes.PropertySemanticType, Constants.SUBJECT_1

        then:
        with (bean.get()) {
            type == Constants.TYPE_EXAMPLE
        }
    }

    def "confirm multiple semantic types are set for the annotated property"() {
        given:
        def source = triples iri(Constants.SUBJECT_1), "a", [ iri(Constants.TYPE_EXAMPLE), iri(Constants.TYPE_OTHER) ]

        when:
        def bean = read source, SemanticTypes.PropertyMultipleSemanticTypes, Constants.SUBJECT_1

        then:
        with (bean.get()) {
            types == [ Constants.TYPE_OTHER, Constants.TYPE_EXAMPLE ] // TODO: different order than expected
        }
    }
}
