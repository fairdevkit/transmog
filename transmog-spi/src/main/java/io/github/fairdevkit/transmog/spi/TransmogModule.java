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
package io.github.fairdevkit.transmog.spi;

import io.github.fairdevkit.transmog.spi.analyzer.IntrinsicTypeResolver;
import io.github.fairdevkit.transmog.spi.analyzer.TypeInspector;
import io.github.fairdevkit.transmog.spi.reader.ArgumentStrategy;
import io.github.fairdevkit.transmog.spi.reader.InstanceStrategy;
import io.github.fairdevkit.transmog.spi.reader.ValueConverter;
import io.github.fairdevkit.transmog.spi.writer.WrapperHandler;

public interface TransmogModule {
    void setup(Context context);

    interface Context {
        default void registerInstanceStrategy(InstanceStrategy.Factory factory) {
        }

        default void registerTypeInspector(TypeInspector inspector) {
        }

        default void registerIntrinsicTypeResolver(IntrinsicTypeResolver<?> resolver) {
        }

        default void registerArgumentStrategy(ArgumentStrategy.Factory factory) {
        }

        default void registerValueConverter(Class<?> target, ValueConverter<?> converter) {
        }

        default void registerWrapperHandler(WrapperHandler handler) {
        }
    }
}
