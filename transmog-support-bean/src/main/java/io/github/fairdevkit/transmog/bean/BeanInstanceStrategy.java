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
package io.github.fairdevkit.transmog.bean;

import io.github.fairdevkit.transmog.spi.analyzer.FieldPropertyAnalysis;
import io.github.fairdevkit.transmog.spi.reader.InstanceStrategy;
import io.github.fairdevkit.transmog.spi.reader.TransmogReaderException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

@NotThreadSafe
public class BeanInstanceStrategy<T> implements InstanceStrategy<T> {
    private static final Map<Class<?>, MethodHandle> cache = new ConcurrentHashMap<>();

    private final T instance;

    /* package-private */ BeanInstanceStrategy(Class<T> type) {
        var constructor = cache.computeIfAbsent(type, clazz -> {
            try {
                var ctor = clazz.getConstructor();

                return MethodHandles.publicLookup().unreflectConstructor(ctor);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new TransmogReaderException("Could not find bean constructor for " + clazz, e);
            }
        });

        try {
            instance = (T)constructor.invoke();
        } catch (Throwable t) {
            throw new TransmogReaderException("Could not invoke bean constructor for " + type, t);
        }
    }

    @Override
    public void add(FieldPropertyAnalysis<?> property, Object value) {
        if (property instanceof BeanPropertyAnalysis<?> beanProperty) {
            var mutator = beanProperty.getMutator();

            try {
                mutator.invoke(instance, value);
            } catch (Throwable t) {
                throw new TransmogReaderException("Could not invoke bean mutator method for property " + property.getName(), t);
            }
        } else {
            throw new TransmogReaderException("Could not find bean mutator method for property " + property.getName());
        }
    }

    @Override
    public T create() {
        return instance;
    }

    @ThreadSafe
    public static class Factory extends BeanSupporting implements InstanceStrategy.Factory {
        @Override
        public <T> InstanceStrategy<T> create(Class<T> type) {
            return new BeanInstanceStrategy<>(type);
        }
    }
}
