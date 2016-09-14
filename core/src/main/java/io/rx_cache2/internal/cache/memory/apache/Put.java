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

import java.util.Map;

/**
 * The "write" subset of the {@link Map} interface.
 * <p>
 * NOTE: in the original {@link Map} interface, {@link Map#put(Object, Object)} is known
 * to have the same return type as {@link Map#get(Object)}, namely {@code V}. {@link Put}
 * makes no assumptions in this regard (there is no association with, nor even knowledge
 * of, a "reading" interface) and thus defines {@link #put(Object, Object)} as returning
 * {@link Object}.
 *
 * @since 4.0
 * @version $Id: Put.java 1543257 2013-11-19 00:45:55Z ggregory $
 *
 * @see Get
 */
public interface Put<K, V> {

    /**
     * @see Map#clear()
     */
    void clear();

    /**
     * Note that the return type is Object, rather than V as in the Map interface.
     * See the class Javadoc for further info.
     *
     * @see Map#put(Object, Object)
     */
    Object put(K key, V value);

    /**
     * @see Map#putAll(Map)
     */
    void putAll(Map<? extends K, ? extends V> t);

}
