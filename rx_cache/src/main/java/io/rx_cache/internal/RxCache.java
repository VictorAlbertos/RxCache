/*
 * Copyright 2015 Victor Albertos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rx_cache.internal;

import java.io.File;
import java.lang.reflect.Proxy;
import java.security.InvalidParameterException;

import io.rx_cache.Persistence;
import io.rx_cache.PolicyHeapCache;
import io.rx_cache.internal.cache.TwoLayersCache;

public final class RxCache {
    private final ProxyProviders proxyProviders;

    private RxCache(ProxyProviders proxyProviders) {
        this.proxyProviders = proxyProviders;
    }

    public <T> T using(final Class<T> providers) {
        T proxy = (T) Proxy.newProxyInstance(
                providers.getClassLoader(),
                new Class<?>[]{providers},
                proxyProviders);
        return proxy;
    }

    /**
     * Builder for building an specific RxCache instance
     */
    public static class Builder {
        private PolicyHeapCache policyHeapCache;
        private boolean useExpiredDataIfLoaderNotAvailable;

        /**
         * If true RxCache will serve Records already expired, instead of evict them and throw an exception
         * If not supplied, false will be the default option
         * @return BuilderRxCache The builder of RxCache
         */
        public Builder useExpiredDataIfLoaderNotAvailable(boolean useExpiredDataIfLoaderNotAvailable) {
            this.useExpiredDataIfLoaderNotAvailable = useExpiredDataIfLoaderNotAvailable;
            return this;
        }

        /**
         * Sets the policy memory used by the memory cache
         * If not supplied, CONSERVATIVE will be the default option
         * @param aPolicyHeapCache The File system used by the persistence implementation of Disk
         * @return BuilderRxCache The builder of RxCache
         * @see PolicyHeapCache
         */
        public Builder withPolicyCache(PolicyHeapCache aPolicyHeapCache) {
            policyHeapCache = aPolicyHeapCache;
            return this;
        }

        /**
         * Sets the File cache system used by Cache
         * @param cacheDirectory The File system used by the persistence implementation of Disk
         * @see TwoLayersCache
         */
        public RxCache persistence(File cacheDirectory) {
            if (cacheDirectory == null)
                throw new InvalidParameterException(Locale.REPOSITORY_DISK_ADAPTER_CAN_NOT_BE_NULL);

            PolicyHeapCache policy = policyHeapCache != null ? policyHeapCache : PolicyHeapCache.CONSERVATIVE;

            ProxyProviders proxyProviders = DaggerRxCacheComponent.builder()
                    .rxCacheModule(new RxCacheModule(cacheDirectory, policy, useExpiredDataIfLoaderNotAvailable))
                    .build().proxyRepository();
            return new RxCache(proxyProviders);
        }

        /**
         * Sets an implementation of Persistence layer. By default, Disk is supplied
         * @param persistence The interface provided to propose a mechanism for persisting data
         * @see Persistence
         * @see Disk
         */
        public RxCache persistence(Persistence persistence) {
            if (persistence == null)
                throw new InvalidParameterException(Locale.PERSISTENCE_CAN_NOT_BE_NULL);

            PolicyHeapCache policy = policyHeapCache != null ? policyHeapCache : PolicyHeapCache.CONSERVATIVE;

            ProxyProviders proxyProviders = DaggerRxCacheComponent.builder()
                    .rxCacheModule(new RxCacheModule(persistence, policy, useExpiredDataIfLoaderNotAvailable))
                    .build().proxyRepository();
            return new RxCache(proxyProviders);
        }

    }

}
