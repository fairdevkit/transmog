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
package io.github.fairdevkit.transmog.record;

import io.github.fairdevkit.transmog.spi.analyzer.FieldPropertyAnalysis;
import io.github.fairdevkit.transmog.spi.reader.InstanceStrategy;
import io.github.fairdevkit.transmog.spi.reader.TransmogReaderException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

@NotThreadSafe
public class RecordInstanceStrategy<T> implements InstanceStrategy<T> {
    private static final Map<Class<?>, MethodHandle> cache = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, Integer>> parameterCache = new ConcurrentHashMap<>();

    private final MethodHandle constructor;
    private final Map<String, Integer> parameterIndices;
    private final Object[] arguments;

    /* package-private */ RecordInstanceStrategy(Class<T> recordType) {
        constructor = cache.computeIfAbsent(recordType, type -> {
            try {
                var parameterTypes = Arrays.stream(type.getRecordComponents())
                        .map(RecordComponent::getType)
                        .toArray(Class<?>[]::new);
                var ctor = type.getDeclaredConstructor(parameterTypes);

                return MethodHandles.publicLookup().unreflectConstructor(ctor);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new TransmogReaderException("Could not find record constructor for " + type);
            }
        });

        parameterIndices = parameterCache.computeIfAbsent(recordType, key -> {
            var components = key.getRecordComponents();
            var mapping = new HashMap<String, Integer>();

            for (int i = 0; i < components.length; i++) {
                var name = components[i].getName();

                mapping.put(name, i);
            }
            return Collections.unmodifiableMap(mapping);
        });

        arguments = new Object[parameterIndices.size()];
    }

    @Override
    public void add(FieldPropertyAnalysis<?> property, Object value) {
        var index = Optional.ofNullable(parameterIndices.get(property.getName()))
                .orElseThrow(() -> new TransmogReaderException("Could not recognise record property " + property.getName()));

        arguments[index] = value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T create() {
        try {
            return (T)constructor.invokeWithArguments(arguments);
        } catch (Throwable t) {
            throw new TransmogReaderException("", t);
        }
    }

    @ThreadSafe
    public static class Factory implements InstanceStrategy.Factory {
        @Override
        public boolean supports(Class<?> type) {
            return type.isRecord();
        }

        @Override
        public <T> InstanceStrategy<T> create(Class<T> type) {
            return new RecordInstanceStrategy<>(type);
        }
    }
}
