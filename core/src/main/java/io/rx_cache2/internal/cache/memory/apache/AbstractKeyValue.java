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
 * Abstract pair class to assist with creating <code>KeyValue</code>
 * and {@link java.util.Map.Entry Map.Entry} implementations.
 *
 * @since 3.0
 * @version $Id: AbstractKeyValue.java 1477753 2013-04-30 18:24:24Z tn $
 */
public abstract class AbstractKeyValue<K, V> implements KeyValue<K, V> {

    /** The key */
    private K key;
    /** The value */
    private V value;

    /**
     * Constructs a new pair with the specified key and given value.
     *
     * @param key  the key for the entry, may be null
     * @param value  the value for the entry, may be null
     */
    protected AbstractKeyValue(final K key, final V value) {
        super();
        this.key = key;
        this.value = value;
    }

    /**
     * Gets the key from the pair.
     *
     * @return the key
     */
    public K getKey() {
        return key;
    }

    protected K setKey(K key) {
        final K old = this.key;
        this.key = key;
        return old;
    }

    /**
     * Gets the value from the pair.
     *
     * @return the value
     */
    public V getValue() {
        return value;
    }

    protected V setValue(V value) {
        final V old = this.value;
        this.value = value;
        return old;
    }

    /**
     * Gets a debugging String view of the pair.
     *
     * @return a String view of the entry
     */
    @Override
    public String toString() {
        return new StringBuilder()
                .append(getKey())
                .append('=')
                .append(getValue())
                .toString();
    }

}
