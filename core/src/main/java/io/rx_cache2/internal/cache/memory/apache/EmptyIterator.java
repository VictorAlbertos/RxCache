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

import java.util.Iterator;

/**
 * Provides an implementation of an empty iterator.
 * <p>
 * This class provides an implementation of an empty iterator.
 * This class provides for binary compatibility between Commons Collections
 * 2.1.1 and 3.1 due to issues with <code>IteratorUtils</code>.
 *
 * @since 2.1.1 and 3.1
 * @version $Id: EmptyIterator.java 1543955 2013-11-20 21:23:53Z tn $
 */
public class EmptyIterator<E> extends AbstractEmptyIterator<E> implements ResettableIterator<E> {

    /**
     * Singleton instance of the iterator.
     * @since 3.1
     */
    @SuppressWarnings("rawtypes")
    public static final ResettableIterator RESETTABLE_INSTANCE = new EmptyIterator<Object>();

    /**
     * Singleton instance of the iterator.
     * @since 2.1.1 and 3.1
     */
    @SuppressWarnings("rawtypes")
    public static final Iterator INSTANCE = RESETTABLE_INSTANCE;

    /**
     * Get a typed resettable empty iterator instance.
     * @param <E> the element type
     * @return ResettableIterator<E>
     */
    @SuppressWarnings("unchecked")
    public static <E> ResettableIterator<E> resettableEmptyIterator() {
        return (ResettableIterator<E>) RESETTABLE_INSTANCE;
    }

    /**
     * Get a typed empty iterator instance.
     * @param <E> the element type
     * @return Iterator<E>
     */
    @SuppressWarnings("unchecked")
    public static <E> Iterator<E> emptyIterator() {
        return (Iterator<E>) INSTANCE;
    }

    /**
     * Constructor.
     */
    protected EmptyIterator() {
        super();
    }

}
