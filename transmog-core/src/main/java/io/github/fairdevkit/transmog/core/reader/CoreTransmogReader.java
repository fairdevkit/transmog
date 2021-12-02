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
package io.github.fairdevkit.transmog.core.reader;

import io.github.fairdevkit.transmog.api.reader.TransmogReader;
import io.github.fairdevkit.transmog.spi.analyzer.FieldPropertyAnalysis;
import io.github.fairdevkit.transmog.api.analyzer.TransmogAnalyzer;
import io.github.fairdevkit.transmog.spi.reader.TransmogReaderException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreTransmogReader implements TransmogReader<InputStream> {
    private static final Logger logger = LoggerFactory.getLogger(CoreTransmogReader.class);
    private final TransmogAnalyzer analyzer;

    public CoreTransmogReader(TransmogAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    @Override
    public <T> Optional<T> read(InputStream source, Class<T> clazz, CharSequence subject) {
        final Model model;
        try {
            model = Rio.parse(source, RDFFormat.TURTLE);
        } catch (IOException | RDFParseException e) {
            throw new TransmogReaderException("Could not read source RDF data", e);
        }

        var iri = Values.iri(subject.toString());

        return readInternal(model, clazz, iri);
    }

    private <T> Optional<T> readInternal(Model model, Class<T> clazz, Resource subject) {
        if (!model.contains(subject, null, null)) {
            return Optional.empty();
        }

        var analysis = analyzer.analyze(clazz);
        var factory = analysis.factory();
        var strategy = factory.create(clazz);

        for (var property : analysis.predicates()) {
            var annotation = property.getAnnotation();
            var predicate = Values.iri(annotation.value());
            var objects = Models.getProperties(model, subject, predicate);

            if (objects.isEmpty()) {
                if (!annotation.readonly() && annotation.required()) {
                    throw new TransmogReaderException("Could not find required property " + annotation.value());
                } else {
                    continue;
                }
            } else if (annotation.readonly()) {
                throw new TransmogReaderException("Could not set a value for readonly property " + predicate);
            }

            var argument = readArgument(property, objects, model);
            strategy.add(property, argument);
        }

        return Optional.of(strategy.create());
    }

    private Object readArgument(FieldPropertyAnalysis<?> property, Set<? extends Value> objects, Model model) {
        var factory = property.getFactory();
        var strategy = factory.create(property, objects.size());

        for (var object : objects) {
            if (property.isNested()) {
                var type = property.getIntrinsicType();

                readInternal(model, type, (Resource)object).ifPresent(strategy::add);
            } else {
                var converter = property.getValueConverter();
                var value = converter.convert(object);
                strategy.add(value);
            }
        }

        return strategy.create();
    }
}
