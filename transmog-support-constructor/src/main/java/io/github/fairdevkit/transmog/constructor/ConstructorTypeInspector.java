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
package io.github.fairdevkit.transmog.constructor;

import io.github.fairdevkit.transmog.spi.analyzer.FieldPropertyAnalysis;
import io.github.fairdevkit.transmog.spi.analyzer.IntrinsicTypeResolver;
import io.github.fairdevkit.transmog.spi.analyzer.TransmogAnalyzerException;
import io.github.fairdevkit.transmog.spi.analyzer.TypeInspector;
import io.github.fairdevkit.transmog.spi.writer.WrapperHandler;
import java.beans.ConstructorProperties;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class ConstructorTypeInspector implements TypeInspector {
    @Override
    public boolean supports(Class<?> type) {
        return Arrays.stream(type.getDeclaredConstructors())
                .anyMatch(constructor -> constructor.isAnnotationPresent(ConstructorProperties.class));
    }

    @Override
    public <A extends Annotation> void inspect(Class<?> type, Class<A> annotationType,
            Iterable<IntrinsicTypeResolver<?>> resolvers, Iterable<WrapperHandler> handlers,
            Consumer<FieldPropertyAnalysis.Builder<A>> consumer) {
        var constructor = Arrays.stream(type.getDeclaredConstructors())
                .filter(ctor -> ctor.isAnnotationPresent(ConstructorProperties.class))
                .findFirst()
                .orElseThrow(() -> new TransmogAnalyzerException("Could not find @ConstructorProperties annotated constructor"));

        var parametersAnnotation = constructor.getAnnotation(ConstructorProperties.class);

        for (var i = 0; i < constructor.getParameterCount(); i++) {
            var parameter = constructor.getParameters()[i];

            final String name;
            if (parameter.isNamePresent()) {
                name = parameter.getName();
            } else {
                name = parametersAnnotation.value()[i];
            }

            final Field field;
            try {
                field = type.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                throw new TransmogAnalyzerException("Could not find field for constructor argument " + name);
            }

            final Method getter;
            try {
                var prefix = field.getType().equals(boolean.class) ? "is" : "get";
                var prefixedName = prefix.concat(capitalize(name));
                getter = type.getMethod(prefixedName);
            } catch (NoSuchMethodException e) {
                throw new TransmogAnalyzerException("Could not find getter method for property " + name, e);//TODO
            }

            var annotation = Optional.ofNullable(field.getAnnotation(annotationType))
                    .or(() -> Optional.ofNullable(getter.getAnnotation(annotationType)));

            if (annotation.isEmpty()) {
                continue;
            }

            var builder = ConstructorPropertyAnalysis.<A>builder();

            annotation.ifPresent(builder::annotation);

            builder.name(field.getName())
                    .type(field.getType())
                    .accessor(getHandle(getter));

            builder.index(i);

            StreamSupport.stream(resolvers.spliterator(), false)
                    .filter(resolver -> resolver.supports(field))
                    .findFirst()
                    .map(resolver -> resolver.resolve(field))
                    .ifPresentOrElse(builder::intrinsicType, () -> {
                        builder.intrinsicType(field.getType());
                    });

            StreamSupport.stream(handlers.spliterator(), false)
                    .filter(handler -> handler.supports(getter.getReturnType()))
                    .findFirst()
                    .ifPresent(builder::wrapperHandler);

            consumer.accept(builder);
        }
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase().concat(s.substring(1));
    }

    public static MethodHandle getHandle(Method m) {
        try {
            return MethodHandles.publicLookup().unreflect(m);
        } catch (IllegalAccessException e) {
            throw new TransmogAnalyzerException("", e);//TODO
        }
    }
}
