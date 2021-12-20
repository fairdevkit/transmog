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
package io.github.fairdevkit.transmog.core.writer

import io.github.fairdevkit.transmog.core.SpiTransmogModule
import io.github.fairdevkit.transmog.core.analyzer.CoreTransmogAnalyzer
import io.github.fairdevkit.transmog.spi.BaseTransmogModule
import io.github.fairdevkit.transmog.test.*
import org.eclipse.rdf4j.model.Value
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

class CoreTransmogWriterSpec extends Specification {
    @Shared analyzer = new CoreTransmogAnalyzer()

    /** System under test */
    def writer = new CoreTransmogWriter(analyzer)

    // convenience closures
    def write = { source, subject = Constants.NS ->
        def sink = new ByteArrayOutputStream()
        writer.write(source, sink, subject)
        return sink.toString()
    }

    def setupSpec() {
        new SpiTransmogModule().setup(analyzer)

        def module = new BaseTransmogModule()
        module.addValueConverter(String, Value.&stringValue)
        module.addValueConverter(boolean, { Boolean.parseBoolean(it.stringValue()) } )
        module.setup(analyzer)
    }

    def "write a string property bean without namespaces"() {
        given:
        def source = ["value": Constants.LITERAL_FOO] as Beans.StringPropertyBean

        when:
        def rdf = write source

        then:
        rdf == """\
                
                <${Constants.NS}> <${Constants.PREDICATE_VALUE}> "${Constants.LITERAL_FOO}" .
                """.stripIndent()
    }

    def "write a string property bean with namespaces registered"() {
        given:
        def source = [value: Constants.LITERAL_FOO] as Beans.StringPropertyBean

        when:
        writer.registerNamespace(Constants.NAMESPACE)
        and:
        def rdf = write source, "http://example.com/1"

        then:
        rdf == """\
                @prefix ex: <http://example.com/> .
                
                ex:1 ex:value "${Constants.LITERAL_FOO}" .
                """.stripIndent()
    }

    def "write a string and boolean properties bean"() {
        given:
        def source = [value: Constants.LITERAL_FOO, flag: true] as Beans.StringAndBooleanPropertiesBean
        and:
        writer.registerNamespace(Constants.NAMESPACE)

        when:
        def rdf = write source, "http://example.com/1"

        then:
        rdf == """\
                @prefix ${Constants.PREFIX}: <${Constants.NS}> .
                
                ex:1 ex:value "${Constants.LITERAL_FOO}";
                  ex:flag true .
                """.stripIndent()
    }

    def "write an absent optional string property bean"() {
        given:
        def source = new Beans.OptionalStringPropertyBean()

        when:
        def rdf = write source, "http://example.com/1"

        then:
        rdf == ""
    }

    def "write a value-containing optional string property bean"() {
        given:
        def source = [value: Constants.LITERAL_FOO] as Beans.OptionalStringPropertyBean
        and:
        writer.registerNamespace(Constants.NAMESPACE)

        when:
        def rdf = write source, "http://example.com/1"

        then:
        rdf == """\
                @prefix ${Constants.PREFIX}: <${Constants.NS}> .
                
                ex:1 ex:value "${Constants.LITERAL_FOO}" .
                """.stripIndent()
    }

    def "write a parent containing a child bean"() {
        given:
        def parent = new Nesting.Parent()
        parent.child = [value: Constants.LITERAL_FOO] as Nesting.Child
        and:
        writer.registerNamespace(Constants.NAMESPACE)

        when:
        def rdf = write parent, "http://example.com/1"

        then:
        rdf == """\
                @prefix ${Constants.PREFIX}: <${Constants.NS}> .
                
                ex:1 ex:child <http://example.com/1#child> .
                
                <http://example.com/1#child> ex:value "${Constants.LITERAL_FOO}" .
                """.stripIndent()
    }

    def "write a self-referencing node bean"() {
        given:
        def node = [
            node: [
                subject: "http://example.com/2",
                node: [
                    subject: "http://example.com/3",
                    value: Constants.LITERAL_FOO
                ] as Nesting.Node
            ] as Nesting.Node
        ] as Nesting.Node
        and:
        writer.registerNamespace(Constants.NAMESPACE)

        when:
        def rdf = write node, "http://example.com/1"

        then:
        rdf == """\
                @prefix ${Constants.PREFIX}: <${Constants.NS}> .
                
                ex:1 ex:node ex:2 .
                
                ex:2 ex:node ex:3 .
                
                ex:3 ex:value "${Constants.LITERAL_FOO}" .
                """.stripIndent()
    }

    def "write property level provided subjects"() {
        given:
        bean.subject = subjectPropertyValue
        bean.value = Constants.LITERAL_FOO

        when:
        def rdf = write bean, subject

        then:
        rdf == """\
                
                <$subject> <${Constants.PREDICATE_VALUE}> "${Constants.LITERAL_FOO}" .
                """.stripIndent()

        where:
        bean                                                  | subjectPropertyValue || subject
        new Subjects.PropertyAbsoluteSubject()                | Constants.SUBJECT_1  || Constants.SUBJECT_1
        new Subjects.PropertyRelativeSubject()                | "foo"                || Constants.SUBJECT_1 + "#foo"
        new Subjects.PropertyRelativeSubjectCustomSeparator() | "foo"                || Constants.SUBJECT_1 + ":foo"
    }

    def "write type level provided semantic types"() {
        given:
        writer.registerNamespace(Constants.NAMESPACE)

        when:
        def rdf = write bean, Constants.SUBJECT_1

        then:
        rdf == """\
                @prefix ${Constants.PREFIX}: <${Constants.NS}> .
                
                ex:1 a $type .
                """.stripIndent()

        where:
        bean                                                                                                   || type
        new SemanticTypes.TypeSemanticType()                                                                   || "ex:Example"
        new SemanticTypes.TypeMultipleSemanticTypes()                                                          || "ex:Example, ex:Other"
    }

    def "write property level provided semantic types"() {
        given:
        bean."set${property.capitalize()}"(*values)
        and:
        writer.registerNamespace(Constants.NAMESPACE)

        when:
        def rdf = write bean, Constants.SUBJECT_1

        then:
        rdf == """\
                @prefix ${Constants.PREFIX}: <${Constants.NS}> .
                
                ex:1 a $writtenTypes .
                """.stripIndent()

        where:
        bean                                              | property | values                                           || writtenTypes
        new SemanticTypes.PropertySemanticType()          | "type"   | [ Constants.TYPE_EXAMPLE ]                       || "ex:Example"
        new SemanticTypes.PropertyMultipleSemanticTypes() | "types"  | [ Constants.TYPE_EXAMPLE, Constants.TYPE_OTHER ] || "ex:Example, ex:Other"
    }
}
