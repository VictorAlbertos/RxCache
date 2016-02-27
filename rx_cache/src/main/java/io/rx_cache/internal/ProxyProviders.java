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


import com.google.common.annotations.VisibleForTesting;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.inject.Inject;

import io.rx_cache.EvictDynamicKey;
import io.rx_cache.EvictDynamicKeyGroup;
import io.rx_cache.Record;
import io.rx_cache.Reply;
import io.rx_cache.Source;
import io.rx_cache.internal.cache.TwoLayersCache;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

final class ProxyProviders implements InvocationHandler {
    private final ProxyTranslator proxyTranslator;
    private final TwoLayersCache twoLayersCache;
    private final Boolean useExpiredDataIfLoaderNotAvailable;

    @Inject public ProxyProviders(ProxyTranslator proxyTranslator, TwoLayersCache twoLayersCache, Boolean useExpiredDataIfLoaderNotAvailable) {
        this.proxyTranslator = proxyTranslator;
        this.twoLayersCache = twoLayersCache;
        this.useExpiredDataIfLoaderNotAvailable = useExpiredDataIfLoaderNotAvailable;
    }

    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final ProxyTranslator.ConfigProvider configProvider = proxyTranslator.processMethod(method, args);
        return getMethodImplementation(configProvider);
    }

    @VisibleForTesting Observable<Object> getMethodImplementation(final ProxyTranslator.ConfigProvider configProvider) {
        return Observable.defer(new Func0<Observable<Object>>() {
            @Override
            public Observable<Object> call() {
                return getData(configProvider);
            }
        });
    }

    private Observable<Object> getData(final ProxyTranslator.ConfigProvider configProvider) {
        return Observable.just(twoLayersCache.retrieve(configProvider.getProviderKey(), configProvider.getDynamicKey(), configProvider.getDynamicKeyGroup(), useExpiredDataIfLoaderNotAvailable, configProvider.getLifeTimeMillis()))
                .map(new Func1<Record, Observable<Reply>>() {
                    @Override public Observable<Reply> call(final Record record) {
                        if (record != null && !configProvider.evictProvider().evict())
                            return Observable.just(new Reply(record.getData(), record.getSource()));

                        return getDataFromLoader(configProvider, record);
                    }
                }).flatMap(new Func1<Observable<Reply>, Observable<Object>>() {
                    @Override
                    public Observable<Object> call(Observable<Reply> responseObservable) {
                        return responseObservable.map(new Func1<Reply, Object>() {
                            @Override
                            public Object call(Reply reply) {
                                return getReturnType(configProvider, reply);
                            }
                        });
                    }
                });
    }

    private Observable<Reply> getDataFromLoader(final ProxyTranslator.ConfigProvider configProvider, final Record record) {
        return configProvider.getLoaderObservable().map(new Func1() {
            @Override
            public Reply call(Object data) {
                if (data == null && useExpiredDataIfLoaderNotAvailable && record != null) {
                    return new Reply(record.getData(), record.getSource());
                }

                clearKeyIfNeeded(configProvider);

                if (data == null)
                    throw new RuntimeException(Locale.NOT_DATA_RETURN_WHEN_CALLING_OBSERVABLE_LOADER + " " + configProvider.getProviderKey());

                twoLayersCache.save(configProvider.getProviderKey(), configProvider.getDynamicKey(), configProvider.getDynamicKeyGroup(), data);
                return new Reply(data, Source.CLOUD);
            }
        }).onErrorReturn(new Func1() {
            @Override public Object call(Object o) {
                clearKeyIfNeeded(configProvider);

                if (useExpiredDataIfLoaderNotAvailable && record != null) {
                    return new Reply(record.getData(), record.getSource());
                }

                throw new RuntimeException(Locale.NOT_DATA_RETURN_WHEN_CALLING_OBSERVABLE_LOADER + " " + configProvider.getProviderKey());
            }
        });
    }

    private void clearKeyIfNeeded(ProxyTranslator.ConfigProvider configProvider) {
        if (configProvider.evictProvider() instanceof EvictDynamicKeyGroup) {
            EvictDynamicKeyGroup evictDynamicKeyGroup = (EvictDynamicKeyGroup) configProvider.evictProvider();
            if (evictDynamicKeyGroup.evict())
                twoLayersCache.evictDynamicKeyGroup(configProvider.getProviderKey(), configProvider.getDynamicKey().toString(), configProvider.getDynamicKeyGroup().toString());
        } else if (configProvider.evictProvider() instanceof EvictDynamicKey) {
            EvictDynamicKey evictDynamicKey = (EvictDynamicKey) configProvider.evictProvider();
            if (evictDynamicKey.evict())
                twoLayersCache.evictDynamicKey(configProvider.getProviderKey(), configProvider.getDynamicKey().toString());
        } else if (configProvider.evictProvider().evict()) {
            twoLayersCache.evictProviderKey(configProvider.getProviderKey());
        }
    }

    private Object getReturnType(ProxyTranslator.ConfigProvider configProvider, Reply reply) {
        if (configProvider.requiredDetailedResponse()) {
            return reply;
        } else {
            return reply.getData();
        }
    }
}