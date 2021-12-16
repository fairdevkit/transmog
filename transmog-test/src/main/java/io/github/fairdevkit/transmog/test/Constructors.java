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
package io.github.fairdevkit.transmog.test;

import io.github.fairdevkit.transmog.annotations.Predicate;
import java.beans.ConstructorProperties;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface Constructors extends Constants {
    @Data
    @RequiredArgsConstructor(onConstructor_ = @ConstructorProperties("value"))
    class StringPropertyConstructor {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private final String value;
    }

    @Data
    @RequiredArgsConstructor(onConstructor_ = @ConstructorProperties("value"))
    abstract class ParentConstructor {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private final String value;
    }

    @Getter
    class ChildConstructor extends ParentConstructor {
        @Predicate(value = PREDICATE_FLAG, literal = true, datatype = "http://www.w3.org/2001/XMLSchema#boolean")
        private final boolean flag;

        @ConstructorProperties({ "value", "flag" })
        public ChildConstructor(String value, boolean flag) {
            super(value);
            this.flag = flag;
        }
    }

    @Getter
    class Foo {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private final String value;

        @ConstructorProperties("value")
        public Foo(String foo) {
            this.value = foo;
        }
    }

    interface Invalid {
        @Data
        class MissingAnnotation {
            @Predicate(value = PREDICATE_VALUE, literal = true)
            private final String value;
        }

        @Data
        @RequiredArgsConstructor(onConstructor_ = @ConstructorProperties("value"))
        class MissingAnnotationAttribute {
            @Predicate(value = PREDICATE_VALUE, literal = true)
            private final String value;
            @Predicate(value = PREDICATE_FLAG, literal = true, datatype = "http://www.w3.org/2001/XMLSchema#boolean")
            private final boolean flag;
        }

        @Data
        // lombok bug, check for fix in versions newer than 1.18.22
        // https://github.com/projectlombok/lombok/issues/3040
        //@RequiredArgsConstructor(onConstructor_ = @ConstructorProperties({ "value", "flag" }))
        class SuperfluousAttributes {
            @Predicate(value = PREDICATE_FLAG, literal = true)
            private final String value;

            @ConstructorProperties({ "value", "flag" })
            public SuperfluousAttributes(String value) {
                this.value = value;
            }
        }

        @RequiredArgsConstructor(onConstructor_ = @ConstructorProperties("value"))
        class AbsentGetter {
            @Predicate(value = PREDICATE_VALUE, literal = true)
            private final String value;
        }
    }
}
