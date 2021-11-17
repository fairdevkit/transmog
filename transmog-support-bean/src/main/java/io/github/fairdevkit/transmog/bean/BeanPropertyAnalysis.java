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
import io.github.fairdevkit.transmog.spi.reader.ArgumentStrategy;
import io.github.fairdevkit.transmog.spi.reader.ValueConverter;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.util.Objects;

public class BeanPropertyAnalysis<A extends Annotation> extends FieldPropertyAnalysis<A> {
    private final MethodHandle mutator;

    private BeanPropertyAnalysis(A annotation, String name, Class<?> type, MethodHandle accessor,
            ArgumentStrategy.Factory factory, ValueConverter<?> valueConverter, boolean nested, MethodHandle mutator) {
        super(annotation, name, type, accessor, factory, valueConverter, nested);
        this.mutator = Objects.requireNonNull(mutator, "Bean analysis property 'mutator' cannot be null");
    }

    public MethodHandle getMutator() {
        return mutator;
    }

    public static <A extends Annotation> Builder<A> builder() {
        return new Builder<>();
    }

    public static class Builder<A extends Annotation> extends FieldPropertyAnalysis.Builder<A> {
        private MethodHandle mutator;

        private Builder() {
        }

        public Builder<A> mutator(MethodHandle mutator) {
            this.mutator = mutator;
            return this;
        }

        public BeanPropertyAnalysis<A> build() {
            return new BeanPropertyAnalysis<>(annotation, name, type, accessor, factory, valueConverter, nested, mutator);
        }
    }
}
