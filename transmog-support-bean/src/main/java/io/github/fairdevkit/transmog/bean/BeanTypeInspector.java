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
import io.github.fairdevkit.transmog.spi.analyzer.IntrinsicTypeResolver;
import io.github.fairdevkit.transmog.spi.analyzer.TransmogAnalyzerException;
import io.github.fairdevkit.transmog.spi.analyzer.TypeInspector;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class BeanTypeInspector extends BeanSupporting implements TypeInspector {
    @Override
    public <A extends Annotation> void inspect(Class<?> type, Class<A> annotationType,
            Stream<IntrinsicTypeResolver<?>> resolvers, Consumer<FieldPropertyAnalysis.Builder<A>> consumer) {
        for (var field : type.getDeclaredFields()) {
            var capitalizedName = capitalize(field.getName());

            final Method getter;
            try {
                var prefix = field.getType().equals(boolean.class) ? "is" : "get";
                getter = type.getMethod(prefix.concat(capitalizedName));
            } catch (NoSuchMethodException e) {
                throw new TransmogAnalyzerException("Could not find getter method for bean " + type, e);
            }

            final Method setter;
            try {
                setter = type.getMethod("set".concat(capitalizedName), field.getType());
            } catch (NoSuchMethodException e) {
                throw new TransmogAnalyzerException("Could not find setter method for bean " + type, e);
            }

            var annotation = Optional.ofNullable(field.getAnnotation(annotationType))
                    .or(() -> Optional.ofNullable(getter.getAnnotation(annotationType)))
                    .or(() -> Optional.ofNullable(setter.getAnnotation(annotationType)));

            if (annotation.isEmpty()) {
                continue;
            }

            var builder = BeanPropertyAnalysis.<A>builder();

            resolvers.filter(resolver -> resolver.supports(field))
                    .findFirst()
                    .map(resolver -> resolver.resolve(field))
                    .ifPresentOrElse(builder::intrinsicType, () -> {
                        builder.intrinsicType(field.getType());
                    });

            annotation.map(builder::annotation);

            builder.mutator(getHandle(setter))
                    .accessor(getHandle(getter));

            builder.name(field.getName())
                    .type(field.getType());

            consumer.accept(builder);
        }
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase().concat(s.substring(1));
    }

    private static MethodHandle getHandle(Method method) {
        try {
            return MethodHandles.lookup().unreflect(method);
        } catch (IllegalAccessException e) {
            throw new TransmogAnalyzerException("Could not resolve method handle for " + method, e);
        }
    }
}
