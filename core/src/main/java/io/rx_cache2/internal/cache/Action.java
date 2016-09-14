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

import java.util.ArrayList;
import java.util.List;

import io.rx_cache2.internal.Persistence;
import io.rx_cache2.internal.Memory;

abstract class Action {
  private static final String PREFIX_DYNAMIC_KEY = "$d$d$d$";
  private static final String PREFIX_DYNAMIC_KEY_GROUP = "$g$g$g$";

  protected final Memory memory;
  protected final Persistence persistence;

  public Action(Memory memory, Persistence persistence) {
    this.memory = memory;
    this.persistence = persistence;
  }

  protected String composeKey(String providerKey, String dynamicKey, String dynamicKeyGroup) {
    return providerKey
        + PREFIX_DYNAMIC_KEY
        + dynamicKey
        + PREFIX_DYNAMIC_KEY_GROUP
        + dynamicKeyGroup;
  }

  protected List<String> getKeysOnMemoryMatchingProviderKey(String providerKey) {
    List<String> keysMatchingProviderKey = new ArrayList<>();

    for (String composedKeyMemory : memory.keySet()) {
      final String keyPartProviderMemory =
          composedKeyMemory.substring(0, composedKeyMemory.lastIndexOf(PREFIX_DYNAMIC_KEY));

      if (providerKey.equals(keyPartProviderMemory)) {
        keysMatchingProviderKey.add(composedKeyMemory);
      }
    }

    return keysMatchingProviderKey;
  }

  protected List<String> getKeysOnMemoryMatchingDynamicKey(String providerKey, String dynamicKey) {
    List<String> keysMatchingDynamicKey = new ArrayList<>();

    String composedProviderKeyAndDynamicKey = providerKey + PREFIX_DYNAMIC_KEY + dynamicKey;

    for (String composedKeyMemory : memory.keySet()) {
      final String keyPartProviderAndDynamicKeyMemory =
          composedKeyMemory.substring(0, composedKeyMemory.lastIndexOf(PREFIX_DYNAMIC_KEY_GROUP));

      if (composedProviderKeyAndDynamicKey.equals(keyPartProviderAndDynamicKeyMemory)) {
        keysMatchingDynamicKey.add(composedKeyMemory);
      }
    }

    return keysMatchingDynamicKey;
  }

  protected String getKeyOnMemoryMatchingDynamicKeyGroup(String providerKey, String dynamicKey,
      String dynamicKeyGroup) {
    return composeKey(providerKey, dynamicKey, dynamicKeyGroup);
  }
}
