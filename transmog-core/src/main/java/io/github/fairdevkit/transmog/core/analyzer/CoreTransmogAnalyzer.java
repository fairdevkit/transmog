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
package io.github.fairdevkit.transmog.core.analyzer;

import static java.util.function.Predicate.not;
import io.github.fairdevkit.transmog.annotations.Predicate;
import io.github.fairdevkit.transmog.annotations.SemanticType;
import io.github.fairdevkit.transmog.annotations.Subject;
import io.github.fairdevkit.transmog.api.analyzer.TransmogAnalyzer;
import io.github.fairdevkit.transmog.core.util.TransmogUtil;
import io.github.fairdevkit.transmog.spi.analyzer.AnnotationPropertyAnalysis;
import io.github.fairdevkit.transmog.spi.analyzer.ClassAnalysis;
import io.github.fairdevkit.transmog.spi.analyzer.FieldPropertyAnalysis;
import io.github.fairdevkit.transmog.spi.analyzer.IntrinsicTypeResolver;
import io.github.fairdevkit.transmog.spi.analyzer.TransmogAnalyzerException;
import io.github.fairdevkit.transmog.spi.analyzer.TypeInspector;
import io.github.fairdevkit.transmog.spi.reader.ArgumentStrategy;
import io.github.fairdevkit.transmog.spi.reader.InstanceStrategy;
import io.github.fairdevkit.transmog.spi.reader.ValueConverter;
import io.github.fairdevkit.transmog.spi.writer.WrapperHandler;
import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class CoreTransmogAnalyzer implements TransmogAnalyzer {
    private final Map<Class<?>, ClassAnalysis> cache;

    private final Collection<InstanceStrategy.Factory> instanceStrategies;
    private final Collection<TypeInspector> typeInspectors;
    private final Collection<IntrinsicTypeResolver<?>> intrinsicTypeResolvers;
    private final Collection<ArgumentStrategy.Factory> argumentStrategies;
    private final Map<Class<?>, ValueConverter<?>> valueConverters;
    private final Collection<WrapperHandler> wrapperHandlers;

    public CoreTransmogAnalyzer() {
        cache = new ConcurrentHashMap<>();

        instanceStrategies = new ArrayList<>();
        typeInspectors = new ArrayList<>();
        intrinsicTypeResolvers = new ArrayList<>();
        argumentStrategies = new ArrayList<>();
        valueConverters = new HashMap<>();
        wrapperHandlers = new ArrayList<>();
    }

    @Override
    public void registerInstanceStrategy(InstanceStrategy.Factory factory) {
        instanceStrategies.add(factory);
    }

    @Override
    public void registerTypeInspector(TypeInspector inspector) {
        typeInspectors.add(inspector);
    }

    @Override
    public void registerIntrinsicTypeResolver(IntrinsicTypeResolver<?> resolver) {
        intrinsicTypeResolvers.add(resolver);
    }

    @Override
    public void registerArgumentStrategy(ArgumentStrategy.Factory factory) {
        argumentStrategies.add(factory);
    }

    @Override
    public void registerValueConverter(Class<?> target, ValueConverter<?> converter) {
        valueConverters.put(target, converter);
    }

    @Override
    public void registerWrapperHandler(WrapperHandler handler) {
        wrapperHandlers.add(handler);
    }

    @Override
    public ClassAnalysis analyze(Class<?> type) {
        return analyzeInternal(type, new ArrayDeque<>());
    }

    private ClassAnalysis analyzeInternal(Class<?> clazz, Deque<Class<?>> stack) {
        if (cache.containsKey(clazz)) {
            return cache.get(clazz);
        }

        stack.offer(clazz);

        var builder = Optional.ofNullable(clazz.getSuperclass())
                .filter(not(TransmogUtil::isSystemClass))
                .filter(not(stack::contains))
                .map(parent -> analyzeInternal(parent, stack))
                .map(ClassAnalysis::copy)
                .orElseGet(ClassAnalysis::builder);

        // consume type annotations
        consumeTypeAnnotation(clazz, Subject.class, builder::subject);
        consumeTypeAnnotation(clazz, SemanticType.class, builder::type);

        // instance strategy
        instanceStrategies.stream()
                .filter(factory -> factory.supports(clazz))
                .findFirst()
                .ifPresent(builder::factory);

        // type inspector
        var inspector = typeInspectors.stream()
                .filter(insp -> insp.supports(clazz))
                .findFirst()
                .orElseThrow(() -> new TransmogAnalyzerException("Could not find type inspector for " + clazz));

        inspector.inspect(clazz, Predicate.class, intrinsicTypeResolvers, wrapperHandlers, bldr -> consumeProperty(bldr, stack, builder::predicate));
        inspector.inspect(clazz, Subject.class, intrinsicTypeResolvers, wrapperHandlers, bldr -> consumeProperty(bldr, stack, builder::subject));
        inspector.inspect(clazz, SemanticType.class, intrinsicTypeResolvers, wrapperHandlers, bldr -> consumeProperty(bldr, stack, builder::type));

        var analysis = builder.build();
        cache.put(clazz, analysis);

        return analysis;
    }

    private <A extends Annotation> void consumeTypeAnnotation(Class<?> type, Class<A> annotationType,
            Consumer<AnnotationPropertyAnalysis<A>> consumer) {
        Optional.ofNullable(type.getAnnotation(annotationType))
                .map(AnnotationPropertyAnalysis.<A>builder()::annotation)
                .map(AnnotationPropertyAnalysis.Builder::build)
                .ifPresent(consumer);
    }

    private <A extends Annotation> void consumeProperty(FieldPropertyAnalysis.Builder<A> builder,
            Deque<Class<?>> stack, Consumer<FieldPropertyAnalysis<A>> callback) {
        var type = builder.peekType().orElseThrow();
        var intrinsicType = builder.peekIntrinsicType().or(builder::peekType).orElseThrow();

        if (!TransmogUtil.isSystemClass(intrinsicType)) {
            if (!stack.contains(intrinsicType)) {
                try {
                    analyzeInternal(intrinsicType, stack);
                } catch (TransmogAnalyzerException e) {
                    throw new TransmogAnalyzerException("Could not analyze nested " + intrinsicType, e);
                }
            }
            builder.nested(true);
        } else {
            if (valueConverters.containsKey(intrinsicType)) {
                builder.valueConverter(valueConverters.get(intrinsicType));
            } else {
                throw new TransmogAnalyzerException("Could not find an appropriate value converter for " + intrinsicType);
            }
        }

        argumentStrategies.stream()
                .filter(factory -> factory.supports(type))
                .findFirst()
                .ifPresentOrElse(builder::factory, () -> {
                    throw new TransmogAnalyzerException("Could not find an argument strategy for " + type);
                });

        callback.accept(builder.build());
    }
}
