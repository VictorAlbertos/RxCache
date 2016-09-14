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

package io.rx_cache2.internal;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.rx_cache2.DynamicKey;
import io.rx_cache2.DynamicKeyGroup;
import io.rx_cache2.EvictDynamicKey;
import io.rx_cache2.EvictDynamicKeyGroup;
import io.rx_cache2.EvictProvider;
import io.rx_cache2.ProviderHelper;
import io.rx_cache2.Reply;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
    TestObserver<List<io.rx_cache2.internal.Mock>> testObserver;

    List<io.rx_cache2.internal.Mock> mocksPage1 = createMocks(SIZE);
    String mockPage1Value = mocksPage1.get(0).getMessage();

    testObserver =
        providersRxCache.getMocksPaginate(Observable.just(mocksPage1), new DynamicKey(1)).test();
    testObserver.awaitTerminalEvent();

    List<io.rx_cache2.internal.Mock> mocksPage2 = createMocks(SIZE);
    String mockPage2Value = mocksPage2.get(0).getMessage();

    testObserver =
        providersRxCache.getMocksPaginate(Observable.just(mocksPage2), new DynamicKey(2)).test();
    testObserver.awaitTerminalEvent();

    List<io.rx_cache2.internal.Mock> mocksPage3 = createMocks(SIZE);
    String mockPage3Value = mocksPage3.get(0).getMessage();

    testObserver =
        providersRxCache.getMocksPaginate(Observable.just(mocksPage3), new DynamicKey(3)).test();
    testObserver.awaitTerminalEvent();

    testObserver =
        providersRxCache.getMocksPaginate(ProviderHelper.<List<io.rx_cache2.internal.Mock>>withoutLoader(), new DynamicKey(1))
            .test();
    testObserver.awaitTerminalEvent();
    assertThat(testObserver.values().get(0).get(0).getMessage(), is(mockPage1Value));

    testObserver =
        providersRxCache.getMocksPaginate(ProviderHelper.<List<io.rx_cache2.internal.Mock>>withoutLoader(), new DynamicKey(2))
            .test();
    testObserver.awaitTerminalEvent();
    assertThat(testObserver.values().get(0).get(0).getMessage(), is(mockPage2Value));

    testObserver =
        providersRxCache.getMocksPaginate(ProviderHelper.<List<io.rx_cache2.internal.Mock>>withoutLoader(), new DynamicKey(3))
            .test();
    testObserver.awaitTerminalEvent();
    assertThat(testObserver.values().get(0).get(0).getMessage(), is(mockPage3Value));
  }

  @Test public void Pagination_Evict_All() {
    paginationEvictAll();
  }


  private void paginationEvictAll() {
    TestObserver<List<io.rx_cache2.internal.Mock>> observer = new TestObserver<>();

    EvictProvider evictProviderFalse = new EvictProvider(false);
    EvictProvider evictProviderTrue = new EvictProvider(true);

    providersRxCache.getMocksPaginateEvictProvider(Observable.just(createMocks(SIZE)),
        new DynamicKey(1), evictProviderFalse)
        .subscribe(observer);
    observer.awaitTerminalEvent();
    assertThat(observer.errors().size(), is(0));
    assertThat(observer.values().size(), is(1));

    observer = providersRxCache.getMocksPaginateEvictProvider(Observable.just(createMocks(SIZE)),
        new DynamicKey(2), evictProviderFalse).test();
    observer.awaitTerminalEvent();
    assertThat(observer.errors().size(), is(0));
    assertThat(observer.values().size(), is(1));

    Observable<List<io.rx_cache2.internal.Mock>> oMocks = Observable.<List<io.rx_cache2.internal.Mock>>just(new ArrayList<io.rx_cache2.internal.Mock>());

    observer = new TestObserver<>();
    providersRxCache.getMocksPaginateEvictProvider(oMocks, new DynamicKey(1), evictProviderTrue)
        .subscribe(observer);
    observer.awaitTerminalEvent();

    observer = providersRxCache.getMocksPaginateEvictProvider(oMocks,
        new DynamicKey(1), evictProviderFalse).test();
    observer.awaitTerminalEvent();
    assertThat(observer.values().size(), is(1));
    assertThat(observer.values().get(0).size(), is(0));

    observer = providersRxCache.getMocksPaginateEvictProvider(oMocks,
        new DynamicKey(2), evictProviderFalse).test();
    observer.awaitTerminalEvent();
    assertThat(observer.values().size(), is(1));
    assertThat(observer.values().get(0).size(), is(0));
  }

  @Test public void Pagination_With_Evict_Cache_By_Page() {
    TestObserver<Reply<List<io.rx_cache2.internal.Mock>>> observer;

    observer = providersRxCache.getMocksDynamicKeyEvictPage(Observable.just(createMocks(SIZE)),
        new DynamicKey(1), new EvictDynamicKey(true)).test();
    observer.awaitTerminalEvent();

    observer = providersRxCache.getMocksDynamicKeyEvictPage(Observable.just(createMocks(SIZE)),
        new DynamicKey(2), new EvictDynamicKey(true)).test();
    observer.awaitTerminalEvent();

    observer = providersRxCache.getMocksDynamicKeyEvictPage(ProviderHelper.<List<io.rx_cache2.internal.Mock>>withoutLoader(),
        new DynamicKey(1), new EvictDynamicKey(true)).test();
    observer.awaitTerminalEvent();
    assertThat(observer.errors().size(), is(1));
    assertThat(observer.values().size(), is(0));

    observer = providersRxCache.getMocksDynamicKeyEvictPage(ProviderHelper.<List<io.rx_cache2.internal.Mock>>withoutLoader(),
        new DynamicKey(2), new EvictDynamicKey(false))
        .test();
    observer.awaitTerminalEvent();
    assertThat(observer.errors().size(), is(0));
    assertThat(observer.values().size(), is(1));
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
    TestObserver<List<io.rx_cache2.internal.Mock>> subscriber = new TestObserver<>();
    List<io.rx_cache2.internal.Mock> mocksFilterPage = getMock(filter_page);

    String filter = filter_page.split("_")[0];
    String page = filter_page.split("_")[1];

    providersRxCache.getMocksFilteredPaginateEvict(Observable.just(mocksFilterPage),
        new DynamicKeyGroup(filter, page), new EvictDynamicKeyGroup(false)).subscribe(subscriber);
    subscriber.awaitTerminalEvent();
  }

  private void retrieveAndCheckFilterPageValue(String filter_page, boolean shouldThrowException) {
    String filter = filter_page.split("_")[0];
    String page = filter_page.split("_")[1];

    TestObserver<List<io.rx_cache2.internal.Mock>> observer =
        providersRxCache.getMocksFilteredPaginateEvict(ProviderHelper.<List<io.rx_cache2.internal.Mock>>withoutLoader(),
            new DynamicKeyGroup(filter, page), new EvictDynamicKeyGroup(false)).test();
    observer.awaitTerminalEvent();

    if (shouldThrowException) {
      observer.assertNoValues();
      observer.assertError(Exception.class);
    } else {
      observer.assertNoErrors();
      String valueFilterPage = observer.values().get(0).get(0).getMessage();
      assertThat(valueFilterPage, is(filter_page));
    }
  }

  private void evictProviderKey(String filter_page) {
    TestObserver<List<io.rx_cache2.internal.Mock>> subscriber = new TestObserver<>();

    String filter = filter_page.split("_")[0];
    String page = filter_page.split("_")[1];

    providersRxCache.getMocksFilteredPaginateEvict(ProviderHelper.<List<io.rx_cache2.internal.Mock>>withoutLoader(),
        new DynamicKeyGroup(filter, page), new EvictProvider(true)).subscribe(subscriber);
    subscriber.awaitTerminalEvent();
  }

  private void evictDynamicKey(String filter_page) {
    TestObserver<List<io.rx_cache2.internal.Mock>> subscriber = new TestObserver<>();

    String filter = filter_page.split("_")[0];
    String page = filter_page.split("_")[1];

    providersRxCache.getMocksFilteredPaginateEvict(ProviderHelper.<List<io.rx_cache2.internal.Mock>>withoutLoader(),
        new DynamicKeyGroup(filter, page), new EvictDynamicKey(true)).subscribe(subscriber);
    subscriber.awaitTerminalEvent();
  }

  private void evictDynamicKeyGroup(String filter_page) {
    TestObserver<List<io.rx_cache2.internal.Mock>> subscriber = new TestObserver<>();

    String filter = filter_page.split("_")[0];
    String page = filter_page.split("_")[1];

    providersRxCache.getMocksFilteredPaginateEvict(ProviderHelper.<List<io.rx_cache2.internal.Mock>>withoutLoader(),
        new DynamicKeyGroup(filter, page), new EvictDynamicKeyGroup(true)).subscribe(subscriber);
    subscriber.awaitTerminalEvent();
  }

  private List<io.rx_cache2.internal.Mock> getMock(String value) {
    return Arrays.asList(new io.rx_cache2.internal.Mock(value));
  }

  private List<io.rx_cache2.internal.Mock> createMocks(int size) {
    long currentTime = System.currentTimeMillis();

    List<io.rx_cache2.internal.Mock> mocks = new ArrayList(size);
    for (int i = 0; i < size; i++) {
      mocks.add(new io.rx_cache2.internal.Mock("mock" + currentTime));
    }

    return mocks;
  }
}
