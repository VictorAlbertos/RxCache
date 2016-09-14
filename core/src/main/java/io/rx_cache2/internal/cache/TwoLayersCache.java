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

package io.rx_cache2.internal.cache;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.rx_cache2.internal.Record;

@Singleton
public final class TwoLayersCache {
  private final EvictRecord evictRecord;
  private final io.rx_cache2.internal.cache.RetrieveRecord retrieveRecord;
  private final SaveRecord saveRecord;

  @Inject public TwoLayersCache(EvictRecord evictRecord, io.rx_cache2.internal.cache.RetrieveRecord retrieveRecord,
      SaveRecord saveRecord) {
    this.evictRecord = evictRecord;
    this.retrieveRecord = retrieveRecord;
    this.saveRecord = saveRecord;
  }

  public <T> Record<T> retrieve(String providerKey, String dynamicKey, String dynamicKeyGroup,
      boolean useExpiredDataIfLoaderNotAvailable, Long lifeTime, boolean isEncrypted) {
    return retrieveRecord.retrieveRecord(providerKey, dynamicKey, dynamicKeyGroup,
        useExpiredDataIfLoaderNotAvailable, lifeTime, isEncrypted);
  }

  public void save(String providerKey, String dynamicKey, String dynamicKeyGroup, Object data,
      Long lifeTime, boolean isExpirable, boolean isEncrypted) {
    saveRecord.save(providerKey, dynamicKey, dynamicKeyGroup, data, lifeTime, isExpirable,
        isEncrypted);
  }

  public void evictProviderKey(final String providerKey) {
    evictRecord.evictRecordsMatchingProviderKey(providerKey);
  }

  public void evictDynamicKey(String providerKey, String dynamicKey) {
    evictRecord.evictRecordsMatchingDynamicKey(providerKey, dynamicKey);
  }

  public void evictDynamicKeyGroup(String key, String dynamicKey, String dynamicKeyGroup) {
    evictRecord.evictRecordMatchingDynamicKeyGroup(key, dynamicKey, dynamicKeyGroup);
  }

  public void evictAll() {
    evictRecord.evictAll();
  }

  //Exists for testing purposes
  public void mockMemoryDestroyed() {
    evictRecord.mockMemoryDestroyed();
  }
}
