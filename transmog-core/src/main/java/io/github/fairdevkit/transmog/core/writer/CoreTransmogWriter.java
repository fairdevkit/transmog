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
package io.github.fairdevkit.transmog.core.writer;

import io.github.fairdevkit.transmog.annotations.Predicate;
import io.github.fairdevkit.transmog.annotations.SemanticType;
import io.github.fairdevkit.transmog.api.analyzer.TransmogAnalyzer;
import io.github.fairdevkit.transmog.api.writer.TransmogWriter;
import io.github.fairdevkit.transmog.core.CoreTransmogModule;
import io.github.fairdevkit.transmog.spi.analyzer.ClassAnalysis;
import io.github.fairdevkit.transmog.spi.analyzer.FieldPropertyAnalysis;
import io.github.fairdevkit.transmog.spi.writer.TransmogWriterException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreTransmogWriter implements TransmogWriter<OutputStream>, CoreTransmogModule.CoreContext {
    private static final Logger logger = LoggerFactory.getLogger(CoreTransmogWriter.class);

    private final TransmogAnalyzer analyzer;
    private final Collection<Namespace> namespaces;

    public CoreTransmogWriter(TransmogAnalyzer analyzer) {
        this.analyzer = analyzer;
        namespaces = new ArrayList<>();
    }

    @Override
    public void registerNamespace(Namespace ns) {
        namespaces.add(ns);
    }

    @Override
    public void write(Object source, OutputStream sink, CharSequence subject) {
        write(source, sink, subject, RDFFormat.TURTLE);
    }

    public void write(Object source, OutputStream sink, CharSequence subject, RDFFormat format) {
        var analysis = analyzer.analyze(source);
        var iri = Values.iri(subject.toString());

        var model = new LinkedHashModel();
        namespaces.forEach(model::setNamespace);

        writeInternal(model, source, analysis, iri);

        try {
            Rio.write(model, sink, format);
        } catch (RDFHandlerException e) {
            throw new TransmogWriterException("", e);//TODO
        }
    }

    private void writeInternal(Model model, Object instance, ClassAnalysis analysis, Resource subject) {
        for (var property : analysis.types()) {
            for (var type : property.getAnnotation().value()) {
                model.add(subject, RDF.TYPE, Values.iri(type));
            }

            if (property instanceof FieldPropertyAnalysis<SemanticType> fieldProperty) {
                var value = unwrap(fieldProperty, instance);

                value.ifPresent(content -> {
                    iterate(content, element -> {
                        model.add(subject, RDF.TYPE, Values.iri(element.toString()));
                    });
                });
            }
        }

        for (var property : analysis.predicates()) {
            var value = unwrap(property, instance);

            var annotation = property.getAnnotation();

            if (value.isEmpty()) {
                if (annotation.required()) {
                    throw new TransmogWriterException("");//TODO
                } else {
                    continue;
                }
            }

            var predicate = Values.iri(annotation.value());
            var content = value.orElseThrow();

            iterate(content, element -> {
                writeStatement(model, property, subject, predicate, element);
            });
        }
    }

    private void writeStatement(Model model, FieldPropertyAnalysis<Predicate> property, Resource subject, IRI predicate, Object object) {
        if (property.isNested()) {
            var analysis = analyzer.analyze(object);

            var iri = Optional.ofNullable(analysis.subject()).flatMap(subjectProperty -> {
                var annotation = subjectProperty.getAnnotation();

                final Object subjectValue;
                if (subjectProperty instanceof FieldPropertyAnalysis subjectFieldProperty) {
                    var handle = subjectFieldProperty.getAccessor();
                    try {
                        subjectValue = handle.invoke(object);
                    } catch (Throwable t) {
                        throw new TransmogWriterException("", t);
                    }
                } else {
                    var value = annotation.value();

                    if (value.isBlank()) {
                        throw new TransmogWriterException("");
                    }

                    if (annotation.relative()) {
                        subjectValue = new StringBuilder()
                                .append(subject)
                                .append(annotation.separator())
                                .append(value);
                    } else {
                        subjectValue = value;
                    }
                }

                if (annotation.blankNode()) {
                    return Optional.ofNullable(subjectValue)
                            .map(Object::toString)
                            .map(Values::bnode)
                            .or(() -> Optional.of(Values.bnode()));
                } else {
                    return Optional.ofNullable(subjectValue)
                            .map(Object::toString)
                            .map(Values::iri);
                }
            }).orElseThrow();//TODO

            model.add(subject, predicate, iri);

            try {
                writeInternal(model, object, analysis, iri);
            } catch (TransmogWriterException e) {
                throw new TransmogWriterException("", e);//TODO
            }

        } else if (property.getAnnotation().literal()) {
            var datatype = Values.iri(property.getAnnotation().datatype());
            var literal = Values.literal(object.toString(), datatype);

            model.add(subject, predicate, literal);
        } else {
            var iri = Values.iri(object.toString());

            model.add(subject, predicate, iri);
        }
    }

    private static Optional<?> unwrap(FieldPropertyAnalysis<?> property, Object instance) {
        final Optional<?> value;
        try {
            var accessor = property.getAccessor();
            var intermediate = accessor.invoke(instance);

            if (property.getWrapperHandler().isPresent()) {
                value = property.getWrapperHandler()
                        .flatMap(handler -> handler.handle(intermediate));
            } else {
                value = Optional.ofNullable(intermediate);
            }
        } catch (Throwable t) {
            throw new TransmogWriterException("", t);//TODO
        }

        return value;
    }

    private static void iterate(Object object, Consumer<Object> consumer) {
        if (object instanceof Iterable iterable) {
            for (var element : iterable) {
                consumer.accept(element);
            }
        } else if (object.getClass().isArray()) {
            for (var element : (Object[])object) {
                consumer.accept(element);
            }
        } else {
            consumer.accept(object);
        }
    }
}
