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
import io.rx_cache.internal.Persistence;
import rx.Observable;
import rx.functions.Func1;

public final class DoMigrations {
    private final GetCacheVersion getCacheVersion;
    private final GetPendingMigrations getPendingMigrations;
    private final GetClassesToEvictFromMigrations getClassesToEvictFromMigrations;
    private final DeleteRecordMatchingClassName deleteRecordMatchingClassName;
    private final UpgradeCacheVersion upgradeCacheVersion;

    @Inject public DoMigrations(Persistence persistence, Class providerClass) {
        this.getCacheVersion = new GetCacheVersion(persistence);
        this.getPendingMigrations = new GetPendingMigrations(providerClass);
        this.getClassesToEvictFromMigrations = new GetClassesToEvictFromMigrations();
        this.deleteRecordMatchingClassName = new DeleteRecordMatchingClassName(persistence);
        this.upgradeCacheVersion = new UpgradeCacheVersion(persistence);
    }

    private List<Migration> migrations;

    public Observable<Void> react() {
        return getCacheVersion.react().flatMap(new Func1<Integer, Observable<? extends List<Migration>>>() {
            @Override
            public Observable<? extends List<Migration>> call(Integer currentCacheVersion) {
                return getPendingMigrations.with(currentCacheVersion).react();
            }
        }).flatMap(new Func1<List<Migration>, Observable<? extends List<Class>>>() {
            @Override public Observable<? extends List<Class>> call(List<Migration> migrations) {
                DoMigrations.this.migrations = migrations;
                return getClassesToEvictFromMigrations.with(migrations).react();
            }
        }).flatMap(new Func1<List<Class>, Observable<? extends Void>>() {
            @Override public Observable<? extends Void> call(List<Class> classes) {
                return deleteRecordMatchingClassName.with(classes).react();
            }
        }).flatMap(new Func1<Object, Observable<? extends Void>>() {
            @Override public Observable<? extends Void> call(Object o) {
                return upgradeCacheVersion.with(migrations).react();
            }
        });
    }
}
