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

import org.yar.AbstractSingleElementWatcher;
import org.yar.Supplier;
import org.yar.Watcher;

import javax.annotation.Nullable;
import java.util.concurrent.locks.*;

import static java.util.Objects.requireNonNull;

/**
* TODO comment
* Date: 2/28/13
* Time: 11:11 AM
*
* @author Romain Gilles
*/
abstract class AbstractBlockingSupplier<T> implements Supplier<T>, Watcher<Supplier<T>> {

    final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    final Lock readLock = readWriteLock.readLock();
    final Lock writeLock = readWriteLock.writeLock();
    /**
     * Condition for waiting takes
     */
    final Condition notEmpty = writeLock.newCondition();

    Supplier<T> delegate;

    AbstractBlockingSupplier(Supplier<T> delegate) {
        this.delegate = delegate;
    }




    @Nullable
    @Override
    public final Supplier<T> add(Supplier<T> element) {
        requireNonNull(element, "element");
        readLock.lock();
        try {
            if (delegate != null) {
                return null;
            }
        } finally {
            readLock.unlock();
        }
        writeLock.lock();
        try {
            if (delegate == null) {
                delegate = element;
                notEmpty.signal();
                return element;
            }
            return null;

        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public final void remove(Supplier<T> element) {
        requireNonNull(element, "element");
        readLock.lock();
        try {
            if (delegate != element) { //identity test handle delegate == null also
                return;
            }
        } finally {
            readLock.unlock();
        }
        writeLock.lock();
        try {
            if (delegate == element) { //identity test handle delegate == null also
                delegate = null;
            }
        } finally {
            writeLock.unlock();
        }
    }
}