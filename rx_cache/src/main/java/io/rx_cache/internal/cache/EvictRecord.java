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

import com.google.common.annotations.VisibleForTesting;

import java.util.List;

import javax.inject.Inject;

import io.rx_cache.Persistence;
import io.rx_cache.internal.Memory;

public final class EvictRecord extends Action {

    @Inject public EvictRecord(Memory memory, Persistence persistence) {
        super(memory, persistence);
    }

    void evictRecordsMatchingProviderKey(String providerKey) {
        List<String> keysMatchingKeyProvider = getKeysMatchingProviderKey(providerKey);

        for (String keyMatchingKeyProvider : keysMatchingKeyProvider) {
            memory.evict(keyMatchingKeyProvider);
            persistence.delete(keyMatchingKeyProvider);
        }
    }

    void evictRecordsMatchingDynamicKey(String providerKey, String dynamicKey) {
        List<String> keysMatchingDynamicKey = getKeysMatchingDynamicKey(providerKey, dynamicKey);

        for (String keyMatchingDynamicKey : keysMatchingDynamicKey) {
            memory.evict(keyMatchingDynamicKey);
            persistence.delete(keyMatchingDynamicKey);
        }
    }

    void evictRecordDynamicKeyGroup(String providerKey, String dynamicKey, String dynamicKeyGroup) {
        String composedKey = getKeyMatchingDynamicKeyGroup(providerKey, dynamicKey, dynamicKeyGroup);

        memory.evict(composedKey);
        persistence.delete(composedKey);
    }

    @VisibleForTesting void mockMemoryDestroyed() {
        memory.evictAll();
    }

    void evictAll() {
        memory.evictAll();
        persistence.deleteAll();
    }
}
