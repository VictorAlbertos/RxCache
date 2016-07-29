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

import io.rx_cache.ConfigProvider;
import io.rx_cache.EvictProvider;
import io.rx_cache.Reply;
import io.rx_cache.Source;
import io.rx_cache.internal.cache.EvictExpirableRecordsPersistence;
import io.rx_cache.internal.cache.EvictExpiredRecordsPersistence;
import io.rx_cache.internal.cache.EvictRecord;
import io.rx_cache.internal.cache.GetDeepCopy;
import io.rx_cache.internal.cache.HasRecordExpired;
import io.rx_cache.internal.cache.RetrieveRecord;
import io.rx_cache.internal.cache.SaveRecord;
import io.rx_cache.internal.cache.TwoLayersCache;
import io.rx_cache.internal.cache.memory.ReferenceMapMemory;
import io.rx_cache.internal.common.BaseTest;
import io.rx_cache.internal.migration.DoMigrations;
import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * Created by victor on 28/12/15.
 */
public class ProcessorProvidersTest extends BaseTest {
    private ProcessorProvidersBehaviour processorProvidersUT;
    private TwoLayersCache twoLayersCacheMock;
    private HasRecordExpired hasRecordExpired;
    private EvictExpiredRecordsPersistence evictExpiredRecordsPersistence;
    private GetDeepCopy getDeepCopy;
    private DoMigrations doMigrations;

    @Override public void setUp() {
        super.setUp();

        hasRecordExpired = new HasRecordExpired();

        Memory memory = new ReferenceMapMemory();
        EvictRecord evictRecord =  new EvictRecord(memory,disk);
        SaveRecord saveRecord = new SaveRecord(memory, disk, 100, new EvictExpirableRecordsPersistence(memory, disk, 100, null), null);
        RetrieveRecord retrieveRecord = new RetrieveRecord(memory,disk, evictRecord, hasRecordExpired, null);

        evictExpiredRecordsPersistence = new EvictExpiredRecordsPersistence(memory, disk, hasRecordExpired, null);
        twoLayersCacheMock = new TwoLayersCache(evictRecord, retrieveRecord, saveRecord);
        getDeepCopy = new GetDeepCopy(memory, disk, Jolyglot$.newInstance());
        doMigrations = new DoMigrations(disk, null, null);
    }

    @Test public void When_First_Retrieve_Then_Source_Retrieved_Is_Cloud() {
        TestSubscriber subscriberMock = getSubscriberCompleted(false, false, true, Loader.VALID, false);
        Reply<Mock> reply = (Reply) subscriberMock.getOnNextEvents().get(0);
        assertThat(reply.getSource(), is(Source.CLOUD));
        assertNotNull(reply.getData());
    }

    @Test public void When_Evict_Cache_Then_Source_Retrieved_Is_Cloud() {
        TestSubscriber subscriberMock = getSubscriberCompleted(true, true, true, Loader.VALID, false);
        Reply<Mock> reply = (Reply) subscriberMock.getOnNextEvents().get(0);
        assertThat(reply.getSource(), is(Source.CLOUD));
        assertNotNull(reply.getData());
    }

    @Test public void When_No_Evict_Cache_Then_Source_Retrieved_Is_Not_Cloud() {
        TestSubscriber subscriberMock = getSubscriberCompleted(true, false, true, Loader.VALID, false);
        Reply<Mock> reply = (Reply) subscriberMock.getOnNextEvents().get(0);
        assertThat(reply.getSource(), is(not(Source.CLOUD)));
        assertNotNull(reply.getData());
    }

    @Test public void When_No_Reply_Then_Get_Mock() {
        TestSubscriber subscriberMock = getSubscriberCompleted(true, false, false, Loader.VALID, false);
        Mock mock = (Mock) subscriberMock.getOnNextEvents().get(0);
        assertNotNull(mock);
    }

    @Test public void When_No_Loader_And_Not_Cache_Then_Get_Throw_Exception() {
        TestSubscriber subscriberMock = getSubscriberCompleted(false, false, false, Loader.NULL, false);
        assertThat(subscriberMock.getOnErrorEvents().size(), is(1));
        assertThat(subscriberMock.getOnNextEvents().size(), is(0));
    }

    @Test public void When_No_Loader_And_Cache_Expired_Then_Get_Throw_Exception() {
        TestSubscriber subscriberMock = getSubscriberCompleted(true, true, false, Loader.NULL, false);
        assertThat(subscriberMock.getOnErrorEvents().size(), is(1));
        assertThat(subscriberMock.getOnNextEvents().size(), is(0));
    }

    @Test public void When_No_Loader_And_Cache_Expired_But_Use_Expired_Data_If_Loader_Not_Available_Then_Get_Mock() {
        processorProvidersUT = new ProcessorProvidersBehaviour(twoLayersCacheMock, false, evictExpiredRecordsPersistence,
            getDeepCopy, doMigrations);

        TestSubscriber subscriberMock = getSubscriberCompleted(true, true, false, Loader.NULL, true);
        assertThat(subscriberMock.getOnErrorEvents().size(), is(0));
        assertThat(subscriberMock.getOnNextEvents().size(), is(1));
    }

    @Test public void When_Loader_Throws_Exception_And_Cache_Expired_Then_Get_Throw_Exception() {
        TestSubscriber subscriberMock = getSubscriberCompleted(true, true, false, Loader.EXCEPTION, false);
        assertThat(subscriberMock.getOnErrorEvents().size(), is(1));
        assertThat(subscriberMock.getOnNextEvents().size(), is(0));
    }

    @Test public void When_Loader_Throws_Exception_And_Cache_Expired_But_Use_Expired_Data_If_Loader_Not_Available_Then_Get_Mock() {
        processorProvidersUT = new ProcessorProvidersBehaviour(twoLayersCacheMock, false, evictExpiredRecordsPersistence,
            getDeepCopy, doMigrations);

        TestSubscriber subscriberMock = getSubscriberCompleted(true, true, false, Loader.EXCEPTION, true);
        assertThat(subscriberMock.getOnErrorEvents().size(), is(0));
        assertThat(subscriberMock.getOnNextEvents().size(), is(1));
    }

    private TestSubscriber getSubscriberCompleted(boolean hasCache, final boolean evictCache,
        boolean detailResponse, Loader loader, boolean useExpiredDataIfLoaderNotAvailable) {
        Observable observable;
        switch (loader) {
            case VALID:
                observable = Observable.just(new Mock("message"));
                break;
            case NULL:
                observable = Observable.just(null);
                break;
            default:
                observable = Observable.create(new Observable.OnSubscribe() {
                    @Override public void call(Object o) {
                        throw new RuntimeException("error");
                    }
                });
                break;
        }

        ConfigProvider configProvider = new ConfigProvider("mockKey",
            null, null, detailResponse, true, false,
            "", "", observable, new EvictProvider(evictCache));

        if (hasCache) twoLayersCacheMock.save("mockKey", "", "", new Mock("message"), configProvider.getLifeTimeMillis(), configProvider.isExpirable(), configProvider.isEncrypted());

        TestSubscriber subscriberMock = new TestSubscriber<>();
        processorProvidersUT = new ProcessorProvidersBehaviour(twoLayersCacheMock, useExpiredDataIfLoaderNotAvailable, evictExpiredRecordsPersistence,
            getDeepCopy, doMigrations);
        processorProvidersUT.getData(configProvider).subscribe(subscriberMock);

        subscriberMock.awaitTerminalEvent();
        return subscriberMock;
    }

    enum Loader {
        VALID, NULL, EXCEPTION
    }
}
