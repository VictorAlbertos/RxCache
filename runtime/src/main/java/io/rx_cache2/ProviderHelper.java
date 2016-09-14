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

package io.rx_cache2;

import io.reactivex.Observable;

/**
 * Helper class to build an Observable to evict all the data associated with a provider.
 */
public class ProviderHelper {
  /**
   Create an Observable to provide a placeholder to
   #1 obtain the cached data when the source loader is not available
   #2 evict all the data associated with a provider
   */
  public static <T> Observable<T> withoutLoader() {
    return Observable.error(new RuntimeException());
  }
}
