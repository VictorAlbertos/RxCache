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
import io.rx_cache2.Encrypt;
import io.rx_cache2.EncryptKey;
import io.rx_cache2.LifeCache;
import io.rx_cache2.internal.common.BaseTestEvictingTask;
import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by victor on 03/03/16.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProvidersRxCacheEvictExpiredRecordsTest extends BaseTestEvictingTask {
  @ClassRule public static TemporaryFolder temporaryFolder = new TemporaryFolder();
  private ProvidersRxCache providersRxCache;

  @Before public void setUp() {
    providersRxCache = new RxCache.Builder()
        .persistence(temporaryFolder.getRoot(), Jolyglot$.newInstance())
        .using(ProvidersRxCache.class);
  }

  @Test public void _1_Populate_Disk_With_Expired_Records() {
    assertEquals(0, getSizeMB(temporaryFolder.getRoot()));

    for (int i = 0; i < 50; i++) {
      waitTime(50);
      TestObserver<List<io.rx_cache2.internal.Mock>> observer = new TestObserver<>();
      String key = System.currentTimeMillis() + i + "";
      providersRxCache.getEphemeralMocksPaginate(createObservableMocks(), new DynamicKey(key))
          .subscribe(observer);
      observer.awaitTerminalEvent();
    }

    assertNotEquals(0, getSizeMB(temporaryFolder.getRoot()));
  }

  @Test public void _2_Perform_Evicting_Task_And_Check_Results() {
    waitTime(1000);
    assertEquals(0, temporaryFolder.getRoot().listFiles().length);
    assertEquals(0, getSizeMB(temporaryFolder.getRoot()));
  }

  @Test public void _3_Populate_Disk_With_No_Expired_Records() {
    deleteAllFiles();
    assertEquals(0, getSizeMB(temporaryFolder.getRoot()));

    for (int i = 0; i < 50; i++) {
      waitTime(50);
      TestObserver<List<io.rx_cache2.internal.Mock>> subscriber = new TestObserver<>();
      String key = System.currentTimeMillis() + i + "";
      providersRxCache.getMocksPaginate(createObservableMocks(), new DynamicKey(key))
          .subscribe(subscriber);
      subscriber.awaitTerminalEvent();
    }

    assertNotEquals(0, getSizeMB(temporaryFolder.getRoot()));
  }

  @Test public void _4_Perform_Evicting_Task_And_Check_Results() {
    waitTime(1000);
    assertNotEquals(0, getSizeMB(temporaryFolder.getRoot()));
  }

  @Test public void _5_Populate_Disk_With_Expired_Encrypted_Records() {
    deleteAllFiles();
    assertEquals(0, temporaryFolder.getRoot().listFiles().length);

    for (int i = 0; i < 50; i++) {
      waitTime(50);
      TestObserver<List<io.rx_cache2.internal.Mock>> observer = new TestObserver<>();
      String key = System.currentTimeMillis() + i + "";
      providersRxCache.getEphemeralEncryptedMocksPaginate(createObservableMocks(),
          new DynamicKey(key)).subscribe(observer);
      observer.awaitTerminalEvent();
    }

    assertNotEquals(0, getSizeMB(temporaryFolder.getRoot()));
  }

  @Test public void _6_Perform_Evicting_Task_And_Check_Results() {
    waitTime(1000);
    assertEquals(0, temporaryFolder.getRoot().listFiles().length);
    assertEquals(0, getSizeMB(temporaryFolder.getRoot()));
  }

  private void deleteAllFiles() {
    File[] files = temporaryFolder.getRoot().listFiles();

    for (File file : files) {
      file.delete();
      waitTime(100);
    }
  }

  @EncryptKey("myStrongKey-1234")
  private interface ProvidersRxCache {
    Observable<List<io.rx_cache2.internal.Mock>> getMocksPaginate(Observable<List<io.rx_cache2.internal.Mock>> mocks, DynamicKey page);

    @LifeCache(duration = 1, timeUnit = TimeUnit.MILLISECONDS)
    Observable<List<io.rx_cache2.internal.Mock>> getEphemeralMocksPaginate(Observable<List<io.rx_cache2.internal.Mock>> mocks, DynamicKey page);

    @Encrypt
    @LifeCache(duration = 1, timeUnit = TimeUnit.MILLISECONDS)
    Observable<List<io.rx_cache2.internal.Mock>> getEphemeralEncryptedMocksPaginate(Observable<List<io.rx_cache2.internal.Mock>> mocks,
        DynamicKey page);
  }
}
