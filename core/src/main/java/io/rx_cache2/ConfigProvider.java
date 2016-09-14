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

package io.rx_cache2;

import io.reactivex.Observable;
import io.rx_cache2.internal.Locale;

public final class ConfigProvider {
  private final String providerKey;
  private final Boolean useExpiredDataIfNotLoaderAvailable;
  private final Long lifeTime;
  private final boolean requiredDetailedResponse;
  private final boolean expirable;
  private final boolean encrypted;
  private final String dynamicKey, dynamicKeyGroup;
  private final Observable loaderObservable;
  private final EvictProvider evictProvider;

  public ConfigProvider(String providerKey, Boolean useExpiredDataIfNotLoaderAvailable,
      Long lifeTime, boolean requiredDetailedResponse,
      boolean expirable, boolean encrypted, String dynamicKey, String dynamicKeyGroup,
      Observable loaderObservable, EvictProvider evictProvider) {
    this.providerKey = providerKey;
    this.useExpiredDataIfNotLoaderAvailable = useExpiredDataIfNotLoaderAvailable;
    this.lifeTime = lifeTime;
    this.requiredDetailedResponse = requiredDetailedResponse;
    this.expirable = expirable;
    this.encrypted = encrypted;
    this.dynamicKey = dynamicKey;
    this.dynamicKeyGroup = dynamicKeyGroup;
    this.loaderObservable = loaderObservable;
    this.evictProvider = evictProvider;
    checkIntegrity();
  }

  public String getProviderKey() {
    return providerKey;
  }

  public String getDynamicKey() {
    return dynamicKey;
  }

  public String getDynamicKeyGroup() {
    return dynamicKeyGroup;
  }

  public Long getLifeTimeMillis() {
    return lifeTime;
  }

  public boolean requiredDetailedResponse() {
    return requiredDetailedResponse;
  }

  public Observable getLoaderObservable() {
    return loaderObservable;
  }

  public EvictProvider evictProvider() {
    return evictProvider;
  }

  public boolean isExpirable() {
    return expirable;
  }

  public boolean isEncrypted() {
    return encrypted;
  }

  public Boolean useExpiredDataIfNotLoaderAvailable() {
    return useExpiredDataIfNotLoaderAvailable;
  }

  private void checkIntegrity() {
    if (evictProvider() instanceof io.rx_cache2.EvictDynamicKeyGroup
        && getDynamicKeyGroup().isEmpty()) {
      String errorMessage = providerKey
          + Locale.EVICT_DYNAMIC_KEY_GROUP_PROVIDED_BUT_NOT_PROVIDED_ANY_DYNAMIC_KEY_GROUP;
      throw new IllegalArgumentException(errorMessage);
    }

    if (evictProvider() instanceof io.rx_cache2.EvictDynamicKey
        && getDynamicKey().isEmpty()) {
      String errorMessage =
          providerKey + Locale.EVICT_DYNAMIC_KEY_PROVIDED_BUT_NOT_PROVIDED_ANY_DYNAMIC_KEY;
      throw new IllegalArgumentException(errorMessage);
    }
  }
}
