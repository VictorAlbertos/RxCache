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

package io.rx_cache2.internal;

import io.reactivex.Observable;
import io.victoralbertos.jolyglot.JolyglotGenerics;
import java.io.File;
import java.lang.reflect.Proxy;
import java.security.InvalidParameterException;

public final class RxCache {
  private final Builder builder;
  private ProxyProviders proxyProviders;

  private RxCache(Builder builder) {
    this.builder = builder;
  }

  public <T> T using(final Class<T> classProviders) {
    proxyProviders = new ProxyProviders(builder, classProviders);

    return (T) Proxy.newProxyInstance(
        classProviders.getClassLoader(),
        new Class<?>[] {classProviders},
        proxyProviders);
  }

  public Observable<Void> evictAll() {
    return proxyProviders.evictAll();
  }

  /**
   * Builder for building an specific RxCache instance
   */
  public static class Builder {
    private boolean useExpiredDataIfLoaderNotAvailable;
    private Integer maxMBPersistenceCache;
    private File cacheDirectory;
    private JolyglotGenerics jolyglot;

    /**
     * If true RxCache will serve Records already expired, instead of evict them and throw an
     * exception If not supplied, false will be the default option
     *
     * @return BuilderRxCache The builder of RxCache
     */
    public Builder useExpiredDataIfLoaderNotAvailable(boolean useExpiredDataIfLoaderNotAvailable) {
      this.useExpiredDataIfLoaderNotAvailable = useExpiredDataIfLoaderNotAvailable;
      return this;
    }

    /**
     * Sets the max memory in megabytes for all stored records on persistence layer If not supplied,
     * 100 megabytes will be the default option
     *
     * @return BuilderRxCache The builder of RxCache
     */
    public Builder setMaxMBPersistenceCache(Integer maxMgPersistenceCache) {
      this.maxMBPersistenceCache = maxMgPersistenceCache;
      return this;
    }

    /**
     * Sets the File cache system and the implementation of {@link JolyglotGenerics} to serialise
     * and deserialize objects
     *
     * @param cacheDirectory The File system used by the persistence implementation of Disk
     * @param jolyglot A concrete implementation of {@link JolyglotGenerics}
     */
    public RxCache persistence(File cacheDirectory, JolyglotGenerics jolyglot) {
      if (cacheDirectory == null) {
        throw new InvalidParameterException(io.rx_cache2.internal.Locale.REPOSITORY_DISK_ADAPTER_CAN_NOT_BE_NULL);
      }
      if (!cacheDirectory.exists()) {
        throw new InvalidParameterException(io.rx_cache2.internal.Locale.REPOSITORY_DISK_ADAPTER_DOES_NOT_EXIST);
      }
      if (!cacheDirectory.canWrite()) {
        throw new InvalidParameterException(io.rx_cache2.internal.Locale.REPOSITORY_DISK_ADAPTER_IS_NOT_WRITABLE);
      }

      if (jolyglot == null) {
        throw new InvalidParameterException(io.rx_cache2.internal.Locale.JSON_CONVERTER_CAN_NOT_BE_NULL);
      }

      this.cacheDirectory = cacheDirectory;
      this.jolyglot = jolyglot;

      return new RxCache(this);
    }

    public boolean useExpiredDataIfLoaderNotAvailable() {
      return useExpiredDataIfLoaderNotAvailable;
    }

    public Integer getMaxMBPersistenceCache() {
      return maxMBPersistenceCache;
    }

    public File getCacheDirectory() {
      return cacheDirectory;
    }

    public JolyglotGenerics getJolyglot() {
      return jolyglot;
    }
  }
}
