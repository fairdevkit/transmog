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

import io.github.fairdevkit.transmog.annotations.Predicate;
import io.github.fairdevkit.transmog.annotations.SemanticType;
import io.github.fairdevkit.transmog.annotations.Subject;
import io.github.fairdevkit.transmog.spi.reader.InstanceStrategy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nullable;

public record ClassAnalysis(
        @Nullable AnnotationPropertyAnalysis<Subject> subject,
        Collection<AnnotationPropertyAnalysis<SemanticType>> types,
        Collection<FieldPropertyAnalysis<Predicate>> predicates,
        @Nullable InstanceStrategy.Factory factory) {
    public static Builder builder() {
        return new Builder();
    }

    public static Builder copy(ClassAnalysis source) {
        return new Builder(source);
    }

    public static class Builder {
        private final Collection<AnnotationPropertyAnalysis<SemanticType>> types;
        private final Collection<FieldPropertyAnalysis<Predicate>> predicates;
        private AnnotationPropertyAnalysis<Subject> subject;
        private InstanceStrategy.Factory factory;

        private Builder() {
            types = new ArrayList<>(0);
            predicates = new ArrayList<>(0);
        }

        private Builder(ClassAnalysis source) {
            types = new ArrayList<>(source.types);
            predicates = new ArrayList<>(source.predicates);

            Optional.ofNullable(source.subject)
                    .ifPresent(subject -> this.subject = subject);
            Optional.ofNullable(source.factory)
                    .ifPresent(factory -> this.factory = factory);
        }

        public Builder subject(AnnotationPropertyAnalysis<Subject> subject) {
            this.subject = subject;
            return this;
        }

        public Builder type(AnnotationPropertyAnalysis<SemanticType> type) {
            types.add(type);
            return this;
        }

        public Builder predicate(FieldPropertyAnalysis<Predicate> predicate) {
            predicates.add(predicate);
            return this;
        }

        public Builder factory(InstanceStrategy.Factory factory) {
            this.factory = factory;
            return this;
        }

        public ClassAnalysis build() {
            return new ClassAnalysis(subject, types, predicates, factory);
        }
    }
}
