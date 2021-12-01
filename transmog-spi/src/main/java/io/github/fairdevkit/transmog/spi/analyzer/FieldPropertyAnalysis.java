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
package io.github.fairdevkit.transmog.spi.analyzer;

import io.github.fairdevkit.transmog.spi.reader.ArgumentStrategy;
import io.github.fairdevkit.transmog.spi.reader.ValueConverter;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class FieldPropertyAnalysis<A extends Annotation> extends AnnotationPropertyAnalysis<A> {
    private final String name;
    private final Class<?> type;
    private final Class<?> intrinsicType;
    private final MethodHandle accessor;
    private final ArgumentStrategy.Factory factory;
    private final ValueConverter<?> valueConverter;
    private final boolean nested;

    protected FieldPropertyAnalysis(A annotation, String name, Class<?> type, Class<?> intrinsicType, MethodHandle accessor,
            ArgumentStrategy.Factory factory, ValueConverter<?> valueConverter, boolean nested) {
        super(annotation);
        this.name = Objects.requireNonNull(name, errMsg("name"));
        this.type = Objects.requireNonNull(type, errMsg("type"));
        this.intrinsicType = Objects.requireNonNull(intrinsicType, errMsg("intrinsicType"));
        this.accessor = Objects.requireNonNull(accessor, errMsg("accessor"));
        this.factory = Objects.requireNonNull(factory, errMsg("factory"));
        this.valueConverter = Objects.requireNonNull(valueConverter, errMsg("valueConverter"));
        this.nested = nested;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public Class<?> getIntrinsicType() {
        return intrinsicType;
    }

    public MethodHandle getAccessor() {
        return accessor;
    }

    public ArgumentStrategy.Factory getFactory() {
        return factory;
    }

    public ValueConverter<?> getValueConverter() {
        return valueConverter;
    }

    public boolean isNested() {
        return nested;
    }

    private static Supplier<String> errMsg(String prop) {
        return () -> String.format("Field analysis property '%s' cannot be null", prop);
    }

    public static abstract class Builder<A extends Annotation> extends AnnotationPropertyAnalysis.Builder<A> {
        protected String name;
        protected Class<?> type;
        protected Class<?> intrinsicType;
        protected MethodHandle accessor;
        protected ArgumentStrategy.Factory factory;
        protected ValueConverter<?> valueConverter;
        protected boolean nested;

        protected Builder() {
        }

        public Builder<A> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<A> type(Class<?> type) {
            this.type = type;
            return this;
        }

        public Builder<A> intrinsicType(Class<?> intrinsicType) {
            this.intrinsicType = intrinsicType;
            return this;
        }

        public Builder<A> accessor(MethodHandle accessor) {
            this.accessor = accessor;
            return this;
        }

        public Builder<A> factory(ArgumentStrategy.Factory factory) {
            this.factory = factory;
            return this;
        }

        public Builder<A> valueConverter(ValueConverter<?> valueConverter) {
            this.valueConverter = valueConverter;
            return this;
        }

        public Builder<A> nested(boolean nested) {
            this.nested = nested;
            return this;
        }

        public Optional<Class<?>> peekType() {
            return Optional.ofNullable(type);
        }

        public Optional<Class<?>> peekIntrinsicType() {
            return Optional.ofNullable(intrinsicType);
        }

        public abstract FieldPropertyAnalysis<A> build();
    }
}
