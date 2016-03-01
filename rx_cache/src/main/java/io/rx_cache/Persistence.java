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

package io.rx_cache;

/**
 * Provides the persistence layer for the cache
 * A default implementation which store the objects in disk is supplied:
 * @see io.rx_cache.internal.Disk
 */
public interface Persistence {

    /**
     * Save the data supplied based on a certain mechanism which provides persistence somehow
     * @param key The key associated with the record to be persisted
     * @param record The record to be persisted
     */
    void saveRecord(String key, Record record);

    /**
     * Delete the data associated with its particular key
     * @param key The key associated with the object to be deleted from persistence
     */
    void evict(String key);

    /**
     * Delete all the data
     */
    void evictAll();

    /**
     * Retrieve the record associated with its particular key
     * @param <T> The actual data to be persisted encapsulated inside a Record object
     * @param key The key associated with the Record to be retrieved from persistence
     * @see Record
     */
    <T> Record<T> retrieveRecord(String key);
}
