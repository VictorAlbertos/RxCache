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

import io.rx_cache.EvictProvider;
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
        providersRxCache.getMocksEvictProvider(createObservableMocks(SIZE), new EvictProvider(false)).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        Reply<List<Mock>> reply = subscriber.getOnNextEvents().get(0);
        assertThat(reply.getSource(), is(Source.CLOUD));
        assertThat(subscriber.getOnNextEvents().get(0).getData().size(), is(SIZE));

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksEvictProvider(createObservableMocks(SIZE), new EvictProvider(false)).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        reply = subscriber.getOnNextEvents().get(0);
        assertThat(reply.getSource(), is(Source.MEMORY));
        assertThat(subscriber.getOnNextEvents().get(0).getData().size(), is(SIZE));

        subscriber = new TestSubscriber<>();
        providersRxCache.getMocksEvictProvider(createObservableMocks(SIZE), new EvictProvider(true)).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        reply = subscriber.getOnNextEvents().get(0);
        assertThat(reply.getSource(), is(Source.CLOUD));
        assertThat(subscriber.getOnNextEvents().get(0).getData().size(), is(SIZE));
    }

    @Test public void _4_Session_Mock() {
        TestSubscriber<Mock> subscriber = new TestSubscriber<>();
        Mock mock = createMocks(SIZE).get(0);

        //not logged mock
        providersRxCache.getLoggedMock(Observable.<Mock>just(null), new EvictProvider(false)).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertThat(subscriber.getOnNextEvents().size(), is(0));
        assertThat(subscriber.getOnErrorEvents().size(), is(1));

        //login mock
        subscriber = new TestSubscriber<>();
        providersRxCache.getLoggedMock(Observable.just(mock), new EvictProvider(true)).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertNotNull(subscriber.getOnNextEvents().get(0));

        //logged mock
        subscriber = new TestSubscriber<>();
        providersRxCache.getLoggedMock(Observable.<Mock>just(null), new EvictProvider(false)).subscribe(subscriber);
        assertNotNull(subscriber.getOnNextEvents().get(0));

        //logout mock
        subscriber = new TestSubscriber<>();
        providersRxCache.getLoggedMock(Observable.<Mock>just(null), new EvictProvider(true)).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertThat(subscriber.getOnNextEvents().size(), is(0));
        assertThat(subscriber.getOnErrorEvents().size(), is(1));

        //not logged mock
        subscriber = new TestSubscriber<>();
        providersRxCache.getLoggedMock(Observable.<Mock>just(null), new EvictProvider(false)).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        assertThat(subscriber.getOnNextEvents().size(), is(0));
        assertThat(subscriber.getOnErrorEvents().size(), is(1));
    }

    @Test public void _5_Use_Expired_Data() {
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

    @Test public void _6_Not_Use_Expired_Data() {
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
