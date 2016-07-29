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

package io.rx_cache.samples;

import io.rx_cache.Actionable;
import io.rx_cache.DynamicKey;
import io.rx_cache.DynamicKeyGroup;
import io.rx_cache.EvictDynamicKey;
import io.rx_cache.EvictDynamicKeyGroup;
import io.rx_cache.EvictProvider;
import io.rx_cache.Mock;
import java.util.List;
import rx.Observable;

public interface RxProviders {
    @Actionable
    Observable<List<Mock>> getMocksEvictProvider(Observable<List<Mock>> oMocks,
                                                 EvictProvider evictProvider);

    @Actionable
    Observable<List<Mock>> getMocksEvictDynamicKey(Observable<List<Mock>> oMocks,
                                                   DynamicKey dynamicKey,
                                                   EvictDynamicKey evictDynamicKey);

    @Actionable
    Observable<List<Mock>> getMocksEvictDynamicKeyGroup(Observable<List<Mock>> oMocks,
                                                        DynamicKeyGroup dynamicKeyGroup,
                                                        EvictDynamicKeyGroup evictDynamicKeyGroup);
}
