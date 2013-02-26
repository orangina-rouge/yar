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
import org.yar.Supplier;
import org.yar.Watcher;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Objects.requireNonNull;

/**
 * TODO comment
 * Date: 2/25/13
 * Time: 10:30 PM
 *
 * @author Romain Gilles
 */
public class BlockingRegistry extends SimpleRegistry implements org.yar.BlockingRegistry {

    public static final long DEFAULT_TIMEOUT = 0L;

    private final long defaultTimeout;

    public BlockingRegistry() {
        this(DEFAULT_TIMEOUT);
    }

    public BlockingRegistry(long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    @Nullable
    @Override
    public <T> Supplier<T> get(Key<T> key) {
        return createSupplier(super.get(key), defaultTimeout, TimeUnit.MILLISECONDS);
    }

    @Nullable
    @Override
    public <T> Supplier<T> get(Class<T> type, long timeout, TimeUnit unit) {
        return createSupplier(super.get(type), timeout, unit);
    }

    @Nullable
    @Override
    public <T> Supplier<T> get(Key<T> key, long timeout, TimeUnit unit) {
        return createSupplier(super.get(key), timeout, unit);
    }

    private static <T> Supplier<T> createSupplier(Supplier<T> originalValue, long timeout, TimeUnit unit) {
        if (timeout == 0) { //direct
            return originalValue;
        } else if (timeout < 0) {
            return new InfiniteBlockingSupplier<>(originalValue);
        } else {
            return new TimeoutBlockingSupplier<>(originalValue, timeout, unit);
        }
    }

    abstract class AbstractBlockingSupplier<T> implements Supplier<T>, Watcher<Supplier<T>> {
        /**
         * Main lock guarding all access
         */
        final ReentrantLock lock;
        /**
         * Condition for waiting takes
         */
        final Condition notEmpty;

        Supplier<T> delegate;

        AbstractBlockingSupplier(Supplier<T> delegate) {
            this.delegate = delegate;
            lock = new ReentrantLock();//TODO see if we propose the fair mode?
            notEmpty = lock.newCondition();
        }

        @Nullable
        @Override
        public final Supplier<T> add(Supplier<T> element) {
            requireNonNull(element, "element");
            lock.lock();
            try {
                if (delegate == null) {
                    delegate = element;
                    notEmpty.signal();
                    return element;
                }
                return null;

            } finally {
                lock.unlock();
            }
        }

        @Override
        public final void remove(Supplier<T> element) {
            lock.lock();
            try {
                delegate = null;
            } finally {
                lock.unlock();
            }
        }
    }

    private static class TimeoutBlockingSupplier<T> extends AbstractBlockingSupplier<T> {
        private final long timeout;
        private final TimeUnit unit;

        private TimeoutBlockingSupplier(Supplier<T> delegate, long timeout, TimeUnit unit) {
            super(delegate);
            this.timeout = timeout;
            this.unit = unit;

        }

        @Override
        public T get() {
            long nanos = unit.toNanos(timeout);
            lock.lock();
            try {
                while (delegate == null) {
                    if (nanos <= 0L) {
                        return null; // TODO maybe throw an exception instead!!!
                    }
                    nanos = notEmpty.awaitNanos(nanos);
                }
                return delegate.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e); // TODO see what to do with the interrupted exception!!!
            } finally {
                lock.unlock();
            }
        }
    }

    private static class InfiniteBlockingSupplier<T> extends AbstractBlockingSupplier<T> {
        private InfiniteBlockingSupplier(Supplier<T> delegate) {
            super(delegate);
        }

        @Override
        public T get() {
            lock.lock();
            try {
                while (delegate == null) {
                    notEmpty.await();
                }
                return delegate.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e); // TODO see what to do with the interrupted exception!!!
            } finally {
                lock.unlock();
            }
        }
    }
}