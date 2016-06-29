/*
 * Copyright 2016 Victor Albertos
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

package io.rx_cache.internal.cache;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.rx_cache.internal.Memory;
import io.rx_cache.internal.Persistence;
import io.rx_cache.internal.Record;
import io.rx_cache.internal.encrypt.GetEncryptKey;
import rx.Observable;

@Singleton
public final class EvictExpiredRecordsPersistence extends Action {
    private final HasRecordExpired hasRecordExpired;
    private final GetEncryptKey getEncryptKey;

    @Inject public EvictExpiredRecordsPersistence(Memory memory, Persistence persistence, HasRecordExpired hasRecordExpired, GetEncryptKey getEncryptKey) {
        super(memory, persistence);
        this.hasRecordExpired = hasRecordExpired;
        this.getEncryptKey = getEncryptKey;
    }

    public Observable<Void> startEvictingExpiredRecords() {
        List<String> allKeys = persistence.allKeys();

        for (String key : allKeys) {
            Record record = persistence.retrieveRecord(key, false, getEncryptKey.getKey());

            if (record == null && getEncryptKey.getKey() != null) {
                record = persistence.retrieveRecord(key, true, getEncryptKey.getKey());
            }

            if (record != null && hasRecordExpired.hasRecordExpired(record)) {
                persistence.evict(key);
            }
        }

        return Observable.just(null);
    }
}
