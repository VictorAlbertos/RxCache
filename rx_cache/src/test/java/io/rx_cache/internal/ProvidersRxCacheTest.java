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

package io.rx_cache.internal;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;

import io.rx_cache.Invalidator;
import io.rx_cache.InvalidatorDynamicKey;
import io.rx_cache.PolicyHeapCache;
import io.rx_cache.Reply;
import io.rx_cache.Source;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Created by victor on 28/12/15.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProvidersRxCacheTest {
    @ClassRule public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    private ProvidersRxCache providersRxCache;
    private final static int SIZE = 100;
    private final Invalidator invalidatorFalse = new Invalidator() {
        @Override public boolean invalidate() {
            return false;
        }
    };

    private final Invalidator invalidatorTrue = new Invalidator() {
        @Override public boolean invalidate() {
            return true;
        }
    };

    @Before public void setUp() {
        providersRxCache = new RxCache.Builder()
                .withPolicyCache(PolicyHeapCache.MODERATE)
                .persistence(temporaryFolder.getRoot())
                .using(ProvidersRxCache.class);
    }

    @Test public void _1_Before_Destroy_Memory() {
        TestSubscriber<Reply<List<Mock>>> subscriber;

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksWithDetailResponse(createObservableMocks(SIZE)).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        Reply<List<Mock>> reply = subscriber.getOnNextEvents().get(0);
        assertThat(reply.getSource(), is(Source.CLOUD));
        assertThat(subscriber.getOnNextEvents().get(0).getData().size(), is(SIZE));

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksWithDetailResponse(createObservableMocks(SIZE)).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        reply = subscriber.getOnNextEvents().get(0);
        assertThat(reply.getSource(), is(Source.MEMORY));
        assertThat(reply.getData().size(), is(SIZE));
    }

    @Test public void _2_After_Memory_Destroyed() {
        TestSubscriber<Reply<List<Mock>>> subscriber;

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksWithDetailResponse(createObservableMocks(SIZE)).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        Reply<List<Mock>> reply = subscriber.getOnNextEvents().get(0);
        assertThat(reply.getSource(), is(Source.PERSISTENCE));
        assertThat(subscriber.getOnNextEvents().get(0).getData().size(), is(SIZE));

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksResponseOneSecond(createObservableMocks(SIZE)).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        reply = subscriber.getOnNextEvents().get(0);
        assertThat(reply.getSource(), is(Source.CLOUD));
        assertThat(subscriber.getOnNextEvents().get(0).getData().size(), is(SIZE));

        waitTime(1100);

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksResponseOneSecond(createObservableMocks(SIZE)).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        reply = subscriber.getOnNextEvents().get(0);
        assertThat(reply.getSource(), is(Source.CLOUD));
        assertThat(subscriber.getOnNextEvents().get(0).getData().size(), is(SIZE));

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksResponseOneSecond(createObservableMocks(SIZE)).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        reply = subscriber.getOnNextEvents().get(0);
        assertThat(reply.getSource(), is(Source.MEMORY));
        assertThat(subscriber.getOnNextEvents().get(0).getData().size(), is(SIZE));
    }

    @Test public void _3_Invalidating_Cache() {
        TestSubscriber<Reply<List<Mock>>> subscriber;

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksInvalidateCache(createObservableMocks(SIZE), invalidatorFalse).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        Reply<List<Mock>> reply = subscriber.getOnNextEvents().get(0);
        assertThat(reply.getSource(), is(Source.CLOUD));
        assertThat(subscriber.getOnNextEvents().get(0).getData().size(), is(SIZE));

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksInvalidateCache(createObservableMocks(SIZE), invalidatorFalse).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        reply = subscriber.getOnNextEvents().get(0);
        assertThat(reply.getSource(), is(Source.MEMORY));
        assertThat(subscriber.getOnNextEvents().get(0).getData().size(), is(SIZE));

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksInvalidateCache(createObservableMocks(SIZE), invalidatorTrue).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        reply = subscriber.getOnNextEvents().get(0);
        assertThat(reply.getSource(), is(Source.CLOUD));
        assertThat(subscriber.getOnNextEvents().get(0).getData().size(), is(SIZE));
    }

    @Test public void _4_Pagination() {
        TestSubscriber<List<Mock>> subscriber;

        List<Mock> mocksPage1 = createMocks(SIZE);
        String mockPage1Value = mocksPage1.get(0).getMessage();

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksPaginate(Observable.just(mocksPage1), 1).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        List<Mock> mocksPage2 = createMocks(SIZE);
        String mockPage2Value = mocksPage2.get(0).getMessage();

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksPaginate(Observable.just(mocksPage2), 2).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        List<Mock> mocksPage3 = createMocks(SIZE);
        String mockPage3Value = mocksPage3.get(0).getMessage();

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksPaginate(Observable.just(mocksPage3), 3).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksPaginate(Observable.<List<Mock>>just(null), 1).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertThat(subscriber.getOnNextEvents().get(0).get(0).getMessage(), is(mockPage1Value));

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksPaginate(Observable.<List<Mock>>just(null), 2).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertThat(subscriber.getOnNextEvents().get(0).get(0).getMessage(), is(mockPage2Value));

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksPaginate(Observable.<List<Mock>>just(null), 3).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertThat(subscriber.getOnNextEvents().get(0).get(0).getMessage(), is(mockPage3Value));
    }

    @Test public void _5_Pagination_Invalidate_All() {
        TestSubscriber<List<Mock>> subscriber;

        Invalidator invalidatorFalse = new Invalidator() {
            @Override public boolean invalidate() {
                return true;
            }
        };

        Invalidator invalidatorTrue = new Invalidator() {
            @Override public boolean invalidate() {
                return true;
            }
        };

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksPaginateInvalidateAll(Observable.just(createMocks(SIZE)), 1, invalidatorFalse).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksPaginateInvalidateAll(Observable.just(createMocks(SIZE)), 2, invalidatorFalse).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksPaginateInvalidateAll(Observable.<List<Mock>>just(null), 1, invalidatorTrue).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksPaginateInvalidateAll(Observable.<List<Mock>>just(null), 1, invalidatorFalse).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertThat(subscriber.getOnErrorEvents().size(), is(1));
        assertThat(subscriber.getOnNextEvents().size(), is(0));

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksPaginateInvalidateAll(Observable.<List<Mock>>just(null), 2, invalidatorFalse).subscribe(subscriber);
        assertThat(subscriber.getOnErrorEvents().size(), is(1));
        assertThat(subscriber.getOnNextEvents().size(), is(0));
    }

    @Test public void _6_Pagination_With_Invalidate_Cache() {
        TestSubscriber<Reply<List<Mock>>> subscriber;

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksDynamicKeyInvalidateCache(Observable.just(createMocks(SIZE)), 1, new InvalidatorDynamicKey() {
            @Override
            public Object dynamicKey() {
                return 1;
            }

            @Override
            public boolean invalidate() {
                return true;
            }
        }).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksDynamicKeyInvalidateCache(Observable.just(createMocks(SIZE)), 2, new InvalidatorDynamicKey() {
            @Override
            public Object dynamicKey() {
                return 2;
            }

            @Override
            public boolean invalidate() {
                return true;
            }
        }).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksDynamicKeyInvalidateCache(Observable.<List<Mock>>just(null), 1, new InvalidatorDynamicKey() {
            @Override
            public Object dynamicKey() {
                return 1;
            }

            @Override
            public boolean invalidate() {
                return true;
            }
        }).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertThat(subscriber.getOnErrorEvents().size(), is(1));
        assertThat(subscriber.getOnNextEvents().size(), is(0));

        //the data associated with key 2 still remains because invalidation dynamic only affect to the key specified
        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksDynamicKeyInvalidateCache(Observable.<List<Mock>>just(null), 2, new InvalidatorDynamicKey() {
            @Override
            public Object dynamicKey() {
                return 2;
            }

            @Override
            public boolean invalidate() {
                return false;
            }
        }).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertThat(subscriber.getOnErrorEvents().size(), is(0));
        assertThat(subscriber.getOnNextEvents().size(), is(1));
    }

    @Test public void _7_Session_Mock() {
        TestSubscriber<Mock> subscriber = new TestSubscriber<>();
        Mock mock = createMocks(SIZE).get(0);

        //not logged mock
        providersRxCache.getLoggedMock(Observable.<Mock>just(null), invalidatorFalse).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertThat(subscriber.getOnNextEvents().size(), is(0));

        //login mock
        subscriber = new TestSubscriber<>();
        providersRxCache.getLoggedMock(Observable.just(mock), invalidatorTrue).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        //logged mock
        subscriber = new TestSubscriber<>();
        providersRxCache.getLoggedMock(Observable.<Mock>just(null), invalidatorFalse).subscribe(subscriber);
        assertNotNull(subscriber.getOnNextEvents().get(0));

        //logout mock
        subscriber = new TestSubscriber<>();
        providersRxCache.getLoggedMock(Observable.<Mock>just(null), invalidatorTrue).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertThat(subscriber.getOnNextEvents().size(), is(0));

        //not logged mock
        subscriber = new TestSubscriber<>();
        providersRxCache.getLoggedMock(Observable.<Mock>just(null), invalidatorFalse).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertThat(subscriber.getOnNextEvents().size(), is(0));
    }

    @Test public void _8_Use_Expired_Data() {
        ProvidersRxCache providersRxCache = new RxCache.Builder()
                .useExpiredDataIfLoaderNotAvailable(true)
                .persistence(temporaryFolder.getRoot())
                .using(ProvidersRxCache.class);

        TestSubscriber<Reply<List<Mock>>> subscriber;

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksResponseOneSecond(createObservableMocks(SIZE)).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        waitTime(1100);

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksResponseOneSecond(Observable.<List<Mock>>just(null)).subscribe(subscriber);

        Reply<List<Mock>> reply = subscriber.getOnNextEvents().get(0);
        assertThat(reply.getData().size(), is(SIZE));
    }


    private void waitTime(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Observable<List<Mock>> createObservableMocks(int size) {
        long currentTime = System.currentTimeMillis();

        List<Mock> mocks = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            mocks.add(new Mock("mock"+currentTime));
        }

        return Observable.just(mocks);
    }

    private List<Mock> createMocks(int size) {
        long currentTime = System.currentTimeMillis();

        List<Mock> mocks = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            mocks.add(new Mock("mock"+currentTime));
        }

        return mocks;
    }
}
