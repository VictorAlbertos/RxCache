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

import dagger.Module;
import dagger.Provides;
import io.rx_cache2.MigrationCache;
import io.rx_cache2.internal.cache.memory.ReferenceMapMemory;
import io.rx_cache2.internal.encrypt.BuiltInEncryptor;
import io.rx_cache2.internal.encrypt.Encryptor;
import io.victoralbertos.jolyglot.JolyglotGenerics;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Singleton;

@Module
public final class RxCacheModule {
  private final File cacheDirectory;
  private final boolean useExpiredDataIfLoaderNotAvailable;
  private final Integer maxMgPersistenceCache;
  private final String encryptKey;
  private final List<MigrationCache> migrations;
  private final JolyglotGenerics jolyglot;

  public RxCacheModule(File cacheDirectory, Boolean useExpiredDataIfLoaderNotAvailable,
      Integer maxMgPersistenceCache,
      String encryptKey, List<MigrationCache> migrations, JolyglotGenerics jolyglot) {
    this.cacheDirectory = cacheDirectory;
    this.useExpiredDataIfLoaderNotAvailable = useExpiredDataIfLoaderNotAvailable;
    this.maxMgPersistenceCache = maxMgPersistenceCache;
    this.encryptKey = encryptKey;
    this.migrations = migrations;
    this.jolyglot = jolyglot;
  }

  @Singleton @Provides File provideCacheDirectory() {
    return cacheDirectory;
  }

  @Singleton @Provides Persistence providePersistence(io.rx_cache2.internal.Disk disk) {
    return disk;
  }

  @Singleton @Provides Boolean useExpiredDataIfLoaderNotAvailable() {
    return useExpiredDataIfLoaderNotAvailable;
  }

  @Singleton @Provides io.rx_cache2.internal.Memory provideMemory() {
    return new ReferenceMapMemory();
  }

  @Singleton @Provides Integer maxMbPersistenceCache() {
    return maxMgPersistenceCache != null ? maxMgPersistenceCache : 100;
  }

  @Singleton @Provides Encryptor provideEncryptor() {
    return new BuiltInEncryptor();
  }

  @Singleton @Provides String provideEncryptKey() {
    return encryptKey != null ? encryptKey : "";
  }

  @Singleton @Provides List<MigrationCache> provideMigrations() {
    return migrations != null ? migrations : new ArrayList<MigrationCache>();
  }

  @Singleton @Provides JolyglotGenerics provideJolyglot() {
    return jolyglot;
  }

  @Provides io.rx_cache2.internal.ProcessorProviders provideProcessorProviders(
      io.rx_cache2.internal.ProcessorProvidersBehaviour processorProvidersBehaviour) {
    return processorProvidersBehaviour;
  }
}
