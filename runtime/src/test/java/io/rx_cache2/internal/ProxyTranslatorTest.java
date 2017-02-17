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

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.rx_cache2.ConfigProvider;
import io.rx_cache2.DynamicKey;
import io.rx_cache2.DynamicKeyGroup;
import io.rx_cache2.EvictDynamicKey;
import io.rx_cache2.EvictDynamicKeyGroup;
import io.rx_cache2.EvictProvider;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by victor on 28/12/15.
 */
public class ProxyTranslatorTest {
  private ProxyTranslator proxyTranslatorUT;
  private final Object[] dataMethod = {Observable.just(new Object[]{})};

  @Before public void init() {
    proxyTranslatorUT = new ProxyTranslator();
  }

  @Test public void Check_Basic_Method() throws NoSuchMethodException {
    Method mockMethod =
        io.rx_cache2.internal.ProvidersRxCache.class.getDeclaredMethod("getMocks", Observable.class);
    ConfigProvider configProvider = proxyTranslatorUT.processMethod(mockMethod, dataMethod);

    assertThat(configProvider.getProviderKey(), is("getMocks"));
    assertNotNull(configProvider.getLoaderObservable());
    assertNull(configProvider.getLifeTimeMillis());
    assertThat(configProvider.evictProvider().evict(), is(false));
    assertThat(configProvider.requiredDetailedResponse(), is(false));
  }

  @Test public void Check_Single_Reactive_Type() throws NoSuchMethodException {
    Method mockMethod =
        io.rx_cache2.internal.ProvidersRxCache.class.getDeclaredMethod("getMocksSingle", Single.class);
    ConfigProvider configProvider = proxyTranslatorUT.processMethod(mockMethod, dataMethod);
    assertNotNull(configProvider.getLoaderObservable());
  }

  @Test public void Check_Maybe_Reactive_Type() throws NoSuchMethodException {
    Method mockMethod =
        io.rx_cache2.internal.ProvidersRxCache.class.getDeclaredMethod("getMocksMaybe", Maybe.class);
    ConfigProvider configProvider = proxyTranslatorUT.processMethod(mockMethod, dataMethod);
    assertNotNull(configProvider.getLoaderObservable());
  }

  @Test public void Check_Flowable_Reactive_Type() throws NoSuchMethodException {
    Method mockMethod =
        io.rx_cache2.internal.ProvidersRxCache.class.getDeclaredMethod("getMocksFlowable", Flowable.class);
    ConfigProvider configProvider = proxyTranslatorUT.processMethod(mockMethod, dataMethod);
    assertNotNull(configProvider.getLoaderObservable());
  }

  @Test public void Check_Method_With_Life_Time_Defined() throws NoSuchMethodException {
    Method mockMethod =
        io.rx_cache2.internal.ProvidersRxCache.class.getDeclaredMethod("getMocksLifeTimeMinutes",
            Observable.class);
    ConfigProvider configProvider = proxyTranslatorUT.processMethod(mockMethod, dataMethod);
    assertThat(configProvider.getLifeTimeMillis(), is(60000l));

    mockMethod =
        io.rx_cache2.internal.ProvidersRxCache.class.getDeclaredMethod("getMocksLifeTimeSeconds",
            Observable.class);
    configProvider = proxyTranslatorUT.processMethod(mockMethod, dataMethod);
    assertThat(configProvider.getLifeTimeMillis(), is(1000l));

    mockMethod =
        io.rx_cache2.internal.ProvidersRxCache.class.getDeclaredMethod("getMocksLifeTimeMillis",
            Observable.class);
    configProvider = proxyTranslatorUT.processMethod(mockMethod, dataMethod);
    assertThat(configProvider.getLifeTimeMillis(), is(65000l));
  }

  @Test public void When_Return_Response_Then_Required_Detail_Response_Is_True()
      throws NoSuchMethodException {
    Method mockMethod =
        io.rx_cache2.internal.ProvidersRxCache.class.getDeclaredMethod("getMocksWithDetailResponse",
            Observable.class);
    ConfigProvider configProvider = proxyTranslatorUT.processMethod(mockMethod, dataMethod);

    assertThat(configProvider.requiredDetailedResponse(), is(true));
  }

