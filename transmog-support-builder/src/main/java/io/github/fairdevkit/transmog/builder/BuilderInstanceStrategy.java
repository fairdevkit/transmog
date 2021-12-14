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
package io.github.fairdevkit.transmog.builder;

import io.github.fairdevkit.transmog.spi.analyzer.FieldPropertyAnalysis;
import io.github.fairdevkit.transmog.spi.reader.InstanceStrategy;
import io.github.fairdevkit.transmog.spi.reader.TransmogReaderException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

@NotThreadSafe
public class BuilderInstanceStrategy<T> implements InstanceStrategy<T> {
    public static final String BUILDER_FACTORY_METHOD_NAME = "builder";
    public static final String BUILDER_CLASS_NAME = "Builder";
    public static final String BUILDER_BUILD_METHOD_NAME = "build";

    private static final Map<Class<?>, MethodHandle> factoryMethodCache = new ConcurrentHashMap<>();
    private static final Map<Class<?>, MethodHandle> buildMethodCache = new ConcurrentHashMap<>();

    private final Object builderInstance;

    /* package-private */ BuilderInstanceStrategy(Class<T> type) {
        var factoryMethod = factoryMethodCache.computeIfAbsent(type, key -> {
            var builderType = Arrays.stream(key.getDeclaredClasses())
                    .filter(declared -> BUILDER_CLASS_NAME.equals(declared.getSimpleName()))
                    .findFirst()
                    .orElseThrow();//TODO

            try {
                return MethodHandles.publicLookup().findStatic(key, BUILDER_FACTORY_METHOD_NAME, MethodType.methodType(builderType));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new TransmogReaderException("", e);
            }
        });

        try {
            builderInstance = factoryMethod.invoke();
        } catch (Throwable t) {
            throw new TransmogReaderException("", t);//TODO
        }
    }

    @Override
    public void add(FieldPropertyAnalysis<?> property, Object value) {
        if (property instanceof BuilderPropertyAnalysis<?> builderProperty) {
            var prop = builderProperty.getBuilderProperty();

            try {
                prop.invoke(builderInstance, value);
            } catch (Throwable t) {
                throw new TransmogReaderException("", t);
            }
        } else {
            throw new TransmogReaderException("");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T create() {
        var buildMethod = buildMethodCache.computeIfAbsent(builderInstance.getClass(), key -> {
            var returnType = MethodType.methodType(key.getEnclosingClass());

            try {
                return MethodHandles.publicLookup().findVirtual(key, BUILDER_BUILD_METHOD_NAME, returnType);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new TransmogReaderException("", e);//TODO
            }
        });

        try {
            return (T)buildMethod.invoke(builderInstance);
        } catch (Throwable t) {
            throw new TransmogReaderException("", t);//TODO
        }
    }

    @ThreadSafe
    public static class Factory implements InstanceStrategy.Factory {
        @Override
        public boolean supports(Class<?> type) {
            try {
                var factoryMethod = type.getDeclaredMethod(BUILDER_FACTORY_METHOD_NAME);

                return Modifier.isStatic(factoryMethod.getModifiers());
            } catch (NoSuchMethodException e) {
                return false;
            }
        }

        @Override
        public <T> InstanceStrategy<T> create(Class<T> type) {
            return new BuilderInstanceStrategy<>(type);
        }
    }
}
