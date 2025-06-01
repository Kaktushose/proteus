// https://github.com/spring-projects/spring-framework/blob/main/spring-core/src/main/java/org/springframework/core/ParameterizedTypeReference.java
/*
 * Copyright 2002-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.kaktushose.proteus.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/// The purpose of this class is to enable capturing and passing a generic [Type]. In order to capture the generic type
/// and retain it at runtime, you need to create a subclass (ideally as anonymous inline class) as follows:
///
/// ```
/// TypeReference<List<String>> typeRef = new TypeReference<List<String>>() {};
/// ```
///
/// The resulting `typeRef` instance can then be used to obtain a [Type] instance that carries the captured
/// parameterized type information at runtime. For more information on "super type tokens" see the link to Neal
/// Gafter's blog post.
///
/// @param <T> the referenced type
/// @author Arjen Poutsma
/// @author Rossen Stoyanchev
/// @see <a href="https://gafter.blogspot.nl/2006/12/super-type-tokens.html">Neal Gafter on Super Type Tokens</a>
public abstract class TypeReference<T> {

    private final Type type;

    @SuppressWarnings("unused")
    protected TypeReference() {
        Class<?> parameterizedTypeReferenceSubclass = findSubclass(getClass());
        Type type = parameterizedTypeReferenceSubclass.getGenericSuperclass();
        if (!(type instanceof ParameterizedType parameterizedType)) {
            throw new IllegalArgumentException("Type must be a parameterized type");
        }
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (actualTypeArguments.length != 1) {
            throw new IllegalArgumentException("Number of type arguments must be 1");
        }

        this.type = actualTypeArguments[0];
    }

    protected TypeReference(Type type) {
        this.type = type;
    }

    @NotNull
    private static Class<?> findSubclass(@NotNull Class<?> child) {
        Class<?> parent = child.getSuperclass();
        if (Object.class == parent) {
            throw new IllegalStateException("Expected TypeReference superclass");
        } else if (TypeReference.class == parent) {
            return child;
        } else {
            return findSubclass(parent);
        }
    }

    public Type type() {
        return type;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof TypeReference<?> that && this.type.equals(that.type)));
    }

    @Override
    public int hashCode() {
        return this.type.hashCode();
    }

    @Override
    public String toString() {
        return "TypeReference<" + this.type + ">";
    }
}