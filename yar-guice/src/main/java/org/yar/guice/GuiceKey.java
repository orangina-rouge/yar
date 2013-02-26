/*
 * Copyright 2013 Romain Gilles
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.yar.guice;

import org.yar.Key;

import javax.annotation.concurrent.Immutable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static java.util.Objects.requireNonNull;

/**
 * TODO comment
 * Date: 2/10/13
 * Time: 11:15 PM
 *
 * @author Romain Gilles
 */
@Immutable
public class GuiceKey<T> implements Key<T> {

    private final com.google.inject.Key<T> key;

    GuiceKey(com.google.inject.Key<T> key) {
        this.key = requireNonNull(key, "key");
    }

    @Override
    public Type type() {
        return key.getTypeLiteral().getType();
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return key.getAnnotationType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GuiceKey guiceKey = (GuiceKey) o;

        return key.equals(guiceKey.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    static <T> Key<T> of( com.google.inject.Key<T> key) {
        return new GuiceKey<>(key);
    }

    static <T> Key<T> of( Class<T> type) {
        return new GuiceKey<>(com.google.inject.Key.get(type));
    }
}