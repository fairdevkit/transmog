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

import io.github.fairdevkit.transmog.spi.analyzer.IntrinsicTypeResolver;
import io.github.fairdevkit.transmog.spi.analyzer.TransmogAnalyzerException;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

public class ParameterizedIntrinsicTypeResolver implements IntrinsicTypeResolver<AnnotatedType> {
    @Override
    public boolean supports(Field field) {
        return supports(field.getAnnotatedType());
    }

    @Override
    public boolean supports(RecordComponent component) {
        return supports(component.getAnnotatedType());
    }

    @Override
    public boolean supports(AnnotatedType annotatedType) {
        if (!(annotatedType instanceof AnnotatedParameterizedType)) {
            var type = (GenericDeclaration) annotatedType.getType();

            return type.getTypeParameters().length > 0;
        }
        return true;
    }

    @Override
    public Class<?> resolve(Field field) {
        return resolve(field.getAnnotatedType());
    }

    @Override
    public Class<?> resolve(RecordComponent component) {
        return resolve(component.getAnnotatedType());
    }

    @Override
    public Class<?> resolve(AnnotatedType annotatedType) {
        if (annotatedType instanceof AnnotatedParameterizedType parameterizedType) {
            var actualTypeArguments = parameterizedType.getAnnotatedActualTypeArguments();

            var actualType = actualTypeArguments[0].getType();

            if (actualType instanceof WildcardType wildcardType) {
                if (wildcardType.getLowerBounds().length > 0) {
                    // <? super Type>
                    return (Class<?>)wildcardType.getLowerBounds()[0];
                }

                // <? extends Type>, or <?>
                return (Class<?>)wildcardType.getUpperBounds()[0];
            }

            if (actualType instanceof TypeVariable typeVariable) {
                if (typeVariable.getBounds().length > 0) {
                    // <T extends Type>
                    return (Class<?>)typeVariable.getBounds()[0];
                }

                // <T>
                return Object.class;
            }

            // <Type>
            return (Class<?>)actualType;
        } else {
            var type = (GenericDeclaration)annotatedType.getType();

            if (type.getTypeParameters().length > 0) {
                // raw type
                return Object.class;
            }

            // non-parameterized type
            throw new TransmogAnalyzerException("nope");//TODO
        }
    }
}
