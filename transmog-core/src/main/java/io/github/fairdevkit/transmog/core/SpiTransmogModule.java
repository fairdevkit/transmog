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

import io.github.fairdevkit.transmog.spi.TransmogModule;
import io.github.fairdevkit.transmog.spi.analyzer.IntrinsicTypeResolver;
import io.github.fairdevkit.transmog.spi.analyzer.TypeInspector;
import io.github.fairdevkit.transmog.spi.reader.ArgumentStrategy;
import io.github.fairdevkit.transmog.spi.reader.InstanceStrategy;
import io.github.fairdevkit.transmog.spi.writer.WrapperHandler;
import java.util.ServiceLoader;

public class SpiTransmogModule implements TransmogModule {
    @Override
    public void setup(Context context) {
        ServiceLoader.load(InstanceStrategy.Factory.class)
                .forEach(context::registerInstanceStrategy);
        ServiceLoader.load(TypeInspector.class)
                .forEach(context::registerTypeInspector);
        ServiceLoader.load(IntrinsicTypeResolver.class)
                .forEach(context::registerIntrinsicTypeResolver);
        ServiceLoader.load(ArgumentStrategy.Factory.class)
                .forEach(context::registerArgumentStrategy);
        ServiceLoader.load(WrapperHandler.class)
                .forEach(context::registerWrapperHandler);
    }
}
