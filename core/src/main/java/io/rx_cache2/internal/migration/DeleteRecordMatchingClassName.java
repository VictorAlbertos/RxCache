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
import java.util.List;
import javax.inject.Inject;

public final class DeleteRecordMatchingClassName {
  private final io.rx_cache2.internal.Persistence persistence;
  private final String encryptKey;
  private List<Class> classes;

  @Inject public DeleteRecordMatchingClassName(io.rx_cache2.internal.Persistence persistence, String encryptKey) {
    this.persistence = persistence;
    this.encryptKey = encryptKey;
  }

  public DeleteRecordMatchingClassName with(List<Class> classes) {
    this.classes = classes;
    return this;
  }

  public Observable<Integer> react() {
    if (classes.isEmpty()) return Observable.just(1);

    List<String> allKeys = persistence.allKeys();

    for (String key : allKeys) {
      io.rx_cache2.internal.Record record = persistence.retrieveRecord(key, false, encryptKey);

      if (record == null) {
        record = persistence.retrieveRecord(key, true, encryptKey);
      }

      if (evictRecord(record)) {
        persistence.evict(key);
      }
    }

    return Observable.just(1);
  }

  private boolean evictRecord(io.rx_cache2.internal.Record record) {
    String candidate = record.getDataClassName();

    for (Class aClass : classes) {
      String className = aClass.getName();
      if (className.equals(candidate)) {
        return true;
      }
    }

    return false;
  }
}
