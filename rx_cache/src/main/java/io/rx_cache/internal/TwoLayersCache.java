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

import javax.inject.Inject;
import javax.inject.Singleton;

import io.rx_cache.Persistence;
import io.rx_cache.Record;
import io.rx_cache.Source;

@Singleton
final class TwoLayersCache {
    private static final String PREFIX_DYNAMIC_KEY = "$_$_$";
    private final Memory memory;
    private final Persistence persistence;

    @Inject public TwoLayersCache(Memory memory, Persistence persistence) {
        this.persistence = persistence;
        this.memory = memory;
    }

    <T> Record<T> retrieve(String key, String dynamicKey, boolean useExpiredDataIfLoaderNotAvailable, long lifeTime) {
        retrieveHasBeenCalled = true;

        key = key + PREFIX_DYNAMIC_KEY + dynamicKey;

        Record<T> record = memory.getIfPresent(key);

        if (record != null) {
            record.setSource(Source.MEMORY);
        } else {
            try {
                record = persistence.retrieveRecord(key);
                record.setSource(Source.PERSISTENCE);
                memory.put(key, record);
            } catch (Exception ignore) {
                return null;
            }
        }

        long now = System.currentTimeMillis();
        long expirationDate = record.getTimeAtWhichWasPersisted() + lifeTime;
        if (lifeTime != 0 && now > expirationDate) {
            clear(key);
            return useExpiredDataIfLoaderNotAvailable ? record : null;
        }

        return record;
    }

    void save(String key, String dynamicKey, Object data) {
        key = key + PREFIX_DYNAMIC_KEY + dynamicKey;
        Record record = new Record(data);
        memory.put(key, record);
        persistence.saveRecord(key, record);
    }

    void clear(final String key) {
        for (String composedKeyRecord : memory.keySet()) {
            final String keyRecord = composedKeyRecord.substring(0, composedKeyRecord.lastIndexOf(PREFIX_DYNAMIC_KEY));
            if (key.equals(keyRecord)) {
                memory.invalidate(composedKeyRecord);
                persistence.delete(composedKeyRecord);
            }
        }
    }

    void clearDynamicKey(String key, String dynamicKey) {
        key = key + PREFIX_DYNAMIC_KEY + dynamicKey;
        memory.invalidate(key);
        persistence.delete(key);
    }

    void clearAll() {
        memory.invalidateAll();
        persistence.deleteAll();
    }

    @VisibleForTesting void mockMemoryDestroyed() {
        memory.invalidateAll();
    }

    private boolean retrieveHasBeenCalled;
    @VisibleForTesting boolean retrieveHasBeenCalled() {
        return retrieveHasBeenCalled;
    }
}
