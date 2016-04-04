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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Set;

import io.rx_cache.PolicyHeapCache;
import io.rx_cache.Record;

public class GuavaMemory implements Memory {
    private final Cache<String, Record> records;

    public GuavaMemory(PolicyHeapCache policyHeapCache) {
        this.records = CacheBuilder.<String, Record>newBuilder()
                .maximumSize(maxCacheSizeBytes(policyHeapCache))
                .build();
    }

    public @VisibleForTesting long maxCacheSizeBytes(PolicyHeapCache policyHeapCache) {
        long amountMemoryBytes  = Runtime.getRuntime().totalMemory();
        return (long) (amountMemoryBytes * policyHeapCache.getPercentageReserved());
    }

    @Override public <T> Record<T> getIfPresent(String key) {
        return records.getIfPresent(key);
    }

    @Override public <T> void put(String key, Record<T> record) {
        records.put(key, record);
    }

    @Override public Set<String> keySet() {
        return records.asMap().keySet();
    }

    @Override public void evict(String key) {
        records.invalidate(key);
    }

    @Override public void evictAll() {
        records.invalidateAll();
    }
}
