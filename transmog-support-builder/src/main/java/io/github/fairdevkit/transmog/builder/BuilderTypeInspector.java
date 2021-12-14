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
package io.github.fairdevkit.transmog.builder;

import io.github.fairdevkit.transmog.spi.analyzer.FieldPropertyAnalysis;
import io.github.fairdevkit.transmog.spi.analyzer.IntrinsicTypeResolver;
import io.github.fairdevkit.transmog.spi.analyzer.TransmogAnalyzerException;
import io.github.fairdevkit.transmog.spi.analyzer.TypeInspector;
import io.github.fairdevkit.transmog.spi.writer.WrapperHandler;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class BuilderTypeInspector implements TypeInspector {
    @Override
    public boolean supports(Class<?> type) {
        try {
            var factoryMethod = type.getMethod(BuilderInstanceStrategy.BUILDER_FACTORY_METHOD_NAME);

            return Modifier.isStatic(factoryMethod.getModifiers());
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @Override
    public <A extends Annotation> void inspect(Class<?> type, Class<A> annotationType,
            Iterable<IntrinsicTypeResolver<?>> resolvers, Iterable<WrapperHandler> handlers,
            Consumer<FieldPropertyAnalysis.Builder<A>> consumer) {
        var builderType = Arrays.stream(type.getDeclaredClasses())
                .filter(declared -> BuilderInstanceStrategy.BUILDER_CLASS_NAME.equals(declared.getSimpleName()))
                .findFirst()
                .orElseThrow();

        for (var field : type.getDeclaredFields()) {
            var name = capitalize(field.getName());

            final Method getter;
            try {
                var prefix = field.getType().equals(boolean.class) ? "is" : "get";
                getter = type.getMethod(prefix.concat(name));
            } catch (NoSuchMethodException e) {
                throw new TransmogAnalyzerException("", e);//TODO
            }

            var annotation = Optional.ofNullable(field.getAnnotation(annotationType))
                    .or(() -> Optional.ofNullable(getter.getAnnotation(annotationType)));

            if (annotation.isEmpty()) {
                continue;
            }

            var builder = BuilderPropertyAnalysis.<A>builder();

            annotation.map(builder::annotation);

            builder.name(field.getName())
                    .type(field.getType())
                    .accessor(getHandle(getter));

            StreamSupport.stream(resolvers.spliterator(), false)
                    .filter(resolver -> resolver.supports(field))
                    .findFirst()
                    .map(resolver -> resolver.resolve(field))
                    .ifPresentOrElse(builder::intrinsicType, () -> {
                        builder.intrinsicType(field.getType());
                    });

            final Method builderProperty;
            try {
                builderProperty = builderType.getDeclaredMethod(field.getName(), builder.peekIntrinsicType().orElseThrow());
            } catch (NoSuchMethodException e) {
                throw new TransmogAnalyzerException("");//TODO
            }
            builder.builderProperty(getHandle(builderProperty));

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

    private static MethodHandle getHandle(Method method) {
        try {
            return MethodHandles.lookup().unreflect(method);
        } catch (IllegalAccessException e) {
            throw new TransmogAnalyzerException("", e);//TODO
        }
    }
}
