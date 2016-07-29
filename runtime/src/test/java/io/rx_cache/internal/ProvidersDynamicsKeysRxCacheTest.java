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

import io.rx_cache.DynamicKey;
import io.rx_cache.DynamicKeyGroup;
import io.rx_cache.EvictDynamicKey;
import io.rx_cache.EvictDynamicKeyGroup;
import io.rx_cache.EvictProvider;
import io.rx_cache.Reply;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import rx.Observable;
import rx.Subscriber;
import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ProvidersDynamicsKeysRxCacheTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private ProvidersRxCache providersRxCache;
  private static final int SIZE = 100;
  private static final String filter1page1 = "filer1_page1", filter1Page2 = "filer1_page2",
      filter1Page3 = "filer1_page3",
      filter2Page1 = "filer2_page1", filter2Page2 = "filer2_page2", filter2Page3 = "filer2_page3";

  @Before public void setUp() {
    providersRxCache = new RxCache.Builder()
        .persistence(temporaryFolder.getRoot(), Jolyglot$.newInstance())
        .using(ProvidersRxCache.class);
  }

  @Test public void Pagination() {
    TestSubscriber<List<Mock>> subscriber;

    List<Mock> mocksPage1 = createMocks(SIZE);
    String mockPage1Value = mocksPage1.get(0).getMessage();

    subscriber = new TestSubscriber<>();
    providersRxCache.getMocksPaginate(Observable.just(mocksPage1), new DynamicKey(1))
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    List<Mock> mocksPage2 = createMocks(SIZE);
    String mockPage2Value = mocksPage2.get(0).getMessage();

    subscriber = new TestSubscriber<>();
    providersRxCache.getMocksPaginate(Observable.just(mocksPage2), new DynamicKey(2))
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    List<Mock> mocksPage3 = createMocks(SIZE);
    String mockPage3Value = mocksPage3.get(0).getMessage();

    subscriber = new TestSubscriber<>();
    providersRxCache.getMocksPaginate(Observable.just(mocksPage3), new DynamicKey(3))
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    subscriber = new TestSubscriber<>();
    providersRxCache.getMocksPaginate(Observable.<List<Mock>>just(null), new DynamicKey(1))
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    assertThat(subscriber.getOnNextEvents().get(0).get(0).getMessage(), is(mockPage1Value));

    subscriber = new TestSubscriber<>();
    providersRxCache.getMocksPaginate(Observable.<List<Mock>>just(null), new DynamicKey(2))
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    assertThat(subscriber.getOnNextEvents().get(0).get(0).getMessage(), is(mockPage2Value));

    subscriber = new TestSubscriber<>();
    providersRxCache.getMocksPaginate(Observable.<List<Mock>>just(null), new DynamicKey(3))
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    assertThat(subscriber.getOnNextEvents().get(0).get(0).getMessage(), is(mockPage3Value));
  }

  @Test public void Pagination_Evict_All_Using_Null_Value_Observable() {
    paginationEvictAll(false);
  }

  @Test public void Pagination_Evict_All_Using_Error_Observable() {
    paginationEvictAll(true);
  }

  private void paginationEvictAll(boolean usingErrorObservableInsteadOfNull) {
    TestSubscriber<List<Mock>> subscriber;

    EvictProvider evictProviderFalse = new EvictProvider(false);
    EvictProvider evictProviderTrue = new EvictProvider(true);

    subscriber = new TestSubscriber<>();
    providersRxCache.getMocksPaginateEvictProvider(Observable.just(createMocks(SIZE)),
        new DynamicKey(1), evictProviderFalse).subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    assertThat(subscriber.getOnErrorEvents().size(), is(0));
    assertThat(subscriber.getOnNextEvents().size(), is(1));

    subscriber = new TestSubscriber<>();
    providersRxCache.getMocksPaginateEvictProvider(Observable.just(createMocks(SIZE)),
        new DynamicKey(2), evictProviderFalse).subscribe(subscriber);
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
    providersRxCache.getMocksPaginateEvictProvider(oMocks, new DynamicKey(1), evictProviderTrue)
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    subscriber = new TestSubscriber<>();
    providersRxCache.getMocksPaginateEvictProvider(Observable.<List<Mock>>just(null),
        new DynamicKey(1), evictProviderFalse).subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    assertThat(subscriber.getOnErrorEvents().size(), is(1));
    assertThat(subscriber.getOnNextEvents().size(), is(0));

    subscriber = new TestSubscriber<>();
    providersRxCache.getMocksPaginateEvictProvider(Observable.<List<Mock>>just(null),
        new DynamicKey(2), evictProviderFalse).subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    assertThat(subscriber.getOnErrorEvents().size(), is(1));
    assertThat(subscriber.getOnNextEvents().size(), is(0));
  }

  @Test public void Pagination_With_Evict_Cache_By_Page() {
    TestSubscriber<Reply<List<Mock>>> subscriber;

    subscriber = new TestSubscriber<>();
    providersRxCache.getMocksDynamicKeyEvictPage(Observable.just(createMocks(SIZE)),
        new DynamicKey(1), new EvictDynamicKey(true)).subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    subscriber = new TestSubscriber<>();
    providersRxCache.getMocksDynamicKeyEvictPage(Observable.just(createMocks(SIZE)),
        new DynamicKey(2), new EvictDynamicKey(true)).subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    subscriber = new TestSubscriber<>();
    providersRxCache.getMocksDynamicKeyEvictPage(Observable.<List<Mock>>just(null),
        new DynamicKey(1), new EvictDynamicKey(true)).subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    assertThat(subscriber.getOnErrorEvents().size(), is(1));
    assertThat(subscriber.getOnNextEvents().size(), is(0));

    subscriber = new TestSubscriber<>();
    providersRxCache.getMocksDynamicKeyEvictPage(Observable.<List<Mock>>just(null),
        new DynamicKey(2), new EvictDynamicKey(false))
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    assertThat(subscriber.getOnErrorEvents().size(), is(0));
    assertThat(subscriber.getOnNextEvents().size(), is(1));
  }

  @Test public void Pagination_Filtering_Evicting_DynamicKeyGroup() {
    populateAndCheckRetrieved();

    evictDynamicKeyGroup(filter1page1);
    retrieveAndCheckFilterPageValue(filter1page1, true);
    retrieveAndCheckFilterPageValue(filter1Page2, false);
    retrieveAndCheckFilterPageValue(filter1Page3, false);
    retrieveAndCheckFilterPageValue(filter2Page1, false);

    evictDynamicKeyGroup(filter1Page2);
    retrieveAndCheckFilterPageValue(filter1Page2, true);
    retrieveAndCheckFilterPageValue(filter1Page3, false);
    retrieveAndCheckFilterPageValue(filter2Page2, false);

    evictDynamicKeyGroup(filter1Page3);
    retrieveAndCheckFilterPageValue(filter1Page3, true);
    retrieveAndCheckFilterPageValue(filter2Page3, false);

    evictDynamicKeyGroup(filter2Page1);
    retrieveAndCheckFilterPageValue(filter2Page1, true);
    retrieveAndCheckFilterPageValue(filter2Page2, false);
    retrieveAndCheckFilterPageValue(filter2Page3, false);

    evictDynamicKeyGroup(filter2Page2);
    retrieveAndCheckFilterPageValue(filter2Page2, true);
    retrieveAndCheckFilterPageValue(filter2Page3, false);

    evictDynamicKeyGroup(filter2Page3);
    retrieveAndCheckFilterPageValue(filter2Page3, true);

    populateAndCheckRetrieved();
  }

  @Test public void Pagination_Filtering_Evicting_DynamicKey() {
    populateAndCheckRetrieved();

    evictDynamicKey(filter1Page2);
    retrieveAndCheckFilterPageValue(filter1page1, true);
    retrieveAndCheckFilterPageValue(filter1Page2, true);
    retrieveAndCheckFilterPageValue(filter1Page3, true);
    retrieveAndCheckFilterPageValue(filter2Page1, false);

    evictDynamicKey(filter2Page1);
    retrieveAndCheckFilterPageValue(filter2Page1, true);
    retrieveAndCheckFilterPageValue(filter2Page2, true);
    retrieveAndCheckFilterPageValue(filter2Page3, true);

    populateAndCheckRetrieved();
  }

  @Test public void Pagination_Filtering_Evicting_ProviderKey() {
    populateAndCheckRetrieved();

    evictProviderKey(filter1Page2);
    retrieveAndCheckFilterPageValue(filter1page1, true);
    retrieveAndCheckFilterPageValue(filter1Page2, true);
    retrieveAndCheckFilterPageValue(filter1Page3, true);
    retrieveAndCheckFilterPageValue(filter2Page1, true);
    retrieveAndCheckFilterPageValue(filter2Page2, true);
    retrieveAndCheckFilterPageValue(filter2Page3, true);

    populateAndCheckRetrieved();
  }

  private void populateAndCheckRetrieved() {
    populateFilterPage(filter1page1);
    populateFilterPage(filter1Page2);
    populateFilterPage(filter1Page3);
    populateFilterPage(filter2Page1);
    populateFilterPage(filter2Page2);
    populateFilterPage(filter2Page3);

    retrieveAndCheckFilterPageValue(filter1page1, false);
    retrieveAndCheckFilterPageValue(filter1Page2, false);
    retrieveAndCheckFilterPageValue(filter1Page3, false);
    retrieveAndCheckFilterPageValue(filter2Page1, false);
    retrieveAndCheckFilterPageValue(filter2Page2, false);
    retrieveAndCheckFilterPageValue(filter2Page3, false);
  }

  private void populateFilterPage(String filter_page) {
    TestSubscriber<List<Mock>> subscriber = new TestSubscriber<>();
    List<Mock> mocksFilterPage = getMock(filter_page);

    String filter = filter_page.split("_")[0];
    String page = filter_page.split("_")[1];

    providersRxCache.getMocksFilteredPaginateEvict(Observable.just(mocksFilterPage),
        new DynamicKeyGroup(filter, page), new EvictDynamicKeyGroup(false)).subscribe(subscriber);
    subscriber.awaitTerminalEvent();
  }

  private void retrieveAndCheckFilterPageValue(String filter_page, boolean shouldThrowException) {
    TestSubscriber<List<Mock>> subscriber = new TestSubscriber<>();

    String filter = filter_page.split("_")[0];
    String page = filter_page.split("_")[1];

    providersRxCache.getMocksFilteredPaginateEvict(Observable.<List<Mock>>just(null),
        new DynamicKeyGroup(filter, page), new EvictDynamicKeyGroup(false)).subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    if (shouldThrowException) {
      subscriber.assertNoValues();
      subscriber.assertError(Exception.class);
    } else {
      subscriber.assertNoErrors();
      String valueFilterPage = subscriber.getOnNextEvents().get(0).get(0).getMessage();
      assertThat(valueFilterPage, is(filter_page));
    }
  }

  private void evictProviderKey(String filter_page) {
    TestSubscriber<List<Mock>> subscriber = new TestSubscriber<>();

    String filter = filter_page.split("_")[0];
    String page = filter_page.split("_")[1];

    providersRxCache.getMocksFilteredPaginateEvict(Observable.<List<Mock>>just(null),
        new DynamicKeyGroup(filter, page), new EvictProvider(true)).subscribe(subscriber);
    subscriber.awaitTerminalEvent();
  }

  private void evictDynamicKey(String filter_page) {
    TestSubscriber<List<Mock>> subscriber = new TestSubscriber<>();

    String filter = filter_page.split("_")[0];
    String page = filter_page.split("_")[1];

    providersRxCache.getMocksFilteredPaginateEvict(Observable.<List<Mock>>just(null),
        new DynamicKeyGroup(filter, page), new EvictDynamicKey(true)).subscribe(subscriber);
    subscriber.awaitTerminalEvent();
  }

  private void evictDynamicKeyGroup(String filter_page) {
    TestSubscriber<List<Mock>> subscriber = new TestSubscriber<>();

    String filter = filter_page.split("_")[0];
    String page = filter_page.split("_")[1];

    providersRxCache.getMocksFilteredPaginateEvict(Observable.<List<Mock>>just(null),
        new DynamicKeyGroup(filter, page), new EvictDynamicKeyGroup(true)).subscribe(subscriber);
    subscriber.awaitTerminalEvent();
  }

  private List<Mock> getMock(String value) {
    return Arrays.asList(new Mock(value));
  }

  private List<Mock> createMocks(int size) {
    long currentTime = System.currentTimeMillis();

    List<Mock> mocks = new ArrayList(size);
    for (int i = 0; i < size; i++) {
      mocks.add(new Mock("mock" + currentTime));
    }

    return mocks;
  }
}
