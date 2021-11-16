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
import java.util.Collection;
import lombok.Data;

public class Beans implements Constants {
    @Data
    public static class IntPropertyBean {
        @Predicate(value = PREDICATE_VALUE, literal = true, datatype = "http://www.w3.org/2001/XMLSchema#int")
        private int value;
    }

    @Data
    public static class IntegerPropertyBean {
        @Predicate(value = PREDICATE_VALUE, literal = true, datatype = "http://www.w3.org/2001/XMLSchema#int")
        private Integer value;
    }

    @Data
    public static class StringPropertyBean {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private String value;
    }

    @Data
    public static class IntArrayPropertyBean {
        @Predicate(value = PREDICATE_VALUE, literal = true, datatype = "http://www.w3.org/2001/XMLSchema#int")
        private int[] value;
    }

    @Data
    public static class IntegerArrayPropertyBean {
        @Predicate(value = PREDICATE_VALUE, literal = true, datatype = "http://www.w3.org/2001/XMLSchema#int")
        private Integer[] value;
    }

    @Data
    public static class StringArrayPropertyBean {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private String[] value;
    }

    @Data
    public static class ParameterizedArrayPropertyBean<T> {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private T[] value;
    }

    @Data
    public static class StringCollectionPropertyBean {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private Collection<String> value;
    }

    @Data
    public static class ParameterizedCollectionPropertyBean<T> {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private Collection<T> value;
    }

    @Data
    public static class ParameterizedNumberCollectionPropertyBean<T extends Number> {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private Collection<T> value;
    }

    @Data
    public static class StringUpperBoundCollectionPropertyBean {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private Collection<? extends String> value;
    }

    @Data
    public static class StringLowerBoundCollectionPropertyBean {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private Collection<? super String> value;
    }

    @Data
    public static class WildcardCollectionPropertyBean {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private Collection<?> value;
    }

    @Data
    public static class RawCollectionPropertyBean {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private Collection value;
    }
}
