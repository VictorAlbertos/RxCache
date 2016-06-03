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

package io.rx_cache.internal.migration;


import java.util.List;

import javax.inject.Inject;

import io.rx_cache.internal.Persistence;
import io.rx_cache.internal.Record;
import io.rx_cache.internal.encrypt.GetEncryptKey;
import rx.Observable;

public final class DeleteRecordMatchingClassName {
    private final Persistence persistence;
    private final GetEncryptKey getEncryptKey;
    private List<Class> classes;

    @Inject public DeleteRecordMatchingClassName(Persistence persistence, GetEncryptKey getEncryptKey) {
        this.persistence = persistence;
        this.getEncryptKey = getEncryptKey;
    }

    public DeleteRecordMatchingClassName with(List<Class> classes) {
        this.classes = classes;
        return this;
    }

    public Observable<Void> react() {
        if (classes.isEmpty()) return Observable.just(null);

        List<String> allKeys = persistence.allKeys();

        for (String key : allKeys) {
            Record record = persistence.retrieveRecord(key, false, getEncryptKey.getKey());

            if (record == null) {
                record = persistence.retrieveRecord(key, true, getEncryptKey.getKey());
            }

            if (evictRecord(record)) {
                persistence.evict(key);
            }
        }

        return Observable.just(null);
    }

    private boolean evictRecord(Record record) {
        String candidate = record.getDataClassName();

        for (Class aClass : classes) {
            String className = aClass.getName();
            if (className.equals(candidate)) {
                return true;
            }
        }

        return false;
    }
}
