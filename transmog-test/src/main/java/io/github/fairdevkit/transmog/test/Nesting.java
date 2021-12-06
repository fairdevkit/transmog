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
import io.github.fairdevkit.transmog.annotations.Subject;
import lombok.Data;

public interface Nesting extends Constants {
    @Data
    class Parent {
        @Predicate(PREDICATE_CHILD)
        private Child child;
    }

    @Data
    @Subject(value = "child", relative = true)
    class Child {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private String value;
    }

    @Data
    class Node {
        @Subject
        private String subject;
        @Predicate(value = PREDICATE_NODE, required = false)
        private Node node;
        @Predicate(value = PREDICATE_VALUE, literal = true, required = false)
        private String value;
    }
}
