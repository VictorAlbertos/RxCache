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

import java.util.NoSuchElementException;

/**
 * Provides an implementation of an empty iterator.
 *
 * @since 3.1
 * @version $Id: AbstractEmptyIterator.java 1477802 2013-04-30 20:01:28Z tn $
 */
abstract class AbstractEmptyIterator<E> {

    /**
     * Constructor.
     */
    protected AbstractEmptyIterator() {
        super();
    }

    public boolean hasNext() {
        return false;
    }

    public E next() {
        throw new NoSuchElementException("Iterator contains no elements");
    }

    public boolean hasPrevious() {
        return false;
    }

    public E previous() {
        throw new NoSuchElementException("Iterator contains no elements");
    }

    public int nextIndex() {
        return 0;
    }

    public int previousIndex() {
        return -1;
    }

    public void add(final E obj) {
        throw new UnsupportedOperationException("addOrUpdate() not supported for empty Iterator");
    }

    public void set(final E obj) {
        throw new IllegalStateException("Iterator contains no elements");
    }

    public void remove() {
        throw new IllegalStateException("Iterator contains no elements");
    }

    public void reset() {
        // do nothing
    }

}
