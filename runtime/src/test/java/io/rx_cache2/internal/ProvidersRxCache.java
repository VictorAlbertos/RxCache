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
import io.rx_cache2.DynamicKey;
import io.rx_cache2.DynamicKeyGroup;
import io.rx_cache2.EvictDynamicKey;
import io.rx_cache2.EvictDynamicKeyGroup;
import io.rx_cache2.EvictProvider;
import io.rx_cache2.Expirable;
import io.rx_cache2.LifeCache;
import io.rx_cache2.Reply;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Provided to test as an integration test the library RxCache
 */
public interface ProvidersRxCache {
  Single<List<Mock>> getMocksSingle(Single<List<io.rx_cache2.internal.Mock>> mocks);

  Maybe<List<Mock>> getMocksMaybe(Maybe<List<io.rx_cache2.internal.Mock>> mocks);

  Flowable<List<Mock>> getMocksFlowable(Flowable<List<io.rx_cache2.internal.Mock>> mocks);

  Observable<List<io.rx_cache2.internal.Mock>> getMocks(Observable<List<io.rx_cache2.internal.Mock>> mocks);

  Observable<Reply<List<io.rx_cache2.internal.Mock>>> getMocksWithDetailResponse(Observable<List<io.rx_cache2.internal.Mock>> mocks);

  @LifeCache(duration = 1, timeUnit = TimeUnit.SECONDS)
  Observable<Reply<List<io.rx_cache2.internal.Mock>>> getMocksListResponseOneSecond(Observable<List<io.rx_cache2.internal.Mock>> mocks);

  Observable<Reply<Map<Integer, io.rx_cache2.internal.Mock>>> getMocksMapResponse(Observable<Map<Integer, io.rx_cache2.internal.Mock>> mocks);

  Observable<Reply<io.rx_cache2.internal.Mock[]>> getMocksArrayResponse(Observable<io.rx_cache2.internal.Mock[]> mocks);

  @LifeCache(duration = 0, timeUnit = TimeUnit.MINUTES)
  Observable<Reply<List<io.rx_cache2.internal.Mock>>> getMocksLife0Minutes(Observable<List<io.rx_cache2.internal.Mock>> mocks);

  @LifeCache(duration = 1, timeUnit = TimeUnit.MINUTES)
  Observable<List<io.rx_cache2.internal.Mock>> getMocksLifeTimeMinutes(Observable<List<io.rx_cache2.internal.Mock>> mocks);

  @LifeCache(duration = 1, timeUnit = TimeUnit.SECONDS)
  Observable<List<io.rx_cache2.internal.Mock>> getMocksLifeTimeSeconds(Observable<List<io.rx_cache2.internal.Mock>> mocks);

  @LifeCache(duration = 65000, timeUnit = TimeUnit.MILLISECONDS)
  Observable<List<io.rx_cache2.internal.Mock>> getMocksLifeTimeMillis(Observable<List<io.rx_cache2.internal.Mock>> mocks);

  Observable<List<io.rx_cache2.internal.Mock>> getMocksPaginate(Observable<List<io.rx_cache2.internal.Mock>> mocks, DynamicKey page);

  @Expirable(false)
  @LifeCache(duration = 1, timeUnit = TimeUnit.DAYS)
  Observable<List<io.rx_cache2.internal.Mock>> getMocksPaginateNotExpirable(Observable<List<io.rx_cache2.internal.Mock>> mocks,
      DynamicKey page);

  Observable<List<io.rx_cache2.internal.Mock>> getMocksPaginateEvictProvider(Observable<List<io.rx_cache2.internal.Mock>> mocks,
      DynamicKey page, EvictProvider evictProvider);

  Reply<List<io.rx_cache2.internal.Mock>> getMocksBadReturnType(Observable<List<io.rx_cache2.internal.Mock>> mocks);

  Observable<Reply<List<io.rx_cache2.internal.Mock>>> getMocksEvictProvider(Observable<List<io.rx_cache2.internal.Mock>> mocks,
      EvictProvider evictProvider);

  Observable<Reply<List<io.rx_cache2.internal.Mock>>> getMocksDynamicKeyEvictPage(Observable<List<io.rx_cache2.internal.Mock>> mocks,
      DynamicKey page, EvictDynamicKey evictPage);

  Observable<io.rx_cache2.internal.Mock> getLoggedMock(Observable<io.rx_cache2.internal.Mock> mock, EvictProvider evictProvider);

  Observable<List<io.rx_cache2.internal.Mock>> getMocksFilteredPaginateEvict(Observable<List<io.rx_cache2.internal.Mock>> oMocks,
      DynamicKeyGroup dynamicKeyGroup, EvictProvider evictDynamicKey);

  @LifeCache(duration = 1, timeUnit = TimeUnit.MILLISECONDS)
  Observable<List<io.rx_cache2.internal.Mock>> getEphemeralMocksPaginate(Observable<List<io.rx_cache2.internal.Mock>> mocks, DynamicKey page);

  Observable<io.rx_cache2.internal.Mock> getMockWithoutLoaderObservable();

  int getMockWithoutReturnObservable();

  Observable<io.rx_cache2.internal.Mock> getMockMultipleObservables(Observable<io.rx_cache2.internal.Mock> mock, Observable<io.rx_cache2.internal.Mock> mock2);

  Observable<io.rx_cache2.internal.Mock> getMockMultipleEvicts(Observable<io.rx_cache2.internal.Mock> mock, EvictProvider evictProvider,
      EvictProvider evictProvider2);

  Observable<io.rx_cache2.internal.Mock> getMockMultipleDynamicKeys(Observable<io.rx_cache2.internal.Mock> mock, DynamicKey dynamicKey,
      DynamicKey dynamicKey2);

  Observable<io.rx_cache2.internal.Mock> getMockEvictDynamicKeyProvidingDynamicKey(Observable<io.rx_cache2.internal.Mock> mock,
      DynamicKey dynamicKey, EvictDynamicKey evictDynamicKey);

  Observable<io.rx_cache2.internal.Mock> getMockEvictDynamicKeyWithoutProvidingDynamicKey(Observable<io.rx_cache2.internal.Mock> mock,
      EvictDynamicKey evictDynamicKey);

  Observable<io.rx_cache2.internal.Mock> getMockEvictDynamicKeyGroupProvidingDynamicKeyGroup(Observable<io.rx_cache2.internal.Mock> mock,
      DynamicKeyGroup dynamicKeyGroup, EvictDynamicKeyGroup evictDynamicKeyGroup);

  Observable<io.rx_cache2.internal.Mock> getMockEvictDynamicKeyGroupWithoutProvidingDynamicKeyGroup(Observable<io.rx_cache2.internal.Mock> mock,
      EvictDynamicKeyGroup evictDynamicKeyGroup);
}

