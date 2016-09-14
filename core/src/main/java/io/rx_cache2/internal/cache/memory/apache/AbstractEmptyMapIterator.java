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
 * @since 4.0
 * @version $Id: AbstractEmptyMapIterator.java 1477802 2013-04-30 20:01:28Z tn $
 */
public abstract class AbstractEmptyMapIterator<K, V> extends AbstractEmptyIterator<K> {

    /**
     * Create a new AbstractEmptyMapIterator.
     */
    public AbstractEmptyMapIterator() {
        super();
    }

    public K getKey() {
        throw new IllegalStateException("Iterator contains no elements");
    }

    public V getValue() {
        throw new IllegalStateException("Iterator contains no elements");
    }

    public V setValue(final V value) {
        throw new IllegalStateException("Iterator contains no elements");
    }

}
