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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import io.rx_cache.Migration;
import io.rx_cache.SchemeMigration;
import rx.Observable;

final class GetPendingMigrations {
    private final Class providersClass;
    private int cacheVersion;

    @Inject GetPendingMigrations(Class providersClass) {
        this.providersClass = providersClass;
    }

    GetPendingMigrations with(int currentCacheVersion) {
        this.cacheVersion = currentCacheVersion;
        return this;
    }

    public Observable<List<Migration>> react() {
        List<Migration> migrations = new ArrayList<>();
        Annotation annotation = providersClass.getAnnotation(SchemeMigration.class);

        if (annotation == null) return Observable.just(migrations);

        SchemeMigration schemeMigration = (SchemeMigration) annotation;
        migrations = Arrays.asList(schemeMigration.value());

        Collections.sort(migrations, new Comparator<Migration>() {
            @Override public int compare(Migration migration1, Migration migration2) {
                return migration1.version() - migration2.version();
            }
        });

        List<Migration> pendingMigrations = new ArrayList<>();

        for (Migration migration : migrations) {
            if (cacheVersion < migration.version())
                pendingMigrations.add(migration);
        }

        return Observable.just(pendingMigrations);
    }

}
