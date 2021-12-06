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
package io.github.fairdevkit.transmog.core;

import io.github.fairdevkit.transmog.api.TransmogConfig;
import io.github.fairdevkit.transmog.api.TransmogMapper;
import io.github.fairdevkit.transmog.api.analyzer.TransmogAnalyzer;
import io.github.fairdevkit.transmog.core.analyzer.CoreTransmogAnalyzer;
import io.github.fairdevkit.transmog.core.reader.CoreTransmogReader;
import io.github.fairdevkit.transmog.core.writer.CoreTransmogWriter;
import io.github.fairdevkit.transmog.spi.TransmogModule;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import org.eclipse.rdf4j.rio.RDFFormat;

public class CoreTransmogMapper implements TransmogMapper<InputStream, OutputStream> {
    private final TransmogAnalyzer analyzer;
    private final CoreTransmogReader reader;
    private final CoreTransmogWriter writer;
    private TransmogConfig config;

    public CoreTransmogMapper() {
        this(new CoreTransmogAnalyzer());
    }

    public CoreTransmogMapper(TransmogAnalyzer analyzer) {
        this.analyzer = analyzer;
        reader = new CoreTransmogReader(analyzer);
        writer = new CoreTransmogWriter(analyzer);

        configure(new TransmogConfig());
    }

    @Override
    public void addModule(TransmogModule module) {
        module.setup(analyzer);
        module.setup(reader);
        module.setup(writer);
    }

    @Override
    public void configure(TransmogConfig config) {
        this.config = config;

        analyzer.configure(config);
        reader.configure(config);
        writer.configure(config);
    }

    @Override
    public <T> Optional<T> read(InputStream source, Class<T> clazz, CharSequence subject) {
        return read(source, clazz, subject, config.get(CoreSettings.DEFAULT_READ_FORMAT));
    }

    public <T> Optional<T> read(InputStream source, Class<T> clazz, CharSequence subject, RDFFormat format) {
        return reader.read(source, clazz, subject, format);
    }

    @Override
    public void write(Object source, OutputStream sink, CharSequence subject) {
        write(source, sink, subject, config.get(CoreSettings.DEFAULT_WRITE_FORMAT));
    }

    public void write(Object source, OutputStream sink, CharSequence subject, RDFFormat format) {
        writer.write(source, sink, subject, format);
    }
}
