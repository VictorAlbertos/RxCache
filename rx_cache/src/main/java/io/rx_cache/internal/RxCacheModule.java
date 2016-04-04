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

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.rx_cache.*;

@Module
public final class RxCacheModule {
    private final File cacheDirectory;
    private final PolicyHeapCache policyHeapCache;
    private final boolean useExpiredDataIfLoaderNotAvailable;
    private final Integer maxMgPersistenceCache;
    private final Class classProviders;

    public RxCacheModule(File cacheDirectory, PolicyHeapCache policyHeapCache, Boolean useExpiredDataIfLoaderNotAvailable, Integer maxMgPersistenceCache, Class classProviders) {
        this.cacheDirectory = cacheDirectory;
        this.policyHeapCache = policyHeapCache;
        this.useExpiredDataIfLoaderNotAvailable = useExpiredDataIfLoaderNotAvailable;
        this.maxMgPersistenceCache = maxMgPersistenceCache;
        this.classProviders = classProviders;
    }

    @Singleton @Provides File provideCacheDirectory() {
        return cacheDirectory;
    }

    @Singleton @Provides PolicyHeapCache providePolicyCache() {
        return policyHeapCache;
    }

    @Singleton @Provides Persistence providePersistence(Disk disk) {
        return disk;
    }

    @Singleton @Provides Boolean useExpiredDataIfLoaderNotAvailable() {
        return useExpiredDataIfLoaderNotAvailable;
    }

    @Singleton @Provides Memory provideMemory(PolicyHeapCache policyHeapCache) {
        try {
            Class.forName("com.google.common.cache.Cache");
            return new GuavaMemory(policyHeapCache);
        } catch( ClassNotFoundException e ) {
            return new SimpleMemory();
        }
    }

    @Singleton @Provides Integer maxMbPersistenceCache() {
        return maxMgPersistenceCache != null ? maxMgPersistenceCache : 100;
    }

    @Singleton @Provides Class provideClassProviders() {
        return classProviders;
    }
}
