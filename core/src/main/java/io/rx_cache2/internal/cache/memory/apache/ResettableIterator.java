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
 * Defines an iterator that can be reset back to an initial state.
 * <p>
 * This interface allows an iterator to be repeatedly reused.
 *
 * @param <E> the type to iterate over
 * @since 3.0
 * @version $Id: ResettableIterator.java 1543263 2013-11-19 00:47:55Z ggregory $
 */
public interface ResettableIterator<E> extends Iterator<E> {

    /**
     * Resets the iterator back to the position at which the iterator
     * was created.
     */
    void reset();

}
