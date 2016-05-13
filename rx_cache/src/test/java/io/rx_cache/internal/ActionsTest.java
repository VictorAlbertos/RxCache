/*
 * Copyright 2016 Victor Albertos
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

package io.rx_cache.internal;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.rx_cache.*;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class ActionsTest {
    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private ProvidersActions providersActions;

    @Before public void setUp() {
        providersActions = new RxCache.Builder()
                .withPolicyCache(PolicyHeapCache.MODERATE)
                .persistence(temporaryFolder.getRoot())
                .using(ProvidersActions.class);
    }

    @Test public void Add_All() {
        checkInitialState();
        addAll(10);
    }

    @Test public void Add_All_First() {
        checkInitialState();
        addAll(10);

        TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .addAllFirst(Arrays.asList(new Mock("11"), new Mock("12")))
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(12));
        assertThat(mocks.get(0).getMessage(), is("11"));
        assertThat(mocks.get(1).getMessage(), is("12"));
    }

    @Test public void Add_All_Last() {
        checkInitialState();
        addAll(10);

        TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .addAllLast(Arrays.asList(new Mock("11"), new Mock("12")))
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(12));
        assertThat(mocks.get(10).getMessage(), is("11"));
        assertThat(mocks.get(11).getMessage(), is("12"));
    }

    @Test public void Add_First() {
        checkInitialState();

        TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .addFirst(new Mock("1"))
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(1));
        assertThat(mocks.get(0).getMessage(), is("1"));
    }

    @Test public void Add_Last() {
        checkInitialState();
        addAll(10);

        TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .addLast(new Mock("11"))
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(11));
        assertThat(mocks.get(10).getMessage(), is("11"));
    }

    @Test public void Add() {
        checkInitialState();
        addAll(10);

        TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .add(new Actions.Func2() {
                        @Override public boolean call(int position, int count) {
                            return position == 5;
                        }
                    }, new Mock("6_added"))
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(11));
        assertThat(mocks.get(5).getMessage(), is("6_added"));
    }

    @Test public void EvictFirst() {
        checkInitialState();
        addAll(10);

        TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .evictFirst()
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(9));
        assertThat(mocks.get(0).getMessage(), is("1"));
    }

    @Test public void EvictFirstX() {
        checkInitialState();
        addAll(10);

        TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .evictFirstX(4)
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(6));
        assertThat(mocks.get(0).getMessage(), is("4"));
    }

    @Test public void EvictFirstExposingCount() {
        checkInitialState();
        addAll(10);

        //do not evict
        TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .evictFirst(new Actions.Func1Count() {
                    @Override public boolean call(int count) {
                        return count > 10;
                    }
                })
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(10));

        //evict
        testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .evictFirst(new Actions.Func1Count() {
                    @Override public boolean call(int count) {
                        return count > 9;
                    }
                })
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(9));
        assertThat(mocks.get(0).getMessage(), is("1"));
    }

    @Test public void EvictFirstXExposingCount() {
        checkInitialState();
        addAll(10);

        //do not evict
        TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .evictFirstX(new Actions.Func1Count() {
                    @Override public boolean call(int count) {
                        return count > 10;
                    }
                }, 5)
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(10));

        //evict
        testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .evictFirstX(new Actions.Func1Count() {
                    @Override public boolean call(int count) {
                        return count > 9;
                    }
                }, 5)
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(5));
        assertThat(mocks.get(0).getMessage(), is("5"));
        assertThat(mocks.get(1).getMessage(), is("6"));
    }

    @Test public void EvictLast() {
        checkInitialState();
        addAll(10);

        TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .evictLast()
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(9));
        assertThat(mocks.get(8).getMessage(), is("8"));
    }

    @Test public void EvictLastX() {
        checkInitialState();
        addAll(10);

        TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .evictLastX(4)
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(6));
        assertThat(mocks.get(0).getMessage(), is("0"));
    }

    @Test public void EvictLastExposingCount() {
        checkInitialState();
        addAll(10);

        //do not evict
        TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .evictLast(new Actions.Func1Count() {
                    @Override public boolean call(int count) {
                        return count > 10;
                    }
                })
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(10));

        //evict
        testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .evictLast(new Actions.Func1Count() {
                    @Override public boolean call(int count) {
                        return count > 9;
                    }
                })
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(9));
        assertThat(mocks.get(8).getMessage(), is("8"));
    }

    @Test public void EvictLastXExposingCount() {
        checkInitialState();
        addAll(10);

        //do not evict
        TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .evictLastX(new Actions.Func1Count() {
                    @Override public boolean call(int count) {
                        return count > 10;
                    }
                }, 5)
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(10));

        //evict
        testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .evictLastX(new Actions.Func1Count() {
                    @Override public boolean call(int count) {
                        return count > 9;
                    }
                }, 5)
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(5));
        assertThat(mocks.get(0).getMessage(), is("0"));
        assertThat(mocks.get(1).getMessage(), is("1"));
    }

    @Test public void EvictExposingElementCurrentIteration() {
        checkInitialState();
        addAll(10);

        TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .evict(new Actions.Func1Element<Mock>() {
                    @Override public boolean call(Mock element) {
                        return element.getMessage().equals("3");
                    }
                })
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(9));
        assertThat(mocks.get(3).getMessage(), is("4"));
    }

    @Test public void EvictExposingCountAndPositionAndElementCurrentIteration() {
        checkInitialState();
        addAll(10);

        //do not evict
        TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .evict(new Actions.Func3<Mock>() {
                    @Override public boolean call(int position, int count, Mock element) {
                        return count > 10 && element.getMessage().equals("3");
                    }
                })
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(10));
        assertThat(mocks.get(3).getMessage(), is("3"));

        //evict
        testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .evict(new Actions.Func3<Mock>() {
                    @Override public boolean call(int position, int count, Mock element) {
                        return count > 9 && element.getMessage().equals("3");
                    }
                })
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(9));
        assertThat(mocks.get(3).getMessage(), is("4"));
    }

    @Test public void EvictIterable() {
        checkInitialState();
        addAll(10);

        TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .evictIterable(new io.rx_cache.Actions.Func3<Mock>() {
                    @Override public boolean call(int position, int count, Mock element) {
                        return element.getMessage().equals("2") || element.getMessage().equals("3");
                    }
                })
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(8));
        assertThat(mocks.get(2).getMessage(), is("4"));
        assertThat(mocks.get(3).getMessage(), is("5"));
    }

    @Test public void EvictAll() {
        checkInitialState();
        addAll(10);

        TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .evictAll()
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(0));
    }

    @Test public void UpdateExposingElementCurrentIteration() {
        checkInitialState();
        addAll(10);

        TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .update(new io.rx_cache.Actions.Func1Element<Mock>() {
                    @Override public boolean call(Mock element) {
                        return element.getMessage().equals("5");
                    }
                }, new io.rx_cache.Actions.Replace<Mock>() {
                    @Override public Mock call(Mock element) {
                        element.setMessage("5_updated");
                        return element;
                    }
                })
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(10));
        assertThat(mocks.get(5).getMessage(), is("5_updated"));
    }

    @Test public void UpdateExposingCountAndPositionAndElementCurrentIteration() {
        checkInitialState();
        addAll(10);

        //do not evict
        TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .update(new io.rx_cache.Actions.Func3<Mock>() {
                    @Override public boolean call(int position, int count, Mock element) {
                        return count > 10 && element.getMessage().equals("5");
                    }
                }, new io.rx_cache.Actions.Replace<Mock>() {
                    @Override public Mock call(Mock element) {
                        element.setMessage("5_updated");
                        return element;
                    }
                })
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(10));
        assertThat(mocks.get(5).getMessage(), is("5"));

        //evict
        testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .update(new io.rx_cache.Actions.Func3<Mock>() {
                    @Override public boolean call(int position, int count, Mock element) {
                        return count > 9 && element.getMessage().equals("5");
                    }
                }, new io.rx_cache.Actions.Replace<Mock>() {
                    @Override public Mock call(Mock element) {
                        element.setMessage("5_updated");
                        return element;
                    }
                })
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(10));
        assertThat(mocks.get(5).getMessage(), is("5_updated"));
    }

    @Test public void UpdateIterableExposingElementCurrentIteration() {
        checkInitialState();
        addAll(10);

        TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .updateIterable(new io.rx_cache.Actions.Func1Element<Mock>() {
                    @Override public boolean call(Mock element) {
                        return element.getMessage().equals("5") || element.getMessage().equals("6");
                    }
                }, new io.rx_cache.Actions.Replace<Mock>() {
                    @Override public Mock call(Mock element) {
                        element.setMessage("5_or_6_updated");
                        return element;
                    }
                })
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(10));
        assertThat(mocks.get(5).getMessage(), is("5_or_6_updated"));
        assertThat(mocks.get(6).getMessage(), is("5_or_6_updated"));
    }

    @Test public void UpdateIterableExposingCountAndPositionAndElementCurrentIteration() {
        checkInitialState();
        addAll(10);

        //do not evict
        TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .updateIterable(new io.rx_cache.Actions.Func3<Mock>() {
                    @Override public boolean call(int position, int count, Mock element) {
                        return count > 10 && (element.getMessage().equals("5") || element.getMessage().equals("6"));
                    }
                }, new io.rx_cache.Actions.Replace<Mock>() {
                    @Override public Mock call(Mock element) {
                        element.setMessage("5_or_6_updated");
                        return element;
                    }
                })
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(10));
        assertThat(mocks.get(5).getMessage(), is("5"));
        assertThat(mocks.get(6).getMessage(), is("6"));

        //evict
        testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .updateIterable(new io.rx_cache.Actions.Func3<Mock>() {
                    @Override public boolean call(int position, int count, Mock element) {
                        return count > 9 && (element.getMessage().equals("5") || element.getMessage().equals("6"));
                    }
                }, new io.rx_cache.Actions.Replace<Mock>() {
                    @Override public Mock call(Mock element) {
                        element.setMessage("5_or_6_updated");
                        return element;
                    }
                })
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(10));
        assertThat(mocks.get(5).getMessage(), is("5_or_6_updated"));
        assertThat(mocks.get(6).getMessage(), is("5_or_6_updated"));
    }

    private Observable<List<Mock>> cache() {
        return providersActions.mocks(Observable.<List<Mock>>just(new ArrayList<Mock>()), new EvictProvider(false));
    }

    private Actions.Evict<Mock> evict() {
        return new io.rx_cache.Actions.Evict<Mock>() {
            @Override public Observable<List<Mock>> call(Observable<List<Mock>> elements) {
                return providersActions.mocks(elements, new EvictProvider(true));
            }
        };
    }

    private void checkInitialState() {
        TestSubscriber<List<Mock>> testSubscriber = new TestSubscriber<>();
        cache().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        if (testSubscriber.getOnNextEvents().isEmpty()) return;

        List<Mock> mocks = testSubscriber.getOnNextEvents().get(0);
        assertThat(mocks.size(), is(0));
    }

    private void addAll(int count) {
        List<Mock> mocks = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            mocks.add(new Mock(String.valueOf(i)));
        }

        TestSubscriber<List<Mock>>  testSubscriber = new TestSubscriber<>();

        Actions.with(evict(), cache())
                .addAll(new io.rx_cache.Actions.Func2() {
                        @Override public boolean call(int position, int count) {
                            return position == count;
                        }
                    }, mocks)
                .toObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();
        assertThat(testSubscriber.getOnNextEvents().get(0).size(),
                is(count));
    }

    public interface ProvidersActions {
        Observable<List<Mock>> mocks(Observable<List<Mock>> mocks, EvictProvider evictProvider);
    }
}
