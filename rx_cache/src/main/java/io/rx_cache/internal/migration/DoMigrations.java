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

import java.util.List;

import javax.inject.Inject;

import io.rx_cache.Migration;
import rx.Observable;
import rx.functions.Func1;

public class DoMigrations {
    private final GetCacheVersion getCacheVersion;
    private final GetPendingMigrations getPendingMigrations;
    private final GetClassesToEvictFromMigrations getClassesToEvictFromMigrations;
    private final UpgradeCacheVersion upgradeCacheVersion;

    @Inject public DoMigrations(GetPendingMigrations getPendingMigrations, GetCacheVersion getCacheVersion, GetClassesToEvictFromMigrations getClassesToEvictFromMigrations, UpgradeCacheVersion upgradeCacheVersion) {
        this.getPendingMigrations = getPendingMigrations;
        this.getCacheVersion = getCacheVersion;
        this.getClassesToEvictFromMigrations = getClassesToEvictFromMigrations;
        this.upgradeCacheVersion = upgradeCacheVersion;
    }

    Observable<Void> react() {
        getCacheVersion.react().flatMap(new Func1<Integer, Observable<? extends List<Migration>>>() {
            @Override public Observable<? extends List<Migration>> call(Integer currentCacheVersion) {
                return getPendingMigrations.with(currentCacheVersion).react();
            }
        }).flatMap(new Func1<List<Migration>, Observable<? extends List<Class>>>() {
            @Override public Observable<? extends List<Class>> call(List<Migration> migrations) {
                return getClassesToEvictFromMigrations.with(migrations).react();
            }
        });


        return null;
    }
}
