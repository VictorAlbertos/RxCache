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

import io.rx_cache.Invalidator;
import io.rx_cache.InvalidatorDynamicKey;
import io.rx_cache.DynamicKey;
import io.rx_cache.InvalidateCache;
import io.rx_cache.LifeCache;
import io.rx_cache.Loader;
import io.rx_cache.Reply;
import rx.Observable;

/**
 * Provided to test as an integration test the library RxCache
 */
interface ProvidersRxCache {
    Observable<List<Mock>> getMocks(@Loader Observable<List<Mock>> mocks);

    Observable<Reply<List<Mock>>> getMocksWithDetailResponse(@Loader Observable<List<Mock>> mocks);

    @LifeCache(duration = 1, timeUnit = TimeUnit.SECONDS)
    Observable<Reply<List<Mock>>> getMocksResponseOneSecond(@Loader Observable<List<Mock>> mocks);

    @LifeCache(duration = 1, timeUnit = TimeUnit.MINUTES)
    Observable<List<Mock>> getMocksLifeTimeMinutes(@Loader Observable<List<Mock>> mocks);

    @LifeCache(duration = 1, timeUnit = TimeUnit.SECONDS)
    Observable<List<Mock>> getMocksLifeTimeSeconds(@Loader Observable<List<Mock>> mocks);

    @LifeCache(duration = 65000, timeUnit = TimeUnit.MILLISECONDS)
    Observable<List<Mock>> getMocksLifeTimeMillis(@Loader Observable<List<Mock>> mocks);

    Observable<List<Mock>> getMocksWithoutLoaderAnnotation(Observable<List<Mock>> mocks);

    Observable<List<Mock>> getMocksPaginate(@Loader Observable<List<Mock>> mocks, @DynamicKey int page);

    Observable<List<Mock>> getMocksPaginateInvalidateAll(@Loader Observable<List<Mock>> mocks, @DynamicKey int page,
                                                         @InvalidateCache Invalidator invalidator);

    Reply<List<Mock>> getMocksBadReturnType(@Loader Observable<List<Mock>> mocks);

    Observable<Reply<List<Mock>>> getMocksInvalidateCache(@Loader Observable<List<Mock>> mocks, @InvalidateCache Invalidator invalidator);

    Observable<Reply<List<Mock>>> getMocksDynamicKeyInvalidateCache(@Loader Observable<List<Mock>> mocks, @DynamicKey int page,
                                                                    @InvalidateCache InvalidatorDynamicKey invalidatorDynamicKey);

    Observable<Mock> getLoggedMock(@Loader Observable<Mock> mock, @InvalidateCache Invalidator invalidator);
}

