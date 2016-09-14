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

package io.rx_cache2.internal.cache;

import io.reactivex.Observable;
import io.rx_cache2.internal.Memory;
import io.rx_cache2.internal.Persistence;
import io.rx_cache2.internal.Record;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class EvictExpiredRecordsPersistence extends Action {
  private final HasRecordExpired hasRecordExpired;
  private final String encryptKey;

  @Inject public EvictExpiredRecordsPersistence(Memory memory, Persistence persistence,
      HasRecordExpired hasRecordExpired, String encryptKey) {
    super(memory, persistence);
    this.hasRecordExpired = hasRecordExpired;
    this.encryptKey = encryptKey;
  }

  public Observable<Integer> startEvictingExpiredRecords() {
    List<String> allKeys = persistence.allKeys();

    for (String key : allKeys) {
      Record record = persistence.retrieveRecord(key, false, encryptKey);

      if (record == null && encryptKey != null && !encryptKey.isEmpty()) {
        record = persistence.retrieveRecord(key, true, encryptKey);
      }

      if (record != null && hasRecordExpired.hasRecordExpired(record)) {
        persistence.evict(key);
      }
    }

    return Observable.just(1);
  }
}
