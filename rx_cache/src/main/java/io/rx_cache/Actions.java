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

package io.rx_cache;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Provides a set of actions in order to perform write operations with providers in a more easy and safely way.
 */
public class Actions<T> {
    protected Observable<List<T>> cache;
    protected final Evict<T> evict;

    private Actions(Evict<T> evict, Observable<List<T>> cache) {
        this.evict = evict;
        this.cache = cache;
    }

    /**
     * Static accessor to create an instance of Actions in order follow the "builder" pattern.
     * @param evict The implementation of Evict interface which allow to persists the changes.
     * @param cache An observable which is the result of calling the provider without evicting its data.
     * @param <T> the type of the data to be processed.
     * @return an instance of Actions.
     */
    public static <T> Actions<T> with(Evict<T> evict, Observable<List<T>> cache) {
        return new Actions<T>(evict, cache);
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

    /**
     * Evict object at the first position of the cache
     * @return itself
     */
    public Actions<T> evictFirst() {
        Func3<T> first = new Func3<T>() {
            @Override public boolean call(int position, int count, T element) {
                return position == 0;
            }
        };
        return evict(first);
    }

    /**
     * Evict object at the last position of the cache.
     * @return itself
     */
    public Actions<T> evictLast() {
        Func3<T> last = new Func3<T>() {
            @Override public boolean call(int position, int count, T element) {
                return position == count - 1;
            }
        };
        return evict(last);
    }

    /**
     * Evict object at the first position of the cache.
     * @param func1Count exposes the count of elements in the cache.
     * @return itself
     */
    public Actions<T> evictFirst(final Func1Count func1Count) {
        Func3<T> firstPlusFunc1 = new Func3<T>() {
            @Override public boolean call(int position, int count, T element) {
                return position == 0 && func1Count.call(count);
            }
        };
        return evict(firstPlusFunc1);
    }

    /**
     * Evict object at the last position of the cache.
     * @param func1Count exposes the count of elements in the cache.
     * @return itself
     */
    public Actions<T> evictLast(final Func1Count func1Count) {
        Func3<T> lastPlusFunc1 = new Func3<T>() {
            @Override public boolean call(int position, int count, T element) {
                return position == count - 1 && func1Count.call(count);
            }
        };
        return evict(lastPlusFunc1);
    }

    /**
     * Func1Element will be called for every iteration until its condition returns true.
     * When true, the element of the current iteration is evicted from the cache.
     * @param func1Element exposes the element of the current iteration.
     * @return itself
     */
    public Actions<T> evict(final Func1Element<T> func1Element) {
        Func3<T> func3 = new Func3<T>() {
            @Override public boolean call(int position, int count, T element) {
                return func1Element.call(element);
            }
        };
        return evict(func3);
    }

    /**
     * Func3 will be called for every iteration until its condition returns true.
     * When true, the element of the current iteration is evicted from the cache.
     * @param func3 exposes the position of the current iteration, the count of elements in the cache and the element of the current iteration.
     * @return itself
     */
    public Actions<T> evict(final Func3<T> func3) {
        cache = cache.map(new Func1<List<T>, List<T>>() {
            @Override public List<T> call(List<T> elements) {
                int count = elements.size();

                for (int position = 0; position < count; position++) {
                    if (func3.call(position, count, elements.get(position))) {
                        elements.remove(position);
                        break;
                    }
                }

                return elements;
            }
        });

        return this;
    }

    /**
     * Evict all elements from the cache
     * @return itself
     */
    public Actions<T> evictAll() {
        Func3<T> func3 = new Func3<T>() {
            @Override public boolean call(int position, int count, T element) {
                return true;
            }
        };

        return evictIterable(func3);
    }

    /**
     * Func3 will be called for every iteration.
     * When true, the element of the current iteration is evicted from the cache.
     * @param func3 exposes the position of the current iteration, the count of elements in the cache and the element of the current iteration.
     * @return itself
     */
    public Actions<T> evictIterable(final Func3<T> func3) {
        cache = cache.map(new Func1<List<T>, List<T>>() {
            @Override public List<T> call(List<T> elements) {
                int count = elements.size();

                for (int position = 0; position < count; position++) {
                    if (func3.call(position, count, elements.get(position))) {
                        elements.set(position, null);
                    }
                }

                elements.removeAll(Collections.singleton(null));
                return elements;
            }
        });

        return this;
    }

    /**
     * Func1Element will be called for every iteration until its condition returns true.
     * When true, the element of the current iteration is updated.
     * @param func1Element exposes the element of the current iteration.
     * @param replace exposes the original element and expects back the one modified.
     * @return itself
     */
    public Actions<T> update(final Func1Element<T> func1Element, Replace<T> replace) {
        Func3<T> func3 = new Func3<T>() {
            @Override public boolean call(int position, int count, T element) {
                return func1Element.call(element);
            }
        };
        return update(func3, replace);
    }

    /**
     * Func3 will be called for every iteration until its condition returns true.
     * When true, the element of the current iteration is updated.
     * @param func3 exposes the position of the current iteration, the count of elements in the cache and the element of the current iteration.
     * @param replace exposes the original element and expects back the one modified.
     * @return itself
     */
    public Actions<T> update(final Func3<T> func3, final Replace<T> replace) {
        cache = cache.map(new Func1<List<T>, List<T>>() {
            @Override public List<T> call(List<T> elements) {
                int count = elements.size();

                for (int position = 0; position < count; position++) {
                    if (func3.call(position, count, elements.get(position))) {
                        elements.set(position, replace.call(elements.get(position)));
                        break;
                    }
                }

                return elements;
            }
        });

        return this;
    }

    /**
     * Func1Element will be called for every.
     * When true, the element of the current iteration is updated.
     * @param func1Element exposes the element of the current iteration.
     * @param replace exposes the original element and expects back the one modified.
     * @return itself
     */
    public Actions<T> updateIterable(final Func1Element<T> func1Element, Replace<T> replace) {
        Func3<T> func3 = new Func3<T>() {
            @Override public boolean call(int position, int count, T element) {
                return func1Element.call(element);
            }
        };
        return updateIterable(func3, replace);
    }

    /**
     * Func3 will be called for every iteration.
     * When true, the element of the current iteration is updated.
     * @param func3 exposes the position of the current iteration, the count of elements in the cache and the element of the current iteration.
     * @param replace exposes the original element and expects back the one modified.
     * @return itself
     */
    public Actions<T> updateIterable(final Func3<T> func3, final Replace<T> replace) {
        cache = cache.map(new Func1<List<T>, List<T>>() {
            @Override public List<T> call(List<T> elements) {
                int count = elements.size();

                for (int position = 0; position < count; position++) {
                    if (func3.call(position, count, elements.get(position))) {
                        elements.set(position, replace.call(elements.get(position)));
                    }
                }

                return elements;
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

    public interface Func1Count {
        boolean call(final int count);
    }

    public interface Func1Element<T> {
        boolean call(final T element);
    }

    public interface Func2 {
        boolean call(final int position, final int count);
    }

    public interface Func3<T> {
        boolean call(final int position, final int count, final T element);
    }

    public interface Replace<T> {
        T call(T element);
    }
}