  @Test(expected = IllegalArgumentException.class)
  public void When_Method_Not_Return_Supported_Reactive_Type_Then_Throw_Exception()
      throws NoSuchMethodException {
    Method mockMethod =
        io.rx_cache2.internal.ProvidersRxCache.class.getDeclaredMethod("getMocksBadReturnType",
            Observable.class);
    proxyTranslatorUT.processMethod(mockMethod, dataMethod);
  }

  @Test public void When_Evict_Cache_Evict() throws NoSuchMethodException {
    Method mockMethod =
        io.rx_cache2.internal.ProvidersRxCache.class.getDeclaredMethod("getMocksEvictProvider",
            Observable.class, EvictProvider.class);

    Object[] dataMethodEvict = {Observable.just(new Object[]{}), new EvictProvider(true)};

    ConfigProvider configProvider = proxyTranslatorUT.processMethod(mockMethod, dataMethodEvict);
    assertThat(configProvider.evictProvider().evict(), is(true));

    Object[] dataMethodNoEvict = {Observable.just(new Object[]{}), new EvictProvider(false)};
    configProvider = proxyTranslatorUT.processMethod(mockMethod, dataMethodNoEvict);
    assertThat(configProvider.evictProvider().evict(), is(false));
  }

  @Test public void When_Evict_Cache_Dynamic_Key_Evict() throws NoSuchMethodException {
    final String dynamicKey = "aDynamicKey";

    Method mockMethod =
        io.rx_cache2.internal.ProvidersRxCache.class.getDeclaredMethod("getMocksDynamicKeyEvictPage",
            Observable.class, DynamicKey.class, EvictDynamicKey.class);

    Object[] dataMethodEvict =
        {Observable.just(new Object[]{}), new DynamicKey(dynamicKey), new EvictDynamicKey(true)};

    ConfigProvider configProvider = proxyTranslatorUT.processMethod(mockMethod, dataMethodEvict);
    EvictDynamicKey evictDynamicKey = (EvictDynamicKey) configProvider.evictProvider();
    assertThat(configProvider.getDynamicKey(), is(dynamicKey));
    assertThat(evictDynamicKey.evict(), is(true));

    Object[] dataMethodNoEvict =
        {Observable.just(new Object[]{}), new DynamicKey(dynamicKey), new EvictDynamicKey(false)};

    configProvider = proxyTranslatorUT.processMethod(mockMethod, dataMethodNoEvict);
    evictDynamicKey = (EvictDynamicKey) configProvider.evictProvider();
    assertThat(configProvider.getDynamicKey(), is(dynamicKey));
    assertThat(evictDynamicKey.evict(), is(false));
  }

