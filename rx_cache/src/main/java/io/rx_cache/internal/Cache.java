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


import net.tribe7.common.annotations.VisibleForTesting;
import net.tribe7.common.cache.CacheBuilder;
import net.tribe7.common.cache.CacheLoader;
import net.tribe7.common.cache.LoadingCache;
import net.tribe7.common.cache.RemovalCause;
import net.tribe7.common.cache.RemovalListener;
import net.tribe7.common.cache.RemovalNotification;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.rx_cache.Persistence;
import io.rx_cache.PolicyHeapCache;
import io.rx_cache.Record;
import io.rx_cache.Source;

@Singleton
final class Cache {
    private static final String PREFIX_DYNAMIC_KEY = "$_$_$";
    private final PolicyHeapCache policyHeapCache;
    private final LoadingCache<String, Record> records;
    private final Persistence persistence;
    private boolean mockMemoryDestroyed;

    @Inject public Cache(PolicyHeapCache policyHeapCache, Persistence persistence) {
        this.policyHeapCache = policyHeapCache;
        this.persistence = persistence;
        this.records = initLoadingCache();
    }

    private LoadingCache<String, Record> initLoadingCache() {
        return CacheBuilder.newBuilder()
                .maximumSize(maxCacheSizeBytes())
                .removalListener(new RemovalListener<String, Record>() {
                    @Override public void onRemoval(RemovalNotification<String, Record> notification) {
                        if (mockMemoryDestroyed) return;

                        if (notification.getCause() == RemovalCause.EXPLICIT ||
                                notification.getCause() == RemovalCause.REPLACED) {
                            persistence.delete(notification.getKey());
                        }
                    }
                }).build(new CacheLoader<String, Record>() {
                    public Record load(String key) throws Exception {
                        return persistence.retrieveRecord(key);
                    }
                });
    }

    <T> Record<T> retrieve(String key, String dynamicKey, boolean useExpiredDataIfLoaderNotAvailable) {
        key = key + PREFIX_DYNAMIC_KEY + dynamicKey;

        Record<T> record = records.getIfPresent(key);

        if (record != null) {
            record.setSource(Source.MEMORY);
        } else {
            try {
                record = records.getUnchecked(key);
                record.setSource(Source.PERSISTENCE);
            } catch (Exception ignore) {
                return null;
            }
        }

        long now = System.currentTimeMillis();
        long expiration = record.getExpirationDate();
        if (expiration != 0 && now > expiration) {
            clear(key);
            return useExpiredDataIfLoaderNotAvailable ? record : null;
        }

        return record;
    }


    void save(String key, String dynamicKey, Object data, long lifeTimeMilli) {
        key = key + PREFIX_DYNAMIC_KEY + dynamicKey;
        long expirationDate = lifeTimeMilli == 0 ? 0 : System.currentTimeMillis() + lifeTimeMilli;
        Record record = new Record(data, expirationDate);
        records.put(key, record);
        persistence.saveRecord(key, record);
    }

    void clear(final String key) {
        for (String composedKeyRecord : records.asMap().keySet()) {
            final String keyRecord = composedKeyRecord.substring(0, composedKeyRecord.lastIndexOf(PREFIX_DYNAMIC_KEY));
            if (key.equals(keyRecord)) records.invalidate(composedKeyRecord);
        }
    }

    void clearDynamicKey(String key, String dynamicKey) {
        key = key + PREFIX_DYNAMIC_KEY + dynamicKey;
        records.invalidate(key);
    }

    void clearAll() {
        records.invalidateAll();
    }

    @VisibleForTesting void mockMemoryDestroyed() {
        mockMemoryDestroyed = true;
        clearAll();
        mockMemoryDestroyed = false;
    }

    @VisibleForTesting long maxCacheSizeBytes() {
        long amountMemoryBytes  = Runtime.getRuntime().totalMemory();
        return (long) (amountMemoryBytes * policyHeapCache.getPercentageReserved());
    }
}
