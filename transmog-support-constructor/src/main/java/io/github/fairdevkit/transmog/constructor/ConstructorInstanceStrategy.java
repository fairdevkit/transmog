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
package io.github.fairdevkit.transmog.constructor;

import io.github.fairdevkit.transmog.spi.analyzer.FieldPropertyAnalysis;
import io.github.fairdevkit.transmog.spi.reader.InstanceStrategy;
import io.github.fairdevkit.transmog.spi.reader.TransmogReaderException;
import java.beans.ConstructorProperties;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

@NotThreadSafe
public class ConstructorInstanceStrategy<T> implements InstanceStrategy<T> {
    private static final Map<Class<?>, Tuple> handleCache = new ConcurrentHashMap<>();

    private final MethodHandle handle;
    private final Object[] arguments;

    /* package-private */ ConstructorInstanceStrategy(Class<T> type) {
        var tuple = handleCache.computeIfAbsent(type, key -> {
            return Arrays.stream(key.getDeclaredConstructors())
                    .filter(ctor -> ctor.isAnnotationPresent(ConstructorProperties.class))
                    .findFirst()
                    .map(ctor -> {
                        var annotation = ctor.getAnnotation(ConstructorProperties.class);
                        var length = annotation.value().length;

                        // confirm attribute count matches parameter count
                        if (ctor.getParameterCount() != length) {
                            throw new TransmogReaderException("@ConstructorProperties attributes do not match the " +
                                    "parameter count on " + ctor);
                        }

                        try {
                            return new Tuple(MethodHandles.publicLookup().unreflectConstructor(ctor), length);
                        } catch (IllegalAccessException e) {
                            throw new TransmogReaderException("", e);
                        }
                    }).orElseThrow(ex("Could not find a @ConstructorProperties annotated constructor"));
        });

        handle = tuple.handle;
        arguments = new Object[tuple.length];
    }

    @Override
    public void add(FieldPropertyAnalysis<?> property, Object value) {
        if (property instanceof ConstructorPropertyAnalysis constructorProperty) {
            var index = constructorProperty.getIndex();

            arguments[index] = value;
        } else {
            // TODO
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T create() {
        try {
            return (T)handle.invokeWithArguments(arguments);
        } catch (Throwable t) {
            throw new TransmogReaderException("", t);
        }
    }

    private static Supplier<TransmogReaderException> ex(String message) {
        return () -> new TransmogReaderException(message);
    }

    private record Tuple(MethodHandle handle, int length) {
    }

    @ThreadSafe
    public static class Factory implements InstanceStrategy.Factory {
        @Override
        public boolean supports(Class<?> type) {
            return Arrays.stream(type.getDeclaredConstructors())
                    .anyMatch(constructor -> constructor.isAnnotationPresent(ConstructorProperties.class));
        }

        @Override
        public <T> InstanceStrategy<T> create(Class<T> type) {
            return new ConstructorInstanceStrategy<>(type);
        }
    }
}
