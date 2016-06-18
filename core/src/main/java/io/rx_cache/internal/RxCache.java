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

import io.victoralbertos.jolyglot.Jolyglot;

public final class RxCache {
    private final Builder builder;
    private static Object retainedProxy;

    private RxCache(Builder builder) {
        this.builder = builder;
    }

    public <T> T using(final Class<T> classProviders) {
        ProxyProviders proxyProviders = DaggerRxCacheComponent.builder()
                .rxCacheModule(new RxCacheModule(builder.cacheDirectory, builder.useExpiredDataIfLoaderNotAvailable,
                        builder.maxMBPersistenceCache, classProviders, builder.jolyglot))
                .build().proxyRepository();

        T proxy = (T) Proxy.newProxyInstance(
                classProviders.getClassLoader(),
                new Class<?>[]{classProviders},
                proxyProviders);

        retainedProxy = proxy;

        return proxy;
    }

    /**
     * To be able to access from ActionsProviders auto-generated class.
     * @return the current instance of the implemented providers interface.
     */
    public static Object retainedProxy() {
        return retainedProxy;
    }

    /**
     * Builder for building an specific RxCache instance
     */
    public static class Builder {
        private boolean useExpiredDataIfLoaderNotAvailable;
        private Integer maxMBPersistenceCache;
        private File cacheDirectory;
        private Jolyglot jolyglot;

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
         * Sets the max memory in megabytes for all stored records on persistence layer
         * If not supplied, 100 megabytes will be the default option
         * @return BuilderRxCache The builder of RxCache
         */
        public Builder setMaxMBPersistenceCache(Integer maxMgPersistenceCache) {
            this.maxMBPersistenceCache = maxMgPersistenceCache;
            return this;
        }

        /**
         * Sets the File cache system and the implementation of {@link Jolyglot} to serialise and deserialize objects
         * @param cacheDirectory The File system used by the persistence implementation of Disk
         * @param jolyglot A concrete implementation of {@link Jolyglot}
         */
        public RxCache persistence(File cacheDirectory, Jolyglot jolyglot) {
            if (cacheDirectory == null)
                throw new InvalidParameterException(Locale.REPOSITORY_DISK_ADAPTER_CAN_NOT_BE_NULL);

            if (jolyglot == null)
                throw new InvalidParameterException(Locale.JSON_CONVERTER_CAN_NOT_BE_NULL);

            this.cacheDirectory = cacheDirectory;
            this.jolyglot = jolyglot;

            return new RxCache(this);
        }

    }

}