  @Test public void When_Get_Page_Get_Pages() throws NoSuchMethodException {
    Object[] dataMethodPaginate = {Observable.just(new Object[]{}), new DynamicKey(1)};

    Method mockMethod =
        io.rx_cache2.internal.ProvidersRxCache.class.getDeclaredMethod("getMocksPaginate",
            Observable.class, DynamicKey.class);
    ConfigProvider configProvider = proxyTranslatorUT.processMethod(mockMethod, dataMethodPaginate);

    assertThat(configProvider.getDynamicKey(), is("1"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void When_Not_Loader_Provided_Throw_Exception() throws NoSuchMethodException {
    Method mockMethod = io.rx_cache2.internal.ProvidersRxCache.class.getDeclaredMethod(
        "getMockWithoutLoaderObservable");
    Object[] emptyDataMethod = {};
    proxyTranslatorUT.processMethod(mockMethod, emptyDataMethod);
  }

  @Test(expected = IllegalArgumentException.class)
  public void When_Not_Return_Observable_Throw_Exception() throws NoSuchMethodException {
    Method mockMethod = io.rx_cache2.internal.ProvidersRxCache.class.getDeclaredMethod(
        "getMockWithoutReturnObservable");
    proxyTranslatorUT.processMethod(mockMethod, dataMethod);
  }

  @Test(expected = IllegalArgumentException.class)
  public void When_Multiple_Observable_Throw_Exception() throws NoSuchMethodException {
    Method mockMethod =
        io.rx_cache2.internal.ProvidersRxCache.class.getDeclaredMethod("getMockMultipleObservables",
            Observable.class, Observable.class);
    Object[] data = {Observable.just(new Object[]{}), Observable.just("")};
    proxyTranslatorUT.processMethod(mockMethod, data);
  }

  @Test(expected = IllegalArgumentException.class) public void When_Multiple_Evict_Throw_Exception()
      throws NoSuchMethodException {
    Method mockMethod =
        io.rx_cache2.internal.ProvidersRxCache.class.getDeclaredMethod("getMockMultipleEvicts",
            Observable.class, EvictProvider.class, EvictProvider.class);
    Object[] data = {Observable.just(new Object[]{}), new EvictProvider(true), new EvictProvider(true)};
    proxyTranslatorUT.processMethod(mockMethod, data);
  }

  @Test(expected = IllegalArgumentException.class)
  public void When_Multiple_Dynamic_Keys_Throw_Exception() throws NoSuchMethodException {
    Method mockMethod =
        io.rx_cache2.internal.ProvidersRxCache.class.getDeclaredMethod("getMockMultipleDynamicKeys",
            Observable.class, DynamicKey.class, DynamicKey.class);
    Object[] data = {Observable.just(new Object[]{}), new DynamicKey(1), new DynamicKey(1)};
    proxyTranslatorUT.processMethod(mockMethod, data);
  }

  @Test public void When_Use_Evict_Dynamic_Key_Providing_Dynamic_Key_Not_Throw_Exception()
      throws NoSuchMethodException {
    Method mockMethod = io.rx_cache2.internal.ProvidersRxCache.class.getDeclaredMethod(
        "getMockEvictDynamicKeyProvidingDynamicKey", Observable.class, DynamicKey.class,
        EvictDynamicKey.class);
    Object[] data = {Observable.just(new Object[]{}), new DynamicKey("1"), new EvictDynamicKey(true)};
    proxyTranslatorUT.processMethod(mockMethod, data);
  }

  @Test(expected = IllegalArgumentException.class)
  public void When_Use_Evict_Dynamic_Key_Without_Providing_Dynamic_Key_Throw_Exception()
      throws NoSuchMethodException {
    Method mockMethod = io.rx_cache2.internal.ProvidersRxCache.class.getDeclaredMethod(
        "getMockEvictDynamicKeyWithoutProvidingDynamicKey", Observable.class,
        EvictDynamicKey.class);
    Object[] data = {Observable.just(new Object[]{}), new EvictDynamicKey(true)};
    proxyTranslatorUT.processMethod(mockMethod, data);
  }

  @Test
  public void When_Use_Evict_Dynamic_Key_Group_Providing_Dynamic_Key_Group_Not_Throw_Exception()
      throws NoSuchMethodException {
    Method mockMethod = io.rx_cache2.internal.ProvidersRxCache.class.getDeclaredMethod(
        "getMockEvictDynamicKeyGroupProvidingDynamicKeyGroup", Observable.class,
        DynamicKeyGroup.class, EvictDynamicKeyGroup.class);
    Object[] data =
        {Observable.just(new Object[]{}), new DynamicKeyGroup("1", "1"), new EvictDynamicKeyGroup(true)};
    proxyTranslatorUT.processMethod(mockMethod, data);
  }

  @Test(expected = IllegalArgumentException.class)
  public void When_Use_Evict_Dynamic_Key_Group_Without_Providing_Dynamic_Key_Group_Throw_Exception()
      throws NoSuchMethodException {
    Method mockMethod = ProvidersRxCache.class.getDeclaredMethod(
        "getMockEvictDynamicKeyGroupWithoutProvidingDynamicKeyGroup", Observable.class,
        EvictDynamicKeyGroup.class);
    Object[] data = {Observable.just(new Object[]{}), new EvictDynamicKeyGroup(true)};
    proxyTranslatorUT.processMethod(mockMethod, data);
  }
}
