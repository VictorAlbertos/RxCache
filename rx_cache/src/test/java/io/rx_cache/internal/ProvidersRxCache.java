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

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.rx_cache.DynamicKey;
import io.rx_cache.EvictDynamicKeyGroup;
import io.rx_cache.EvictProvider;
import io.rx_cache.EvictDynamicKey;
import io.rx_cache.LifeCache;
import io.rx_cache.Reply;
import rx.Observable;

/**
 * Provided to test as an integration test the library RxCache
 */
interface ProvidersRxCache {
    Observable<List<Mock>> getMocks(Observable<List<Mock>> mocks);

    Observable<Reply<List<Mock>>> getMocksWithDetailResponse(Observable<List<Mock>> mocks);

    @LifeCache(duration = 1, timeUnit = TimeUnit.SECONDS)
    Observable<Reply<List<Mock>>> getMocksResponseOneSecond(Observable<List<Mock>> mocks);

    @LifeCache(duration = 1, timeUnit = TimeUnit.MINUTES)
    Observable<List<Mock>> getMocksLifeTimeMinutes(Observable<List<Mock>> mocks);

    @LifeCache(duration = 1, timeUnit = TimeUnit.SECONDS)
    Observable<List<Mock>> getMocksLifeTimeSeconds(Observable<List<Mock>> mocks);

    @LifeCache(duration = 65000, timeUnit = TimeUnit.MILLISECONDS)
    Observable<List<Mock>> getMocksLifeTimeMillis(Observable<List<Mock>> mocks);

    Observable<List<Mock>> getMocksPaginate(Observable<List<Mock>> mocks, DynamicKey page);

    Observable<List<Mock>> getMocksPaginateEvictProvider(Observable<List<Mock>> mocks, DynamicKey page, EvictProvider evictProvider);

    Reply<List<Mock>> getMocksBadReturnType(Observable<List<Mock>> mocks);

    Observable<Reply<List<Mock>>> getMocksEvictProvider(Observable<List<Mock>> mocks, EvictProvider evictProvider);

    Observable<Reply<List<Mock>>> getMocksDynamicKeyEvictPage(Observable<List<Mock>> mocks, DynamicKey page, EvictDynamicKey evictPage);

    Observable<Mock> getLoggedMock(Observable<Mock> mock, EvictProvider evictProvider);



    Observable<Mock> getMockWithoutLoaderObservable();

    int getMockWithoutReturnObservable();

    Observable<Mock> getMockMultipleObservables(Observable<Mock> mock, Observable<Mock> mock2);

    Observable<Mock> getMockMultipleEvicts(Observable<Mock> mock, EvictProvider evictProvider, EvictProvider evictProvider2);

    Observable<Mock> getMockMultipleDynamicKeys(Observable<Mock> mock, DynamicKey dynamicKey, DynamicKey dynamicKey2);

    Observable<Mock> getMockEvictDynamicKeyWithoutProvidingDynamicKey(Observable<Mock> mock, EvictDynamicKey evictDynamicKey);

    Observable<Mock> getMockEvictDynamicKeyGroupWithoutProvidingDynamicKeyGroup(Observable<Mock> mock, EvictDynamicKeyGroup evictDynamicKeyGroup);
}

