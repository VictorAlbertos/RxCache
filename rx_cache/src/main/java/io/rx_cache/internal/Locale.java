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

package io.rx_cache.internal;

import io.rx_cache.Loader;

interface Locale {
    String NOT_OBSERVABLE_LOADER_FOUND = " requires an observable as source Loader annotated with : " + Loader.class.getName();
    String NOT_MORE_THAN_ONE_ANNOTATION_TYPE = "More than one parameter contains annotation " ;
    String INVALID_RETURN_TYPE = " needs to return an Observable<T> or Observable<Reply<T>>" ;
    String NOT_DATA_RETURN_WHEN_CALLING_OBSERVABLE_LOADER = "The Loader provided did not return any data and there is not data to load from the Cache";
    String REPOSITORY_DISK_ADAPTER_CAN_NOT_BE_NULL = "File cache directory can not be null";
    String PERSISTENCE_CAN_NOT_BE_NULL = "Persistence can not be null";
    String NOT_KEY_FOUND = "Key can not be null non empty";
}
