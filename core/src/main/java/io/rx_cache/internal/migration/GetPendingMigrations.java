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

import io.rx_cache.MigrationCache;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;

final class GetPendingMigrations {
  private int cacheVersion;
  private List<MigrationCache> migrations;

  @Inject GetPendingMigrations() {
  }

  GetPendingMigrations with(int currentCacheVersion, List<MigrationCache> migrations) {
    this.cacheVersion = currentCacheVersion;
    this.migrations = migrations;
    return this;
  }

  public Observable<List<MigrationCache>> react() {
    if (migrations == null || migrations.isEmpty()) {
      return Observable.just((List<MigrationCache>) new ArrayList<MigrationCache>());
    }

    Collections.sort(migrations, new Comparator<MigrationCache>() {
      @Override public int compare(MigrationCache migration1, MigrationCache migration2) {
        return migration1.version() - migration2.version();
      }
    });

    List<MigrationCache> pendingMigrations = new ArrayList<>();

    for (MigrationCache migration : migrations) {
      if (cacheVersion < migration.version()) {
        pendingMigrations.add(migration);
      }
    }

    return Observable.just(pendingMigrations);
  }
}
