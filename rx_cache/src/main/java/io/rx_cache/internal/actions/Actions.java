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

package io.rx_cache.internal.actions;

import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

public class Actions<T> {
    protected Observable<List<T>> cache;
    protected final Evict<T> evict;

    public Actions(Evict<T> evict, Observable<List<T>> cache) {
        this.evict = evict;
        this.cache = cache;
    }

    /**
     * Func2 will be called for every iteration until its condition returns true.
     * When true, the element is added to the cache at the position of the current iteration.
     * @param func2 exposes the position of the current iteration and the count of elements in the cache.
     * @param element the object to add to the cache.
     * @return itself
     */
    public Actions<T> add(Func2 func2, T element) {
        return addAll(func2, Arrays.asList(element));
    }

    /**
     * Add the object at the first position of the cache.
     * @param element the object to add to the cache.
     * @return itself
     */
    public Actions<T> addFirst(T element) {
        Func2 first = new Func2() {
            @Override public boolean call(int position, int count) {
                return position == 0;
            }
        };
        return addAll(first, Arrays.asList(element));
    }

    /**
     * Add the object at the last position of the cache.
     * @param element the object to add to the cache.
     * @return itself
     */
    public Actions<T> addLast(T element) {
        Func2 last = new Func2() {
            @Override public boolean call(int position, int count) {
                return position == count;
            }
        };

        return addAll(last, Arrays.asList(element));
    }

    /**
     * Func2 will be called for every iteration until its condition returns true.
     * When true, the elements are added to the cache at the position of the current iteration.
     * @param func2 exposes the position of the current iteration and the count of elements in the cache.
     * @param elements the objects to add to the cache.
     * @return itself
     */
    public Actions<T> addAll(final Func2 func2, final List<T> elements) {
        cache = cache.map(new Func1<List<T>, List<T>>() {
            @Override public List<T> call(List<T> items) {
                int count = items.size();

                for (int position = 0; position <= count; position++) {
                    if (func2.call(position, count)) {
                        items.addAll(position, elements);
                        break;
                    }
                }

                return items;
            }
        });

        return this;
    }

    public Observable<List<T>> toObservable() {
        return evict.call(cache);
    }

    public interface Evict<T> {
        Observable<List<T>> call(final Observable<List<T>> elements);
    }

    interface Func1Count {
        boolean call(final int count);
    }

    interface Func1Element<T> {
        boolean call(final T element);
    }

    interface Func2 {
        boolean call(final int position, final int count);
    }

    interface Func3<T> {
        boolean call(final int position, final int count, final T element);
    }

    interface Replace<T> {
        T call(T element);
    }
}
