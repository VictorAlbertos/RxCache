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

import java.util.List;

/**
 * Provides the persistence layer for the cache
 * A default implementation which store the objects in disk is supplied:
 * @see io.rx_cache.internal.Disk
 */
public interface Persistence {

    /**
     * 基于某种数据持久化保存数据
     * @param 数据的键
     * @param 需要保存的数据
     */
    void save(String key, Object object);


    /**
     * 持久化保存纪录
     * @param key The key associated with the record to be persisted
     * @param record The record to be persisted
     */
    void saveRecord(String key, Record record);

    /**
     * 通过指定键删除已保存的持久化数据
     * @param 被持久化对象的键
     */
    void evict(String key);

    /**
     * 删除所有数据
     */
    void evictAll();

    /**
     * 检索所有数据
     */
    List<String> allKeys();

    /**
     * 持久化数据文件的大小，单位MB
     */
    int storedMB();

    /**
     * 根据特定key检索数据
     * @param <T> 返回数据的class
     * @param key 指定key
     * @see Record
     */
    <T> T retrieve(String key, Class<T> clazz);

    /**
     * 更加特殊key检索纪录
     * @param <T> The actual data to be persisted encapsulated inside a Record object
     * @param key The key associated with the Record to be retrieved from persistence
     * @see Record
     */
    <T> Record<T> retrieveRecord(String key);
}
