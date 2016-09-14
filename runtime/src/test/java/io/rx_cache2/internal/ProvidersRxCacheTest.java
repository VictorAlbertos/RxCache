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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;
import io.rx_cache2.DynamicKey;
import io.rx_cache2.EvictProvider;
import io.rx_cache2.ProviderHelper;
import io.rx_cache2.Reply;
import io.rx_cache2.Source;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.MethodSorters;

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
  private RxCache rxCache;
  private final static int SIZE = 100;

  private void initProviders(boolean useExpiredDataIfLoaderNotAvailable) {
    rxCache = new RxCache.Builder()
        .useExpiredDataIfLoaderNotAvailable(useExpiredDataIfLoaderNotAvailable)
        .persistence(temporaryFolder.getRoot(), Jolyglot$.newInstance());

    providersRxCache = rxCache
        .using(ProvidersRxCache.class);
  }

  @Test public void _00_Use_Expired_Data() {
    initProviders(true);

    TestObserver<Reply<List<io.rx_cache2.internal.Mock>>> observer;

    observer = new TestObserver<>();
    providersRxCache.getMocksListResponseOneSecond(createObservableMocks(SIZE))
        .subscribe(observer);
    observer.awaitTerminalEvent();

    waitTime(1500);

    observer = new TestObserver<>();
    providersRxCache.getMocksListResponseOneSecond(Observable.just(Arrays.asList(new io.rx_cache2.internal.Mock())))
        .subscribe(observer);
    observer.awaitTerminalEvent();

    Reply<List<io.rx_cache2.internal.Mock>> reply = observer.values().get(0);
    assertThat(reply.getData().size(), is(SIZE));
    assertThat(observer.errors().size(), is(0));
  }

  @Test public void _01_Before_Destroy_Memory() {
    initProviders(false);

    TestObserver<Reply<List<io.rx_cache2.internal.Mock>>> subscriber;

    subscriber = new TestObserver<>();
    providersRxCache.getMocksWithDetailResponse(createObservableMocks(SIZE)).subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    Reply<List<io.rx_cache2.internal.Mock>> reply = subscriber.values().get(0);
    assertThat(reply.getSource(), CoreMatchers.is(Source.CLOUD));
    assertThat(subscriber.values().get(0).getData().size(), is(SIZE));

    subscriber = new TestObserver<>();
    providersRxCache.getMocksWithDetailResponse(createObservableMocks(SIZE)).subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    reply = subscriber.values().get(0);
    assertThat(reply.getSource(), is(Source.MEMORY));
    assertThat(reply.getData().size(), is(SIZE));
  }

  @Test public void _02_After_Memory_Destroyed() {
    initProviders(false);

    TestObserver<Reply<List<io.rx_cache2.internal.Mock>>> subscriber;

    subscriber = new TestObserver<>();
    providersRxCache.getMocksWithDetailResponse(createObservableMocks(SIZE)).subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    Reply<List<io.rx_cache2.internal.Mock>> reply = subscriber.values().get(0);
    assertThat(reply.getSource(), is(Source.PERSISTENCE));
    assertThat(subscriber.values().get(0).getData().size(), is(SIZE));

    subscriber = new TestObserver<>();
    providersRxCache.getMocksListResponseOneSecond(createObservableMocks(SIZE))
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    reply = subscriber.values().get(0);
    assertThat(reply.getSource(), is(Source.CLOUD));
    assertThat(subscriber.values().get(0).getData().size(), is(SIZE));

    waitTime(1100);

    subscriber = new TestObserver<>();
    providersRxCache.getMocksListResponseOneSecond(createObservableMocks(SIZE))
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    reply = subscriber.values().get(0);
    assertThat(reply.getSource(), is(Source.CLOUD));
    assertThat(subscriber.values().get(0).getData().size(), is(SIZE));

    subscriber = new TestObserver<>();
    providersRxCache.getMocksListResponseOneSecond(createObservableMocks(SIZE))
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    reply = subscriber.values().get(0);
    assertThat(reply.getSource(), is(Source.MEMORY));
    assertThat(subscriber.values().get(0).getData().size(), is(SIZE));
  }

  @Test public void _03_Evicting_Cache() {
    initProviders(false);

    TestObserver<Reply<List<io.rx_cache2.internal.Mock>>> subscriber;

    subscriber = new TestObserver<>();
    providersRxCache.getMocksEvictProvider(createObservableMocks(SIZE), new EvictProvider(false))
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    Reply<List<io.rx_cache2.internal.Mock>> reply = subscriber.values().get(0);
    assertThat(reply.getSource(), is(Source.CLOUD));
    assertThat(subscriber.values().get(0).getData().size(), is(SIZE));

    subscriber = new TestObserver<>();
    providersRxCache.getMocksEvictProvider(createObservableMocks(SIZE), new EvictProvider(false))
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    reply = subscriber.values().get(0);
    assertThat(reply.getSource(), is(Source.MEMORY));
    assertThat(subscriber.values().get(0).getData().size(), is(SIZE));

    subscriber = new TestObserver<>();
    providersRxCache.getMocksEvictProvider(createObservableMocks(SIZE), new EvictProvider(true))
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    reply = subscriber.values().get(0);
    assertThat(reply.getSource(), is(Source.CLOUD));
    assertThat(subscriber.values().get(0).getData().size(), is(SIZE));
  }

  @Test public void _04_Session_Mock() {
    initProviders(false);

    TestObserver<io.rx_cache2.internal.Mock> subscriber = new TestObserver<>();
    io.rx_cache2.internal.Mock mock = createMocks(SIZE).get(0);

    //not logged mock
    providersRxCache.getLoggedMock(ProviderHelper.<io.rx_cache2.internal.Mock>withoutLoader(), new EvictProvider(false))
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    assertThat(subscriber.values().size(), is(0));
    assertThat(subscriber.errors().size(), is(1));

    //login mock
    subscriber = new TestObserver<>();
    providersRxCache.getLoggedMock(Observable.just(mock), new EvictProvider(true))
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    assertNotNull(subscriber.values().get(0));

    //logged mock
    subscriber = new TestObserver<>();
    providersRxCache.getLoggedMock(ProviderHelper.<io.rx_cache2.internal.Mock>withoutLoader(), new EvictProvider(false))
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    assertNotNull(subscriber.values().get(0));

    //logout mock
    subscriber = new TestObserver<>();
    providersRxCache.getLoggedMock(ProviderHelper.<io.rx_cache2.internal.Mock>withoutLoader(), new EvictProvider(true))
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    assertThat(subscriber.values().size(), is(0));
    assertThat(subscriber.errors().size(), is(1));

    //not logged mock
    subscriber = new TestObserver<>();
    providersRxCache.getLoggedMock(ProviderHelper.<io.rx_cache2.internal.Mock>withoutLoader(), new EvictProvider(false))
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    assertThat(subscriber.values().size(), is(0));
    assertThat(subscriber.errors().size(), is(1));
  }

  @Test public void _06_Not_Use_Expired_Data() {
    initProviders(false);

    TestObserver<Reply<List<io.rx_cache2.internal.Mock>>> subscriber;

    subscriber = new TestObserver<>();
    providersRxCache.getMocksListResponseOneSecond(createObservableMocks(SIZE))
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    waitTime(1100);

    subscriber = new TestObserver<>();
    providersRxCache.getMocksListResponseOneSecond(ProviderHelper.<List<io.rx_cache2.internal.Mock>>withoutLoader())
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    assertThat(subscriber.errors().size(), is(1));
    assertThat(subscriber.values().size(), is(0));
  }

  @Test public void _07_When_Retrieve_Cached_Data_After_Remove_Item_List_Then_Item_Still_Remains() {
    initProviders(false);

    TestObserver<Reply<List<io.rx_cache2.internal.Mock>>> subscriber;

    subscriber = new TestObserver<>();
    providersRxCache.getMocksWithDetailResponse(createObservableMocks(SIZE)).subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    Reply<List<io.rx_cache2.internal.Mock>> reply = subscriber.values().get(0);
    assertThat(reply.getData().size(), is(SIZE));
    reply.getData().remove(0);
    assertThat(reply.getData().size(), is(SIZE - 1));

    subscriber = new TestObserver<>();
    providersRxCache.getMocksWithDetailResponse(ProviderHelper.<List<io.rx_cache2.internal.Mock>>withoutLoader())
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    assertThat(subscriber.errors().size(), is(0));
    assertThat(subscriber.values().size(), is(1));
    reply = subscriber.values().get(0);
    assertThat(reply.getData().size(), is(SIZE));
  }

  @Test
  public void _08_When_Retrieve_Cached_Data_After_Remove_Item_Array_Then_Item_Still_Remains() {
    initProviders(false);

    TestObserver<Reply<io.rx_cache2.internal.Mock[]>> subscriber;

    subscriber = new TestObserver<>();
    providersRxCache.getMocksArrayResponse(createObservableMocksArray(SIZE)).subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    Reply<io.rx_cache2.internal.Mock[]> reply = subscriber.values().get(0);
    assertThat(reply.getData().length, is(SIZE));
    reply = new Reply<>(Arrays.copyOf(reply.getData(), reply.getData().length - 1), reply.getSource(), false);
    assertThat(reply.getData().length, is(SIZE - 1));

    subscriber = new TestObserver<>();
    providersRxCache.getMocksArrayResponse(ProviderHelper.<io.rx_cache2.internal.Mock[]>withoutLoader()).subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    assertThat(subscriber.errors().size(), is(0));
    assertThat(subscriber.values().size(), is(1));
    reply = subscriber.values().get(0);
    assertThat(reply.getData().length, is(SIZE));
  }

  @Test public void _09_When_Retrieve_Cached_Data_After_Remove_Item_Map_Then_Item_Still_Remains() {
    initProviders(false);

    TestObserver<Reply<Map<Integer, io.rx_cache2.internal.Mock>>> subscriber;

    subscriber = new TestObserver<>();
    providersRxCache.getMocksMapResponse(createObservableMocksMap(SIZE)).subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    Reply<Map<Integer, io.rx_cache2.internal.Mock>> reply = subscriber.values().get(0);
    assertThat(reply.getData().size(), is(SIZE));
    reply.getData().remove(0);
    assertThat(reply.getData().size(), is(SIZE - 1));

    subscriber = new TestObserver<>();
    providersRxCache.getMocksMapResponse(ProviderHelper.<Map<Integer, io.rx_cache2.internal.Mock>>withoutLoader())
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    assertThat(subscriber.errors().size(), is(0));
    assertThat(subscriber.values().size(), is(1));
    reply = subscriber.values().get(0);
    assertThat(reply.getData().size(), is(SIZE));
  }

  @Test
  public void _10_When_Retrieve_Cached_Data_After_Modified_Object_On_Item_List_Then_Object_Preserves_Initial_State() {
    initProviders(false);

    TestObserver<Reply<List<io.rx_cache2.internal.Mock>>> subscriber;

    subscriber = new TestObserver<>();
    providersRxCache.getMocksWithDetailResponse(createObservableMocks(SIZE)).subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    Reply<List<io.rx_cache2.internal.Mock>> reply = subscriber.values().get(0);
    Type type = new TypeToken<Reply<List<io.rx_cache2.internal.Mock>>>() {
    }.getType();
    Reply<List<io.rx_cache2.internal.Mock>> replyOriginal = (Reply<List<io.rx_cache2.internal.Mock>>) deepCopy(reply, type);
    assertThat(compare(reply, replyOriginal, type), is(true));
    reply.getData().get(0).setMessage("modified");
    assertThat(compare(reply, replyOriginal, type), is(false));

    subscriber = new TestObserver<>();
    providersRxCache.getMocksWithDetailResponse(ProviderHelper.<List<io.rx_cache2.internal.Mock>>withoutLoader())
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    assertThat(subscriber.errors().size(), is(0));
    assertThat(subscriber.values().size(), is(1));
    reply = subscriber.values().get(0);
    type = new TypeToken<List<io.rx_cache2.internal.Mock>>() {
    }.getType();
    assertThat(compare(reply.getData(), replyOriginal.getData(), type), is(true));
  }

  @Test
  public void _11_When_Retrieve_Cached_Data_After_Modified_Object_On_Item_Array_Then_Object_Preserves_Initial_State() {
    initProviders(false);

    TestObserver<Reply<io.rx_cache2.internal.Mock[]>> subscriber;

    subscriber = new TestObserver<>();
    providersRxCache.getMocksArrayResponse(createObservableMocksArray(SIZE)).subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    Reply<io.rx_cache2.internal.Mock[]> reply = subscriber.values().get(0);
    Type type = new TypeToken<Reply<io.rx_cache2.internal.Mock[]>>() {
    }.getType();
    Reply<io.rx_cache2.internal.Mock[]> replyOriginal = (Reply<io.rx_cache2.internal.Mock[]>) deepCopy(reply, type);
    assertThat(compare(reply, replyOriginal, type), is(true));
    reply.getData()[0].setMessage("modified");
    assertThat(compare(reply, replyOriginal, type), is(false));

    subscriber = new TestObserver<>();
    providersRxCache.getMocksArrayResponse(ProviderHelper.<io.rx_cache2.internal.Mock[]>withoutLoader()).subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    assertThat(subscriber.errors().size(), is(0));
    assertThat(subscriber.values().size(), is(1));
    reply = subscriber.values().get(0);
    type = new TypeToken<io.rx_cache2.internal.Mock[]>() {
    }.getType();
    assertThat(compare(reply.getData(), replyOriginal.getData(), type), is(true));
  }

  @Test
  public void _12_When_Retrieve_Cached_Data_After_Modified_Object_On_Item_Map_Then_Object_Preserves_Initial_State() {
    initProviders(false);

    TestObserver<Reply<Map<Integer, io.rx_cache2.internal.Mock>>> subscriber;

    subscriber = new TestObserver<>();
    providersRxCache.getMocksMapResponse(createObservableMocksMap(SIZE)).subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    Reply<Map<Integer, io.rx_cache2.internal.Mock>> reply = subscriber.values().get(0);
    Type type = new TypeToken<Reply<Map<Integer, io.rx_cache2.internal.Mock>>>() {
    }.getType();
    Reply<Map<Integer, io.rx_cache2.internal.Mock>> replyOriginal = (Reply<Map<Integer, io.rx_cache2.internal.Mock>>) deepCopy(reply, type);
    assertThat(compare(reply, replyOriginal, type), is(true));
    reply.getData().get(0).setMessage("modified");
    assertThat(compare(reply, replyOriginal, type), is(false));

    subscriber = new TestObserver<>();
    providersRxCache.getMocksMapResponse(ProviderHelper.<Map<Integer, io.rx_cache2.internal.Mock>>withoutLoader())
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    assertThat(subscriber.errors().size(), is(0));
    assertThat(subscriber.values().size(), is(1));
    reply = subscriber.values().get(0);
    type = new TypeToken<Map<Integer, io.rx_cache2.internal.Mock>>() {
    }.getType();
    assertThat(compare(reply.getData(), replyOriginal.getData(), type), is(true));
  }

  @Test
  public void _13_When_Retrieve_Cached_Data_After_Modified_Object_Then_Object_Preserves_Initial_State() {
    initProviders(false);

    TestObserver<io.rx_cache2.internal.Mock> subscriber;

    subscriber = new TestObserver<>();
    providersRxCache.getLoggedMock(Observable.<io.rx_cache2.internal.Mock>just(createMocks(SIZE).get(0)),
        new EvictProvider(true)).subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    io.rx_cache2.internal.Mock mock = subscriber.values().get(0);
    Type type = new TypeToken<io.rx_cache2.internal.Mock>() {
    }.getType();
    io.rx_cache2.internal.Mock mockOriginal = (io.rx_cache2.internal.Mock) deepCopy(mock, type);
    assertThat(compare(mock, mockOriginal, type), is(true));
    mock.setMessage("modified");
    assertThat(compare(mock, mockOriginal, type), is(false));

    subscriber = new TestObserver<>();
    providersRxCache.getLoggedMock(ProviderHelper.<io.rx_cache2.internal.Mock>withoutLoader(), new EvictProvider(false))
        .subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    assertThat(subscriber.errors().size(), is(0));
    assertThat(subscriber.values().size(), is(1));
    mock = subscriber.values().get(0);
    assertThat(compare(mock, mockOriginal, type), is(true));
  }

  @Test public void _14_When_0_Is_The_Value_For_Life_Time_Not_Cached_Ad_Infinitum() {
    initProviders(false);

    TestObserver<Reply<List<io.rx_cache2.internal.Mock>>> subscriber;
    subscriber = new TestObserver<>();
    providersRxCache.getMocksLife0Minutes(createObservableMocks(10)).subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    Reply<List<io.rx_cache2.internal.Mock>> reply = subscriber.values().get(0);
    assertThat(reply.getSource(), is(Source.CLOUD));

    subscriber = new TestObserver<>();
    providersRxCache.getMocksLife0Minutes(createObservableMocks(10)).subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    reply = subscriber.values().get(0);
    assertThat(reply.getSource(), is(Source.CLOUD));
  }

  @Test public void _15_When_Evict_All_Evict_All() {
    initProviders(false);

    TestObserver<Void> subscriberEvict = new TestObserver<>();
    rxCache.evictAll().subscribe(subscriberEvict);
    subscriberEvict.awaitTerminalEvent();


    for (int i = 0; i < SIZE; i++) {
      TestObserver<List<io.rx_cache2.internal.Mock>> subscriber = new TestObserver<>();
      providersRxCache.getMocksPaginate(createObservableMocks(1), new DynamicKey(i))
          .subscribe(subscriber);
      subscriber.awaitTerminalEvent();
    }

    assertThat(temporaryFolder.getRoot().listFiles().length, is(SIZE));

    subscriberEvict = new TestObserver<>();
    rxCache.evictAll().subscribe(subscriberEvict);
    subscriberEvict.awaitTerminalEvent();

    subscriberEvict.assertComplete();
    subscriberEvict.assertNoErrors();
    subscriberEvict.assertNoValues();

    assertThat(temporaryFolder.getRoot().listFiles().length, is(0));
  }

  private Object deepCopy(Object object, Type type) {
    return new Gson().fromJson(new Gson().toJson(object), type);
  }

  private boolean compare(Object object1, Object object2, Type type) {
    return new Gson().toJson(object1, type).equals(new Gson().toJson(object2, type));
  }

  private void waitTime(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private Observable<List<io.rx_cache2.internal.Mock>> createObservableMocks(int size) {
    long currentTime = System.currentTimeMillis();

    List<io.rx_cache2.internal.Mock> mocks = new ArrayList(size);
    for (int i = 0; i < size; i++) {
      mocks.add(new io.rx_cache2.internal.Mock("mock" + currentTime));
    }

    return Observable.just(mocks);
  }

  private Observable<Map<Integer, io.rx_cache2.internal.Mock>> createObservableMocksMap(int size) {
    long currentTime = System.currentTimeMillis();

    Map<Integer, io.rx_cache2.internal.Mock> mocks = new HashMap<>();
    for (int i = 0; i < size; i++) {
      mocks.put(i, new io.rx_cache2.internal.Mock("mock" + currentTime));
    }

    return Observable.just(mocks);
  }

  private Observable<io.rx_cache2.internal.Mock[]> createObservableMocksArray(int size) {
    return createObservableMocks(size)
        .map(new Function<List<io.rx_cache2.internal.Mock>, io.rx_cache2.internal.Mock[]>() {
          @Override
          public io.rx_cache2.internal.Mock[] apply(List<io.rx_cache2.internal.Mock> mockList) throws Exception {
            return mockList.toArray(new io.rx_cache2.internal.Mock[mockList.size()]);
          }
        });
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
