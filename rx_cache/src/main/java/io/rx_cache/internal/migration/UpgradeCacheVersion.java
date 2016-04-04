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


import javax.inject.Inject;

import io.rx_cache.internal.Persistence;
import rx.Observable;

final class UpgradeCacheVersion extends CacheVersion {
    private int newVersion;

    @Inject public UpgradeCacheVersion(Persistence persistence) {
        super(persistence);
    }

    UpgradeCacheVersion with(int newVersion) {
        this.newVersion = newVersion;
        return this;
    }

    Observable<Void> react() {
        persistence.save(KEY_CACHE_VERSION, newVersion);
        return Observable.empty();
    }
}
