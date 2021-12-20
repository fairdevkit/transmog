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

public interface Subjects extends Constants {
    @Data
    @Subject(SUBJECT_1)
    class TypeAbsoluteSubject {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private String value;
    }

    @Data
    @Subject(value = "rel", relative = true)
    class TypeRelativeSubject {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private String value;
    }

    @Data
    @Subject(value = "rel", separator = ':')
    class TypeRelativeSubjectCustomSeparator {
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private String value;
    }

    @Data
    class PropertyAbsoluteSubject {
        @Subject
        private String subject;
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private String value;
    }

    @Data
    class PropertyRelativeSubject {
        @Subject(relative = true)
        private String subject;
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private String value;
    }

    @Data
    class PropertyRelativeSubjectCustomSeparator {
        @Subject(relative = true, separator =':')
        private String subject;
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private String value;
    }

    @Data
    class PropertySubjectBlankNode {
        @Subject(blankNode = true)
        private String subject;
        @Predicate(value = PREDICATE_VALUE, literal = true)
        private String value;
    }

    @Data
    class Holder<T> {
        @Predicate(PREDICATE_CHILD)
        private T child;
    }

    class XHolder extends Holder<TypeAbsoluteSubject> {
    }
}
