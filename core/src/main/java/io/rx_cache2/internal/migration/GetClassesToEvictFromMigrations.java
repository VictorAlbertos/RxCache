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
import java.util.List;
import javax.inject.Inject;

final class GetClassesToEvictFromMigrations {
  private List<io.rx_cache2.MigrationCache> migrations;

  @Inject public GetClassesToEvictFromMigrations() {
  }

  GetClassesToEvictFromMigrations with(List<io.rx_cache2.MigrationCache> migrations) {
    this.migrations = migrations;
    return this;
  }

  Observable<List<Class>> react() {
    List<Class> classesToEvict = new ArrayList<>();

    for (io.rx_cache2.MigrationCache migration : migrations) {
      for (Class candidate : migration.evictClasses()) {
        if (!isAlreadyAdded(classesToEvict, candidate)) classesToEvict.add(candidate);
      }
    }

    return Observable.just(classesToEvict);
  }

  private boolean isAlreadyAdded(List<Class> classesToEvict, Class candidate) {
    for (Class aClass : classesToEvict) {
      String className = aClass.getName();
      String classNameCandidate = candidate.getName();
      if (className.equals(classNameCandidate)) {
        return true;
      }
    }

    return false;
  }
}
