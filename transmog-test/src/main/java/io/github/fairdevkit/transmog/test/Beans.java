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
import io.github.fairdevkit.transmog.annotations.SemanticType;
import java.util.Collection;
import java.util.Optional;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public interface Beans extends Constants {
    @Data
    class IntPropertyBean {
        @Predicate(value = PREDICATE_VALUE, literal = true, datatype = "http://www.w3.org/2001/XMLSchema#int")
        private int value;
    }

    @Data
    class IntegerPropertyBean {
        @Predicate(value = PREDICATE_VALUE, literal = true, datatype = "http://www.w3.org/2001/XMLSchema#int")
        private Integer value;
    }

    @Data
    class BooleanPropertyBean {
        @Predicate(value = PREDICATE_VALUE, literal = true, datatype = "http://www.w3.org/2001/XMLSchema#boolean")
        private boolean value;
    }

    @Data
    class StringPropertyBean {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private String value;
    }

    @Data
    class IntArrayPropertyBean {
        @Predicate(value = PREDICATE_VALUE, literal = true, datatype = "http://www.w3.org/2001/XMLSchema#int")
        private int[] value;
    }

    @Data
    class IntegerArrayPropertyBean {
        @Predicate(value = PREDICATE_VALUE, literal = true, datatype = "http://www.w3.org/2001/XMLSchema#int")
        private Integer[] value;
    }

    @Data
    class StringArrayPropertyBean {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private String[] value;
    }

    @Data
    class ParameterizedArrayPropertyBean<T> {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private T[] value;
    }

    @Data
    class StringCollectionPropertyBean {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private Collection<String> value;
    }

    @Data
    class ParameterizedCollectionPropertyBean<T> {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private Collection<T> value;
    }

    @Data
    class ParameterizedNumberCollectionPropertyBean<T extends Number> {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private Collection<T> value;
    }

    @Data
    class StringUpperBoundCollectionPropertyBean {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private Collection<? extends String> value;
    }

    @Data
    class StringLowerBoundCollectionPropertyBean {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private Collection<? super String> value;
    }

    @Data
    class WildcardCollectionPropertyBean {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private Collection<?> value;
    }

    @Data
    class RawCollectionPropertyBean {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private Collection value;
    }

    @Data
    class ReadonlyStringPropertyBean {
        @Predicate(value = PREDICATE_VALUE, literal = true, readonly = true)
        private String value;
    }

    @Data
    class OptionalStringPropertyBean {
        @Predicate(value = PREDICATE_VALUE, literal = true, required = false)
        private String value;

        public Optional<String> getValue() {
            return Optional.ofNullable(value);
        }
    }

    @Data
    class StringAndBooleanPropertiesBean {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private String value;
        @Predicate(value = PREDICATE_FLAG, literal = true, datatype = "http://www.w3.org/2001/XMLSchema#boolean")
        private boolean flag;
    }

    interface Invalid {
        @Getter @Setter
        class PrivateConstructor {
            @Predicate(value = PREDICATE_VALUE, literal = true)
            private String value;

            private PrivateConstructor() {
            }
        }

        @Data
        class ArgConstructor {
            @Predicate(value = PREDICATE_VALUE, literal = true)
            private final String value;
        }

        @Setter
        class AbsentGetter {
            @Predicate(value = PREDICATE_VALUE, literal = true)
            private String value;
        }

        @Getter
        class AbsentSetter {
            @Predicate(value = PREDICATE_VALUE, literal = true)
            private String value;
        }

        @Data
        class ThrowingConstructor {
            @Predicate(value = PREDICATE_VALUE, literal = true)
            private String value;

            public ThrowingConstructor() {
                throw new IllegalStateException("for testing");
            }
        }

        @Data
        class ThrowingMutator {
            @Predicate(value = PREDICATE_VALUE, literal = true)
            private String value;

            public void setValue(String value) {
                throw new IllegalStateException("for testing");
            }
        }
    }
}
