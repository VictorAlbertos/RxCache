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

package io.rx_cache2.internal.migration;

import io.reactivex.Observable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;

final class GetPendingMigrations {
  private int cacheVersion;
  private List<io.rx_cache2.MigrationCache> migrations;

  @Inject GetPendingMigrations() {
  }

  GetPendingMigrations with(int currentCacheVersion, List<io.rx_cache2.MigrationCache> migrations) {
    this.cacheVersion = currentCacheVersion;
    this.migrations = migrations;
    return this;
  }

  public Observable<List<io.rx_cache2.MigrationCache>> react() {
    if (migrations == null || migrations.isEmpty()) {
      return Observable.just((List<io.rx_cache2.MigrationCache>) new ArrayList<io.rx_cache2.MigrationCache>());
    }

    Collections.sort(migrations, new Comparator<io.rx_cache2.MigrationCache>() {
      @Override public int compare(
          io.rx_cache2.MigrationCache migration1, io.rx_cache2.MigrationCache migration2) {
        return migration1.version() - migration2.version();
      }
    });

    List<io.rx_cache2.MigrationCache> pendingMigrations = new ArrayList<>();

    for (io.rx_cache2.MigrationCache migration : migrations) {
      if (cacheVersion < migration.version()) {
        pendingMigrations.add(migration);
      }
    }

    return Observable.just(pendingMigrations);
  }
}
