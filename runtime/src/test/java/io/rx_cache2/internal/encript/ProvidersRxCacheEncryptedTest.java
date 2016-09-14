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

package io.rx_cache2.internal.encript;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.rx_cache2.Encrypt;
import io.rx_cache2.EncryptKey;
import io.rx_cache2.ProviderHelper;
import io.rx_cache2.Reply;
import io.rx_cache2.Source;
import io.rx_cache2.internal.Jolyglot$;
import io.rx_cache2.internal.Mock;
import io.rx_cache2.internal.RxCache;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.MethodSorters;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by miguel on 01/06/16.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProvidersRxCacheEncryptedTest {
  private final static int SIZE = 1000;
  @ClassRule public static TemporaryFolder temporaryFolder = new TemporaryFolder();
  private ProvidersRxCache providersRxCache;

  @Before public void init() {
    providersRxCache = new RxCache.Builder()
        .persistence(temporaryFolder.getRoot(), Jolyglot$.newInstance())
        .using(ProvidersRxCache.class);
  }

  @Test public void _00_Save_Record_On_Disk_In_Order_To_Test_Following_Tests() {
    TestObserver<Reply<List<Mock>>> observer =
        providersRxCache.getMocksEncryptedWithDetailResponse(createObservableMocks(SIZE)).test();
    observer.awaitTerminalEvent();

    observer = new TestObserver<>();
    providersRxCache.getMocksNotEncryptedWithDetailResponse(createObservableMocks(SIZE))
        .subscribe(observer);

    observer.awaitTerminalEvent();
  }

  @Test
  public void _01_When_Encrypted_Record_Has_Been_Persisted_And_Memory_Has_Been_Destroyed_Then_Retrieve_From_Disk() {
    TestObserver<Reply<List<Mock>>> observer =
        providersRxCache.getMocksEncryptedWithDetailResponse(ProviderHelper.<List<Mock>>withoutLoader())
            .test();
    observer.awaitTerminalEvent();

    Reply<List<Mock>> reply = observer.values().get(0);
    assertThat(reply.getSource(), is(Source.PERSISTENCE));
    assertThat(reply.isEncrypted(), is(true));
  }

  @Test
  public void _02_If_Class_Has_Been_Annotated_With_EncryptedKey_Then_Only_Encrypt_When_Provider_Has_Been_Annotated_With_Encrypt() {
    TestObserver<Reply<List<Mock>>> observer =
        providersRxCache.getMocksNotEncryptedWithDetailResponse(ProviderHelper.<List<Mock>>withoutLoader())
            .test();
    observer.awaitTerminalEvent();

    Reply<List<Mock>> reply = observer.values().get(0);
    assertThat(reply.getSource(), is(Source.PERSISTENCE));
    assertThat(reply.isEncrypted(), is(false));

    observer =
        providersRxCache.getMocksEncryptedWithDetailResponse(ProviderHelper.<List<Mock>>withoutLoader()).test();
    observer.awaitTerminalEvent();

    reply = observer.values().get(0);
    assertThat(reply.getSource(), is(Source.PERSISTENCE));
    assertThat(reply.isEncrypted(), is(true));
  }

  private Observable<List<Mock>> createObservableMocks(int size) {
    long currentTime = System.currentTimeMillis();

    List<Mock> mocks = new ArrayList(size);
    for (int i = 0; i < size; i++) {
      mocks.add(new Mock("mock" + currentTime));
    }

    return Observable.just(mocks);
  }

  @EncryptKey("myStrongKey-1234")
  public interface ProvidersRxCache {

    @Encrypt Observable<Reply<List<Mock>>> getMocksEncryptedWithDetailResponse(
        Observable<List<Mock>> mocks);

    Observable<Reply<List<Mock>>> getMocksNotEncryptedWithDetailResponse(
        Observable<List<Mock>> mocks);
  }
}
