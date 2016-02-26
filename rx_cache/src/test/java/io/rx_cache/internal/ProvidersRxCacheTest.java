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

import io.rx_cache.DynamicKey;
import io.rx_cache.EvictDynamicKey;
import io.rx_cache.EvictProvider;
import io.rx_cache.PolicyHeapCache;
import io.rx_cache.Reply;
import io.rx_cache.Source;
import rx.Observable;
import rx.Subscriber;
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
    private final EvictProvider evictProviderFalse = new EvictProvider(false);
    private final EvictProvider evictProviderTrue = new EvictProvider(true);

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

    @Test public void _3_Evicting_Cache() {
        TestSubscriber<Reply<List<Mock>>> subscriber;

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksEvictProvider(createObservableMocks(SIZE), evictProviderFalse).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        Reply<List<Mock>> reply = subscriber.getOnNextEvents().get(0);
        assertThat(reply.getSource(), is(Source.CLOUD));
        assertThat(subscriber.getOnNextEvents().get(0).getData().size(), is(SIZE));

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksEvictProvider(createObservableMocks(SIZE), evictProviderFalse).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        reply = subscriber.getOnNextEvents().get(0);
        assertThat(reply.getSource(), is(Source.MEMORY));
        assertThat(subscriber.getOnNextEvents().get(0).getData().size(), is(SIZE));

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksEvictProvider(createObservableMocks(SIZE), evictProviderTrue).subscribe(subscriber);
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
        providersRxCache.getMocksPaginate(Observable.just(mocksPage1), new DynamicKey(1)).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        List<Mock> mocksPage2 = createMocks(SIZE);
        String mockPage2Value = mocksPage2.get(0).getMessage();

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksPaginate(Observable.just(mocksPage2), new DynamicKey(2)).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        List<Mock> mocksPage3 = createMocks(SIZE);
        String mockPage3Value = mocksPage3.get(0).getMessage();

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksPaginate(Observable.just(mocksPage3), new DynamicKey(3)).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksPaginate(Observable.<List<Mock>>just(null), new DynamicKey(1)).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertThat(subscriber.getOnNextEvents().get(0).get(0).getMessage(), is(mockPage1Value));

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksPaginate(Observable.<List<Mock>>just(null), new DynamicKey(2)).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertThat(subscriber.getOnNextEvents().get(0).get(0).getMessage(), is(mockPage2Value));

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksPaginate(Observable.<List<Mock>>just(null), new DynamicKey(3)).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertThat(subscriber.getOnNextEvents().get(0).get(0).getMessage(), is(mockPage3Value));
    }

    @Test public void _5_Pagination_Evict_All_Using_Null_Value_Observable() {
        paginationEvictAll(false);
    }

    @Test public void _6_Pagination_Evict_All_Using_Error_Observable() {
        paginationEvictAll(true);
    }

    private void paginationEvictAll(boolean usingErrorObservableInsteadOfNull) {
        TestSubscriber<List<Mock>> subscriber;

        EvictProvider evictProviderFalse = new EvictProvider(false);
        EvictProvider evictProviderTrue = new EvictProvider(true);

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksPaginateEvictProvider(Observable.just(createMocks(SIZE)), new DynamicKey(1), evictProviderFalse).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertThat(subscriber.getOnErrorEvents().size(), is(0));
        assertThat(subscriber.getOnNextEvents().size(), is(1));

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksPaginateEvictProvider(Observable.just(createMocks(SIZE)), new DynamicKey(2), evictProviderFalse).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertThat(subscriber.getOnErrorEvents().size(), is(0));
        assertThat(subscriber.getOnNextEvents().size(), is(1));

        Observable<List<Mock>> oMocks;
        if (usingErrorObservableInsteadOfNull) {
            oMocks = Observable.create(new Observable.OnSubscribe<List<Mock>>() {
                @Override public void call(Subscriber<? super List<Mock>> subscriber) {
                    subscriber.onError(new RuntimeException("fuck off"));
                }
            });
        } else {
            oMocks = Observable.just(null);
        }

       subscriber = new TestSubscriber<>();
        providersRxCache.getMocksPaginateEvictProvider(oMocks, new DynamicKey(1), evictProviderTrue).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksPaginateEvictProvider(Observable.<List<Mock>>just(null), new DynamicKey(1), evictProviderFalse).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertThat(subscriber.getOnErrorEvents().size(), is(1));
        assertThat(subscriber.getOnNextEvents().size(), is(0));

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksPaginateEvictProvider(Observable.<List<Mock>>just(null), new DynamicKey(2), evictProviderFalse).subscribe(subscriber);
        assertThat(subscriber.getOnErrorEvents().size(), is(1));
        assertThat(subscriber.getOnNextEvents().size(), is(0));
    }


    @Test public void _6_Pagination_With_Evict_Cache_By_Page() {
        TestSubscriber<Reply<List<Mock>>> subscriber;

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksDynamicKeyEvictPage(Observable.just(createMocks(SIZE)), new DynamicKey(1), new EvictDynamicKey(true)).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksDynamicKeyEvictPage(Observable.just(createMocks(SIZE)), new DynamicKey(2), new EvictDynamicKey(true)).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksDynamicKeyEvictPage(Observable.<List<Mock>>just(null), new DynamicKey(1), new EvictDynamicKey(true)).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertThat(subscriber.getOnErrorEvents().size(), is(1));
        assertThat(subscriber.getOnNextEvents().size(), is(0));

        //the data associated with key 2 still remains because invalidation dynamic only affect to the key specified
        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksDynamicKeyEvictPage(Observable.<List<Mock>>just(null), new DynamicKey(2), new EvictDynamicKey(false))
                .subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertThat(subscriber.getOnErrorEvents().size(), is(0));
        assertThat(subscriber.getOnNextEvents().size(), is(1));
    }

    @Test public void _7_Session_Mock() {
        TestSubscriber<Mock> subscriber = new TestSubscriber<>();
        Mock mock = createMocks(SIZE).get(0);

        //not logged mock
        providersRxCache.getLoggedMock(Observable.<Mock>just(null), evictProviderFalse).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertThat(subscriber.getOnNextEvents().size(), is(0));
        assertThat(subscriber.getOnErrorEvents().size(), is(1));

        //login mock
        subscriber = new TestSubscriber<>();
        providersRxCache.getLoggedMock(Observable.just(mock), evictProviderTrue).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertNotNull(subscriber.getOnNextEvents().get(0));

        //logged mock
        subscriber = new TestSubscriber<>();
        providersRxCache.getLoggedMock(Observable.<Mock>just(null), evictProviderFalse).subscribe(subscriber);
        assertNotNull(subscriber.getOnNextEvents().get(0));

        //logout mock
        subscriber = new TestSubscriber<>();
        providersRxCache.getLoggedMock(Observable.<Mock>just(null), evictProviderTrue).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertThat(subscriber.getOnNextEvents().size(), is(0));
        assertThat(subscriber.getOnErrorEvents().size(), is(1));

        //not logged mock
        subscriber = new TestSubscriber<>();
        providersRxCache.getLoggedMock(Observable.<Mock>just(null), evictProviderFalse).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertThat(subscriber.getOnNextEvents().size(), is(0));
        assertThat(subscriber.getOnErrorEvents().size(), is(1));
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
        assertThat(subscriber.getOnErrorEvents().size(), is(0));
    }

    @Test public void _9_Not_Use_Expired_Data() {
        ProvidersRxCache providersRxCache = new RxCache.Builder()
                .useExpiredDataIfLoaderNotAvailable(false)
                .persistence(temporaryFolder.getRoot())
                .using(ProvidersRxCache.class);

        TestSubscriber<Reply<List<Mock>>> subscriber;

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksResponseOneSecond(createObservableMocks(SIZE)).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        waitTime(1100);

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksResponseOneSecond(Observable.<List<Mock>>just(null)).subscribe(subscriber);
        assertThat(subscriber.getOnErrorEvents().size(), is(1));
        assertThat(subscriber.getOnNextEvents().size(), is(0));
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
