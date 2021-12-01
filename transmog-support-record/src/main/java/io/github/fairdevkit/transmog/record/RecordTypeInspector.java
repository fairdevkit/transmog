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
package io.github.fairdevkit.transmog.record;

import io.github.fairdevkit.transmog.spi.analyzer.FieldPropertyAnalysis;
import io.github.fairdevkit.transmog.spi.analyzer.IntrinsicTypeResolver;
import io.github.fairdevkit.transmog.spi.analyzer.TransmogAnalyzerException;
import io.github.fairdevkit.transmog.spi.analyzer.TypeInspector;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class RecordTypeInspector implements TypeInspector {
    @Override
    public boolean supports(Class<?> type) {
        return type.isRecord();
    }

    @Override
    public <A extends Annotation> void inspect(Class<?> type, Class<A> annotationType,
            Stream<IntrinsicTypeResolver<?>> resolvers, Consumer<FieldPropertyAnalysis.Builder<A>> consumer) {
        for (var component : type.getRecordComponents()) {
            if (!component.isAnnotationPresent(annotationType)) {
                continue;
            }

            var builder = RecordPropertyAnalysis.<A>builder();

            resolvers.filter(resolver -> resolver.supports(component))
                    .findFirst()
                    .map(resolver -> resolver.resolve(component))
                    .ifPresentOrElse(builder::intrinsicType, () -> {
                        builder.intrinsicType(component.getType());
                    });

            builder.accessor(getHandle(component.getAccessor()))
                    .name(component.getName())
                    .type(component.getType())
                    .annotation(component.getAnnotation(annotationType));

            consumer.accept(builder);
        }
    }

    private static MethodHandle getHandle(Method m) {
        try {
            return MethodHandles.lookup().unreflect(m);
        } catch (IllegalAccessException e) {
            throw new TransmogAnalyzerException("Could not resolve method handle for " + m, e);
        }
    }
}
