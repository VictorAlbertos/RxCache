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

package io.rx_cache2.internal;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.observers.TestObserver;
import io.rx_cache2.ConfigProvider;
import io.rx_cache2.EvictProvider;
import io.rx_cache2.Reply;
import io.rx_cache2.Source;
import io.rx_cache2.internal.cache.EvictExpirableRecordsPersistence;
import io.rx_cache2.internal.cache.EvictExpiredRecordsPersistence;
import io.rx_cache2.internal.cache.EvictRecord;
import io.rx_cache2.internal.cache.GetDeepCopy;
import io.rx_cache2.internal.cache.HasRecordExpired;
import io.rx_cache2.internal.cache.RetrieveRecord;
import io.rx_cache2.internal.cache.SaveRecord;
import io.rx_cache2.internal.cache.TwoLayersCache;
import io.rx_cache2.internal.cache.memory.ReferenceMapMemory;
import io.rx_cache2.internal.common.BaseTest;
import io.rx_cache2.internal.migration.DoMigrations;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * Created by victor on 28/12/15.
 */
public class ProcessorProvidersTest extends BaseTest {
    private io.rx_cache2.internal.ProcessorProvidersBehaviour processorProvidersUT;
    private TwoLayersCache twoLayersCacheMock;
    private HasRecordExpired hasRecordExpired;
    private EvictExpiredRecordsPersistence evictExpiredRecordsPersistence;
    private GetDeepCopy getDeepCopy;
    private DoMigrations doMigrations;

    @Override public void setUp() {
        super.setUp();

        hasRecordExpired = new HasRecordExpired();

        io.rx_cache2.internal.Memory memory = new ReferenceMapMemory();
        EvictRecord evictRecord = new EvictRecord(memory,disk);
        SaveRecord saveRecord = new SaveRecord(memory, disk, 100, new EvictExpirableRecordsPersistence(memory, disk, 100, null), null);
        RetrieveRecord retrieveRecord = new RetrieveRecord(memory,disk, evictRecord, hasRecordExpired, null);

        evictExpiredRecordsPersistence = new EvictExpiredRecordsPersistence(memory, disk, hasRecordExpired, null);
        twoLayersCacheMock = new TwoLayersCache(evictRecord, retrieveRecord, saveRecord);
        getDeepCopy = new GetDeepCopy(memory, disk, Jolyglot$.newInstance());
        doMigrations = new DoMigrations(disk, null, null);
    }

    @Test public void When_First_Retrieve_Then_Source_Retrieved_Is_Cloud() {
        TestObserver observerMock = getSubscriberCompleted(false, false, true, Loader.VALID, false);
        Reply<Mock> reply = (Reply) observerMock.values().get(0);
        assertThat(reply.getSource(), is(Source.CLOUD));
        assertNotNull(reply.getData());
    }

    @Test public void When_Evict_Cache_Then_Source_Retrieved_Is_Cloud() {
        TestObserver observerMock = getSubscriberCompleted(true, true, true, Loader.VALID, false);
        Reply<Mock> reply = (Reply) observerMock.values().get(0);
        assertThat(reply.getSource(), is(Source.CLOUD));
        assertNotNull(reply.getData());
    }

    @Test public void When_No_Evict_Cache_Then_Source_Retrieved_Is_Not_Cloud() {
        TestObserver observerMock = getSubscriberCompleted(true, false, true, Loader.VALID, false);
        Reply<Mock> reply = (Reply) observerMock.values().get(0);
        assertThat(reply.getSource(), is(not(Source.CLOUD)));
        assertNotNull(reply.getData());
    }

    @Test public void When_No_Reply_Then_Get_Mock() {
        TestObserver observerMock = getSubscriberCompleted(true, false, false, Loader.VALID, false);
        Mock mock = (Mock) observerMock.values().get(0);
        assertNotNull(mock);
    }

    @Test public void When_No_Loader_And_Not_Cache_Then_Get_Throw_Exception() {
        TestObserver observerMock = getSubscriberCompleted(false, false, false, Loader.NULL, false);
        assertThat(observerMock.errorCount(), is(1));
        assertThat(observerMock.valueCount(), is(0));
    }

    @Test public void When_No_Loader_And_Cache_Expired_Then_Get_Throw_Exception() {
        TestObserver observerMock = getSubscriberCompleted(true, true, false, Loader.NULL, false);
        assertThat(observerMock.errorCount(), is(1));
        assertThat(observerMock.valueCount(), is(0));
    }

    @Test public void When_No_Loader_And_Cache_Expired_But_Use_Expired_Data_If_Loader_Not_Available_Then_Get_Mock() {
        processorProvidersUT = new io.rx_cache2.internal.ProcessorProvidersBehaviour(twoLayersCacheMock, false, evictExpiredRecordsPersistence,
            getDeepCopy, doMigrations);

        TestObserver observerMock =
            getSubscriberCompleted(true, true, false, Loader.NULL, true);
        assertThat(observerMock.errorCount(), is(0));
        assertThat(observerMock.valueCount(), is(1));
    }

    @Test public void When_Loader_Throws_Exception_And_Cache_Expired_Then_Get_Throw_Exception() {
        TestObserver observerMock = getSubscriberCompleted(true, true, false, Loader.EXCEPTION, false);
        assertThat(observerMock.errorCount(), is(1));
        assertThat(observerMock.valueCount(), is(0));
    }

    @Test public void When_Loader_Throws_Exception_And_Cache_Expired_But_Use_Expired_Data_If_Loader_Not_Available_Then_Get_Mock() {
        processorProvidersUT = new io.rx_cache2.internal.ProcessorProvidersBehaviour(twoLayersCacheMock, false, evictExpiredRecordsPersistence,
            getDeepCopy, doMigrations);

        TestObserver observerMock = getSubscriberCompleted(true, true, false, Loader.EXCEPTION, true);
        assertThat(observerMock.errorCount(), is(0));
        assertThat(observerMock.valueCount(), is(1));
    }

    private TestObserver getSubscriberCompleted(boolean hasCache, final boolean evictCache,
        boolean detailResponse, Loader loader, boolean useExpiredDataIfLoaderNotAvailable) {
        Observable observable;
        switch (loader) {
            case VALID:
                observable = Observable.just(new Mock("message"));
                break;
            case NULL:
                observable = Observable.error(new RuntimeException("No data"));
                break;
            default:
                observable = Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override public void subscribe(ObservableEmitter<Object> e) throws Exception {
                        throw new RuntimeException("error");
                    }
                });
                break;
        }

        ConfigProvider configProvider = new ConfigProvider("mockKey",
            null, null, detailResponse, true, false,
            "", "", observable, new EvictProvider(evictCache));

        if (hasCache) twoLayersCacheMock.save("mockKey", "", "", new Mock("message"), configProvider.getLifeTimeMillis(), configProvider.isExpirable(), configProvider.isEncrypted());

        processorProvidersUT = new ProcessorProvidersBehaviour(twoLayersCacheMock, useExpiredDataIfLoaderNotAvailable, evictExpiredRecordsPersistence,
            getDeepCopy, doMigrations);

        TestObserver observerMock = processorProvidersUT.getData(configProvider).test();
        observerMock.awaitTerminalEvent();
        return observerMock;
    }

    enum Loader {
        VALID, NULL, EXCEPTION
    }
}
