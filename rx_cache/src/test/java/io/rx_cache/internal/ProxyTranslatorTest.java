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

import java.lang.reflect.Method;

import io.rx_cache.Invalidator;
import io.rx_cache.InvalidatorDynamicKey;
import io.rx_cache.internal.common.BaseTest;
import rx.Observable;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * Created by victor on 28/12/15.
 */
public class ProxyTranslatorTest extends BaseTest {
    private ProxyTranslator proxyTranslatorUT;
    private final Object[] dataMethod = {Observable.just(null)};

    @Override public void setUp() {
        super.setUp();
        proxyTranslatorUT = new ProxyTranslator();
    }

    @Test public void Check_Basic_Method() throws NoSuchMethodException {
        Method mockMethod = ProvidersRxCache.class.getDeclaredMethod("getMocks", Observable.class);
        ProxyTranslator.ConfigProvider configProvider = proxyTranslatorUT.processMethod(mockMethod, dataMethod);

        assertThat(configProvider.getKey(), is("getMocks"));
        assertNotNull(configProvider.getLoaderObservable());
        assertThat(configProvider.getLifeTimeMillis(), is(0l));
        assertThat(configProvider.invalidator().invalidate(), is(false));
        assertThat(configProvider.requiredDetailedResponse(), is(false));
    }

    @Test public void Check_Method_With_Life_Time_Defined() throws NoSuchMethodException {
        Method mockMethod = ProvidersRxCache.class.getDeclaredMethod("getMocksLifeTimeMinutes", Observable.class);
        ProxyTranslator.ConfigProvider configProvider = proxyTranslatorUT.processMethod(mockMethod, dataMethod);
        assertThat(configProvider.getLifeTimeMillis(), is(60000l));

        mockMethod = ProvidersRxCache.class.getDeclaredMethod("getMocksLifeTimeSeconds", Observable.class);
        configProvider = proxyTranslatorUT.processMethod(mockMethod, dataMethod);
        assertThat(configProvider.getLifeTimeMillis(), is(1000l));

        mockMethod = ProvidersRxCache.class.getDeclaredMethod("getMocksLifeTimeMillis", Observable.class);
        configProvider = proxyTranslatorUT.processMethod(mockMethod, dataMethod);
        assertThat(configProvider.getLifeTimeMillis(), is(65000l));
    }

    @Test(expected=IllegalArgumentException.class) public void When_Not_Loader_Defined_Throw_Exception() throws NoSuchMethodException {
        Method mockMethod = ProvidersRxCache.class.getDeclaredMethod("getMocksWithoutLoaderAnnotation", Observable.class);
        proxyTranslatorUT.processMethod(mockMethod, dataMethod);
    }

    @Test public void When_Return_Response_Then_Required_Detail_Response_Is_True() throws NoSuchMethodException {
        Method mockMethod = ProvidersRxCache.class.getDeclaredMethod("getMocksWithDetailResponse", Observable.class);
        ProxyTranslator.ConfigProvider configProvider = proxyTranslatorUT.processMethod(mockMethod, dataMethod);

        assertThat(configProvider.requiredDetailedResponse(), is(true));
    }

    @Test(expected=IllegalArgumentException.class) public void When_Method_Not_Return_Observable_Then_Throw_Exception() throws NoSuchMethodException {
        Method mockMethod = ProvidersRxCache.class.getDeclaredMethod("getMocksBadReturnType", Observable.class);
        proxyTranslatorUT.processMethod(mockMethod, dataMethod);
    }

    @Test public void When_Invalidate_Cache_Invalidate() throws NoSuchMethodException {
        Method mockMethod = ProvidersRxCache.class.getDeclaredMethod("getMocksInvalidateCache", Observable.class, Invalidator.class);

        Object[] dataMethodInvalidate = {Observable.just(null), new Invalidator() {
            @Override public boolean invalidate() {
                return true;
            }
        }};

        ProxyTranslator.ConfigProvider configProvider = proxyTranslatorUT.processMethod(mockMethod, dataMethodInvalidate);
        assertThat(configProvider.invalidator().invalidate(), is(true));

        Object[] dataMethodNoInvalidate = {Observable.just(null), new Invalidator() {
            @Override public boolean invalidate() {
                return false;
            }
        }};
        configProvider = proxyTranslatorUT.processMethod(mockMethod, dataMethodNoInvalidate);
        assertThat(configProvider.invalidator().invalidate(), is(false));
    }

    @Test public void When_Invalidate_Cache_Dynamic_Key_Invalidate() throws NoSuchMethodException {
        final String dynamicKey = "aDynamicKey";

        Method mockMethod = ProvidersRxCache.class.getDeclaredMethod("getMocksDynamicKeyInvalidateCache", Observable.class, int.class, InvalidatorDynamicKey.class);

        Object[] dataMethodInvalidate = {Observable.just(null), 1, new InvalidatorDynamicKey() {
            @Override public Object dynamicKey() {
                return dynamicKey;
            }

            @Override public boolean invalidate() {
                return true;
            }
        }};

        ProxyTranslator.ConfigProvider configProvider = proxyTranslatorUT.processMethod(mockMethod, dataMethodInvalidate);
        InvalidatorDynamicKey invalidatorDynamicKey = (InvalidatorDynamicKey) configProvider.invalidator();
        assertThat(invalidatorDynamicKey.dynamicKey().toString(), is(dynamicKey));
        assertThat(invalidatorDynamicKey.invalidate(), is(true));

        Object[] dataMethodNoInvalidate = {Observable.just(null), 1, new InvalidatorDynamicKey() {
            @Override public Object dynamicKey() {
                return dynamicKey;
            }

            @Override public boolean invalidate() {
                return false;
            }
        }};

        configProvider = proxyTranslatorUT.processMethod(mockMethod, dataMethodNoInvalidate);
        invalidatorDynamicKey = (InvalidatorDynamicKey) configProvider.invalidator();
        assertThat(invalidatorDynamicKey.dynamicKey().toString(), is(dynamicKey));
        assertThat(invalidatorDynamicKey.invalidate(), is(false));
    }

    @Test public void When_Get_Page_Get_Pages() throws NoSuchMethodException {
        Object[] dataMethodPaginate = {Observable.just(null), 1};

        Method mockMethod = ProvidersRxCache.class.getDeclaredMethod("getMocksPaginate", Observable.class, int.class);
        ProxyTranslator.ConfigProvider configProvider = proxyTranslatorUT.processMethod(mockMethod, dataMethodPaginate);

        assertThat(configProvider.getDynamicKey(), is("1"));
    }

}
