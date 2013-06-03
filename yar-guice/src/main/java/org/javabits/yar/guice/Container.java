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

package org.javabits.yar.guice;


import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * TODO comment
 * Date: 2/22/13
 * Time: 11:49 AM
 *
 * @author Romain Gilles
 */
public interface Container<K, V> {

    List<V> getAll(K key);

    @Nullable
    V getFirst(K key);

    boolean put(K key, V value);

    boolean remove(K key, V value);

    Map<K, ? extends Collection<V>> asMap();

    void invalidate(K key);

    void invalidateAll(Iterable<K> keys);

    void addKeyListener(KeyListener<K> keyListener);

    void removeKeyListener(KeyListener<K> keyListener);
}
