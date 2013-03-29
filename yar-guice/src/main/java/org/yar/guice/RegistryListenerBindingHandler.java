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

import com.google.common.collect.ImmutableList;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.yar.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;

import static org.yar.guice.Matchers.getYarKey;

/**
 * TODO comment
 * TODO add support for Supplier and BlockingSupplier
 * Date: 3/14/13
 *
 * @author Romain Gilles
 */
public class RegistryListenerBindingHandler implements RegistryListenerHandler {
    private final Injector injector;
    private final Registry registry;
    //we keep a strong reference on the watcher to avoid it to be garbage collected
    //therefore the lifecycle to this watcher is at least associated to the lifecycle of owning injector
    private final List<Pair<Registration<?>,Watcher>> listenerRegistrations;


    @Inject
    public RegistryListenerBindingHandler(Injector injector, Registry registry) {
        this.injector = injector;
        this.registry = registry;
        this.listenerRegistrations = addListenerToRegistry();
    }
    @SuppressWarnings("unchecked")
    private List<Pair<Registration<?>, Watcher>> addListenerToRegistry() {
        ImmutableList.Builder<Pair<Registration<?>, Watcher>> registrationsBuilder = ImmutableList.builder();
        for (Binding<GuiceWatcherRegistration> watcherRegistrationBinding : injector.findBindingsByType(TypeLiteral.get(GuiceWatcherRegistration.class))) {
            GuiceWatcherRegistration guiceWatcherRegistration = watcherRegistrationBinding.getProvider().get();
            Watcher watcher = guiceWatcherRegistration.watcher();
            Registration<?> registration = registry.addWatcher(guiceWatcherRegistration.matcher(), watcher);
            registrationsBuilder.add(new StrongPair<Registration<?>, Watcher>(registration, watcher));
        }
        return registrationsBuilder.build();
    }

    @Override
    public void clear() {
        for (Pair<Registration<?>, Watcher> listenerRegistration : listenerRegistrations) {
            registry.removeWatcher(listenerRegistration.left());
        }

    }
}
