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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BaseTransmogModule implements TransmogModule {
    private final Collection<InstanceStrategy.Factory> instanceStrategies;
    private final Collection<TypeInspector> typeInspectors;
    private final Collection<IntrinsicTypeResolver<?>> intrinsicTypeResolvers;
    private final Collection<ArgumentStrategy.Factory> argumentStrategies;
    private final Map<Class<?>, ValueConverter<?>> valueConverters;

    public BaseTransmogModule() {
        instanceStrategies = new ArrayList<>();
        typeInspectors = new ArrayList<>();
        intrinsicTypeResolvers = new ArrayList<>();
        argumentStrategies = new ArrayList<>();
        valueConverters = new HashMap<>();
    }

    public void addInstanceStrategy(InstanceStrategy.Factory factory) {
        instanceStrategies.add(factory);
    }

    public void addTypeInspector(TypeInspector inspector) {
        typeInspectors.add(inspector);
    }

    public void addIntrinsicTypeResolver(IntrinsicTypeResolver<?> resolver) {
        intrinsicTypeResolvers.add(resolver);
    }

    public void addArgumentStrategy(ArgumentStrategy.Factory factory) {
        argumentStrategies.add(factory);
    }

    public <T> void addValueConverter(Class<T> type, ValueConverter<T> converter) {
        valueConverters.put(type, converter);
    }

    @Override
    public void setup(Context context) {
        instanceStrategies.forEach(context::registerInstanceStrategy);
        typeInspectors.forEach(context::registerTypeInspector);
        intrinsicTypeResolvers.forEach(context::registerIntrinsicTypeResolver);
        argumentStrategies.forEach(context::registerArgumentStrategy);
        valueConverters.forEach(context::registerValueConverter);
    }
}
