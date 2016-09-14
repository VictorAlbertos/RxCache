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

import javax.inject.Inject;

import io.rx_cache2.internal.Persistence;
import io.rx_cache2.internal.Record;
import io.rx_cache2.Source;
import io.rx_cache2.internal.Memory;

public final class RetrieveRecord extends Action {
  private final EvictRecord evictRecord;
  private final HasRecordExpired hasRecordExpired;
  private final String encryptKey;

  @Inject public RetrieveRecord(Memory memory, Persistence persistence, EvictRecord evictRecord,
      HasRecordExpired hasRecordExpired, String encryptKey) {
    super(memory, persistence);
    this.evictRecord = evictRecord;
    this.hasRecordExpired = hasRecordExpired;
    this.encryptKey = encryptKey;
  }

  <T> Record<T> retrieveRecord(String providerKey, String dynamicKey, String dynamicKeyGroup,
      boolean useExpiredDataIfLoaderNotAvailable, Long lifeTime, boolean isEncrypted) {
    String composedKey = composeKey(providerKey, dynamicKey, dynamicKeyGroup);

    Record<T> record = memory.getIfPresent(composedKey);

    if (record != null) {
      record.setSource(Source.MEMORY);
    } else {
      try {
        record = persistence.retrieveRecord(composedKey, isEncrypted, encryptKey);
        record.setSource(Source.PERSISTENCE);
        memory.put(composedKey, record);
      } catch (Exception ignore) {
        return null;
      }
    }

    record.setLifeTime(lifeTime);

    if (hasRecordExpired.hasRecordExpired(record)) {
      if (!dynamicKeyGroup.isEmpty()) {
        evictRecord.evictRecordMatchingDynamicKeyGroup(providerKey, dynamicKey,
            dynamicKeyGroup);
      } else if (!dynamicKey.isEmpty()) {
        evictRecord.evictRecordsMatchingDynamicKey(providerKey, dynamicKey);
      } else {
        evictRecord.evictRecordsMatchingProviderKey(providerKey);
      }

      return useExpiredDataIfLoaderNotAvailable ? record : null;
    }

    return record;
  }
}
