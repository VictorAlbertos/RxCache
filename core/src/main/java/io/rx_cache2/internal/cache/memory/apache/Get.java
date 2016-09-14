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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * The "read" subset of the {@link java.util.Map} interface.
 *
 * @since 4.0
 * @version $Id: Get.java 1543265 2013-11-19 00:48:44Z ggregory $
 *
 * @see Put
 */
public interface Get<K, V> {

    /**
     * @see java.util.Map#containsKey(Object)
     */
    boolean containsKey(Object key);

    /**
     * @see java.util.Map#containsValue(Object)
     */
    boolean containsValue(Object value);

    /**
     * @see java.util.Map#entrySet()
     */
    Set<Map.Entry<K, V>> entrySet();

    /**
     * @see java.util.Map#get(Object)
     */
    V get(Object key);

    /**
     * @see java.util.Map#remove(Object)
     */
    V remove(Object key);

    /**
     * @see java.util.Map#isEmpty()
     */
    boolean isEmpty();

    /**
     * @see java.util.Map#keySet()
     */
    Set<K> keySet();

    /**
     * @see java.util.Map#size()
     */
    int size();

    /**
     * @see java.util.Map#values()
     */
    Collection<V> values();

}
