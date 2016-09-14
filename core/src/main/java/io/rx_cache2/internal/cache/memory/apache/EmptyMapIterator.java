/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rx_cache2.internal.cache.memory.apache;

/**
 * Provides an implementation of an empty map iterator.
 *
 * @since 3.1
 * @version $Id: EmptyMapIterator.java 1543955 2013-11-20 21:23:53Z tn $
 */
public class EmptyMapIterator<K, V> extends AbstractEmptyMapIterator<K, V> implements
    io.rx_cache2.internal.cache.memory.apache.MapIterator<K, V>,
    io.rx_cache2.internal.cache.memory.apache.ResettableIterator<K> {

    /**
     * Singleton instance of the iterator.
     * @since 3.1
     */
    @SuppressWarnings("rawtypes")
    public static final io.rx_cache2.internal.cache.memory.apache.MapIterator INSTANCE = new EmptyMapIterator<Object, Object>();

    /**
     * Get a typed instance of the iterator.
     * @param <K> the key type
     * @param <V> the value type
     * @return {@link io.rx_cache2.internal.cache.memory.apache.MapIterator}<K, V>
     */
    @SuppressWarnings("unchecked")
    public static <K, V> io.rx_cache2.internal.cache.memory.apache.MapIterator<K, V> emptyMapIterator() {
        return (io.rx_cache2.internal.cache.memory.apache.MapIterator<K, V>) INSTANCE;
    }

    /**
     * Constructor.
     */
    protected EmptyMapIterator() {
        super();
    }

}
