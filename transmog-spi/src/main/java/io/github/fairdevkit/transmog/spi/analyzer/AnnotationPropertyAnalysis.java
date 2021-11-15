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

import java.lang.annotation.Annotation;
import java.util.Objects;

public class AnnotationPropertyAnalysis<A extends Annotation> {
    private final A annotation;

    protected AnnotationPropertyAnalysis(A annotation) {
        this.annotation = Objects.requireNonNull(annotation, "Annotation analysis property 'annotation' cannot be null");
    }

    public A getAnnotation() {
        return this.annotation;
    }

    public static <A extends Annotation> Builder<A> builder() {
        return new Builder<>();
    }

    public static class Builder<A extends Annotation> {
        protected A annotation;

        protected Builder() {
        }

        public Builder<A> annotation(A annotation) {
            this.annotation = annotation;
            return this;
        }

        public AnnotationPropertyAnalysis<A> build() {
            return new AnnotationPropertyAnalysis<>(annotation);
        }
    }
}
