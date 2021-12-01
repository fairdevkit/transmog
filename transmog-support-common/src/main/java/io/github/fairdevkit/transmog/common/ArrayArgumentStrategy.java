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
package io.github.fairdevkit.transmog.common;

import io.github.fairdevkit.transmog.spi.analyzer.FieldPropertyAnalysis;
import io.github.fairdevkit.transmog.spi.reader.ArgumentStrategy;
import io.github.fairdevkit.transmog.spi.reader.TransmogReaderException;
import java.lang.reflect.Array;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

@NotThreadSafe
public class ArrayArgumentStrategy implements ArgumentStrategy<Object[]> {
    private final Object[] array;
    private final int targetSize;
    private int index;

    /* package-private */ ArrayArgumentStrategy(Class<?> componentType, int size) {
        array = (Object[])Array.newInstance(componentType, size);
        targetSize = size;
    }

    @Override
    public void add(Object value) {
        if (index >= array.length) {
            throw new TransmogReaderException("Could not add more values to the array than the allocated size");
        }

        array[index] = value;

        index++;
    }

    @Override
    public Object[] create() {
        if (index != targetSize) {
            throw new TransmogReaderException("Array was not populated up to its allocated size");
        }

        return array;
    }

    @ThreadSafe
    public static class Factory implements ArgumentStrategy.Factory {
        @Override
        public boolean supports(Class<?> type) {
            return type.isArray();
        }

        @Override
        public ArgumentStrategy<?> create(FieldPropertyAnalysis<?> property, int size) {
            return new ArrayArgumentStrategy(property.getIntrinsicType(), size);
        }
    }
}
