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

package io.rx_cache2;

/**
 * A migration configuration.
 */
public final class MigrationCache {
  /**
   * Migration number version. The first migration should start with 1
   */
  private final int version;

  /**
   * Classes to be evicted due to inconsistency properties regarding prior migrations. It means when
   * a new field of a class has been added. Deleting classes or deleting fields of classes would be
   * handle automatically by RxCache.
   */
  private final Class[] evictClasses;

  public MigrationCache(int version, Class[] evictClasses) {
    this.version = version;
    this.evictClasses = evictClasses;
  }

  public int version() {
    return version;
  }

  public Class[] evictClasses() {
    return evictClasses;
  }
}
