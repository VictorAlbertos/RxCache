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

package io.rx_cache2.internal;

import java.util.List;

/**
 * Provides the persistence layer for the cache A default implementation which store the objects in
 * disk is supplied:
 *
 * @see Disk
 */
public interface Persistence {

  /**
   * Save the data supplied based on a certain mechanism which provides persistence somehow
   *
   * @param key The key associated with the object to be persisted
   * @param object The object to be persisted
   * @param isEncrypted If the persisted record is encrypted or not.
   * @param encryptKey The key used to encrypt/decrypt the record to be persisted. 
   */
  void save(String key, Object object, boolean isEncrypted, String encryptKey);

  /**
   * Save the data supplied based on a certain mechanism which provides persistence somehow
   *
   * @param key The key associated with the record to be persisted
   * @param record The record to be persisted
   * @param isEncrypted If the persisted record is encrypted or not.
   * @param encryptKey The key used to encrypt/decrypt the record to be persisted.
   */
  void saveRecord(String key, Record record, boolean isEncrypted, String encryptKey);

  /**
   * Delete the data associated with its particular key
   *
   * @param key The key associated with the object to be deleted from persistence
   */
  void evict(String key);

  /**
   * Delete all the data
   */
  void evictAll();

  /**
   * Retrieve the keys from all records persisted
   */
  List<String> allKeys();

  /**
   * Retrieve accumulated memory records in megabytes
   */
  int storedMB();

  /**
   * Retrieve the object associated with its particular key
   *
   * @param key The key associated with the object to be retrieved from persistence
   * @param <T> The data to be retrieved
   * @param isEncrypted If the persisted record is encrypted or not.
   * @param encryptKey The key used to encrypt/decrypt the record to be persisted.
   * @see Record
   */
  <T> T retrieve(String key, Class<T> clazz, boolean isEncrypted, String encryptKey);

  /**
   * Retrieve the record associated with its particular key
   *
   * @param key The key associated with the Record to be retrieved from persistence
   * @param isEncrypted If the persisted record is encrypted or not.
   * @param encryptKey The key used to encrypt/decrypt the record to be persisted.
   * @see Record
   */
  <T> Record<T> retrieveRecord(String key, boolean isEncrypted, String encryptKey);
}
