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

import io.rx_cache.InvalidatorDynamicKey;
import io.rx_cache.Record;
import io.rx_cache.Reply;
import io.rx_cache.Source;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

final class ProxyProviders implements InvocationHandler {
    private final ProxyTranslator proxyTranslator;
    private final Cache cache;
    private final Boolean useExpiredDataIfLoaderNotAvailable;

    @Inject public ProxyProviders(ProxyTranslator proxyTranslator, Cache cache, Boolean useExpiredDataIfLoaderNotAvailable) {
        this.proxyTranslator = proxyTranslator;
        this.cache = cache;
        this.useExpiredDataIfLoaderNotAvailable = useExpiredDataIfLoaderNotAvailable;
    }

    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final ProxyTranslator.Translation translation = proxyTranslator.processMethod(method, args);
        return getMethodImplementation(translation);
    }

    @VisibleForTesting Observable<Object> getMethodImplementation(final ProxyTranslator.Translation translation) {
        return Observable.defer(new Func0<Observable<Object>>() {
            @Override public Observable<Object> call() {
                return getData(translation);
            }
        });
    }

    private Observable<Object> getData(final ProxyTranslator.Translation translation) {
        return Observable.just(cache.retrieve(translation.getKey(), translation.getDynamicKey(), useExpiredDataIfLoaderNotAvailable))
                .map(new Func1<Record, Observable<Reply>>() {
                    @Override public Observable<Reply> call(final Record record) {
                        if (record != null && !translation.invalidator().invalidate())
                            return Observable.just(new Reply(record.getData(), record.getSource()));

                        return getDataFromLoader(translation, record);
                    }
                }).flatMap(new Func1<Observable<Reply>, Observable<Object>>() {
                    @Override public Observable<Object> call(Observable<Reply> responseObservable) {
                        return responseObservable.map(new Func1<Reply, Object>() {
                            @Override public Object call(Reply reply) {
                                return getReturnType(translation, reply);
                            }
                        });
                    }
                });
    }

    private Observable<Reply> getDataFromLoader(final ProxyTranslator.Translation translation, final Record record) {
        return translation.getLoaderObservable().map(new Func1() {
            @Override public Reply call(Object data) {
                if (data == null && useExpiredDataIfLoaderNotAvailable && record != null) {
                    return new Reply(record.getData(), record.getSource());
                }

                if (translation.invalidator() instanceof InvalidatorDynamicKey) {
                    InvalidatorDynamicKey invalidatorDynamicKey = (InvalidatorDynamicKey) translation.invalidator();
                    if (invalidatorDynamicKey.invalidate())
                        cache.clearDynamicKey(translation.getKey(), invalidatorDynamicKey.dynamicKey().toString());
                } else if (translation.invalidator().invalidate()) {
                    cache.clear(translation.getKey());
                }

                if (data == null)
                    throw new RuntimeException(Locale.NOT_DATA_RETURN_WHEN_CALLING_OBSERVABLE_LOADER + " " + translation.getKey());

                cache.save(translation.getKey(), translation.getDynamicKey(), data, translation.getLifeTimeMillis());
                return new Reply(data, Source.CLOUD);
            }
        });
    }

    private Object getReturnType(ProxyTranslator.Translation translation, Reply reply) {
        if (translation.requiredDetailedResponse()) {
            return reply;
        } else {
            return reply.getData();
        }
    }
}