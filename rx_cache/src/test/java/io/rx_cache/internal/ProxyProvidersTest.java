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

import org.junit.Test;

import io.rx_cache.Invalidator;
import io.rx_cache.PolicyHeapCache;
import io.rx_cache.Reply;
import io.rx_cache.Source;
import io.rx_cache.internal.common.BaseTest;
import rx.Observable;
import rx.observers.TestSubscriber;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * Created by victor on 28/12/15.
 */
public class ProxyProvidersTest extends BaseTest {
    private ProxyProviders proxyProvidersUT;
    private TwoLayersCache twoLayersCacheMock;


    @Override public void setUp() {
        super.setUp();
        twoLayersCacheMock = new TwoLayersCache(new GuavaMemory(PolicyHeapCache.MODERATE), disk);
    }

    @Test public void When_First_Retrieve_Then_Source_Retrieved_Is_Cloud() {
        TestSubscriber subscriberMock = getSubscriberCompleted(false, false, true, Loader.VALID, false);
        Reply<Mock> reply = (Reply) subscriberMock.getOnNextEvents().get(0);
        assertThat(reply.getSource(), is(Source.CLOUD));
        assertNotNull(reply.getData());
    }

    @Test public void When_Invalidate_Cache_Then_Source_Retrieved_Is_Cloud() {
        TestSubscriber subscriberMock = getSubscriberCompleted(true, true, true, Loader.VALID, false);
        Reply<Mock> reply = (Reply) subscriberMock.getOnNextEvents().get(0);
        assertThat(reply.getSource(), is(Source.CLOUD));
        assertNotNull(reply.getData());
    }

    @Test public void When_No_Invalidate_Cache_Then_Source_Retrieved_Is_Not_Cloud() {
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
        proxyProvidersUT = new ProxyProviders(null, twoLayersCacheMock, false);

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
        proxyProvidersUT = new ProxyProviders(null, twoLayersCacheMock, false);

        TestSubscriber subscriberMock = getSubscriberCompleted(true, true, false, Loader.EXCEPTION, true);
        assertThat(subscriberMock.getOnErrorEvents().size(), is(0));
        assertThat(subscriberMock.getOnNextEvents().size(), is(1));
    }

    private TestSubscriber getSubscriberCompleted(boolean hasCache, final boolean invalidateCache, boolean detailResponse, Loader loader, boolean useExpiredDataIfLoaderNotAvailable) {
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

        ProxyTranslator.ConfigProvider configProvider = new ProxyTranslator.ConfigProvider("mockKey", "", observable, 0, detailResponse, new Invalidator() {
            @Override public boolean invalidate() {
                return invalidateCache;
            }
        });

        if (hasCache) twoLayersCacheMock.save("mockKey", "", new Mock("message"));

        TestSubscriber subscriberMock = new TestSubscriber<>();
        proxyProvidersUT = new ProxyProviders(null, twoLayersCacheMock, useExpiredDataIfLoaderNotAvailable);
        proxyProvidersUT.getMethodImplementation(configProvider).subscribe(subscriberMock);

        subscriberMock.awaitTerminalEvent();
        return subscriberMock;
    }

    @Test public void When_Get_Method_Implementation_Is_Called_Retrieve_Operation_Is_Deferred_Until_Subscription() {
        ProxyTranslator.ConfigProvider configProvider = new ProxyTranslator.ConfigProvider("mockKey", "", Observable.just(new Mock("message")), 0, false, new Invalidator() {
            @Override public boolean invalidate() {
                return false;
            }
        });

        TestSubscriber subscriberMock = new TestSubscriber<>();
        proxyProvidersUT = new ProxyProviders(null, twoLayersCacheMock, true);
        Observable<Object> oData = proxyProvidersUT.getMethodImplementation(configProvider);
        assertThat(twoLayersCacheMock.retrieveHasBeenCalled(), is(false));

        oData.subscribe(subscriberMock);
        subscriberMock.awaitTerminalEvent();
        assertThat(twoLayersCacheMock.retrieveHasBeenCalled(), is(true));
    }

    enum Loader {
        VALID, NULL, EXCEPTION
    }
}
