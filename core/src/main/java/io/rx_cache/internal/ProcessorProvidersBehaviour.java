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

import io.rx_cache.ConfigProvider;
import io.rx_cache.EvictDynamicKey;
import io.rx_cache.EvictDynamicKeyGroup;
import io.rx_cache.Reply;
import io.rx_cache.RxCacheException;
import io.rx_cache.Source;
import io.rx_cache.internal.cache.EvictExpiredRecordsPersistence;
import io.rx_cache.internal.cache.GetDeepCopy;
import io.rx_cache.internal.cache.TwoLayersCache;
import io.rx_cache.internal.migration.DoMigrations;
import javax.inject.Inject;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public final class ProcessorProvidersBehaviour implements ProcessorProviders {
  private final TwoLayersCache twoLayersCache;
  private final Boolean useExpiredDataIfLoaderNotAvailable;
  private final GetDeepCopy getDeepCopy;
  private final Observable<Void> oProcesses;
  private volatile Boolean hasProcessesEnded;

  @Inject public ProcessorProvidersBehaviour(TwoLayersCache twoLayersCache,
      Boolean useExpiredDataIfLoaderNotAvailable,
      EvictExpiredRecordsPersistence evictExpiredRecordsPersistence,
      GetDeepCopy getDeepCopy, DoMigrations doMigrations) {
    this.hasProcessesEnded = false;
    this.twoLayersCache = twoLayersCache;
    this.useExpiredDataIfLoaderNotAvailable = useExpiredDataIfLoaderNotAvailable;
    this.getDeepCopy = getDeepCopy;
    this.oProcesses = startProcesses(doMigrations, evictExpiredRecordsPersistence);
  }

  private Observable<Void> startProcesses(DoMigrations doMigrations,
      final EvictExpiredRecordsPersistence evictExpiredRecordsPersistence) {
    Observable<Void> oProcesses =
        doMigrations.react().flatMap(new Func1<Void, Observable<? extends Void>>() {
          @Override public Observable<? extends Void> call(Void nothing) {
            return evictExpiredRecordsPersistence.startEvictingExpiredRecords();
          }
        }).subscribeOn((Schedulers.io())).observeOn(Schedulers.io()).share();

    oProcesses.subscribe(new Action1<Void>() {
      @Override public void call(Void nothing) {
        hasProcessesEnded = true;
      }
    });

    return oProcesses;
  }

  @Override
  public <T> Observable<T> process(final ConfigProvider configProvider) {
    return (Observable<T>) Observable.defer(new Func0<Observable<Object>>() {
      @Override public Observable<Object> call() {
        if (hasProcessesEnded) {
          return getData(configProvider);
        }

        return oProcesses.flatMap(new Func1<Void, Observable<?>>() {
          @Override public Observable<?> call(Void aVoid) {
            return getData(configProvider);
          }
        });
      }
    });
  }

  //VisibleForTesting
  <T> Observable<T> getData(final ConfigProvider configProvider) {
    return (Observable<T>) Observable.just(
        twoLayersCache.retrieve(configProvider.getProviderKey(), configProvider.getDynamicKey(),
            configProvider.getDynamicKeyGroup(), useExpiredDataIfLoaderNotAvailable,
            configProvider.getLifeTimeMillis(), configProvider.isEncrypted()))
        .map(new Func1<Record, Observable<Reply>>() {
          @Override public Observable<Reply> call(final Record record) {
            if (record != null && !configProvider.evictProvider().evict()) {
              return Observable.just(
                  new Reply(record.getData(), record.getSource(), configProvider.isEncrypted()));
            }

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

  private Observable<Reply> getDataFromLoader(final ConfigProvider configProvider,
      final Record record) {
    return configProvider.getLoaderObservable().map(new Func1() {
      @Override public Reply call(Object data) {
        boolean useExpiredData = configProvider.useExpiredDataIfNotLoaderAvailable() != null ?
            configProvider.useExpiredDataIfNotLoaderAvailable()
            : useExpiredDataIfLoaderNotAvailable;

        if (data == null && useExpiredData && record != null) {
          return new Reply(record.getData(), record.getSource(), configProvider.isEncrypted());
        }

        clearKeyIfNeeded(configProvider);

        if (data == null) {
          throw new RxCacheException(Locale.NOT_DATA_RETURN_WHEN_CALLING_OBSERVABLE_LOADER
              + " "
              + configProvider.getProviderKey());
        }

        twoLayersCache.save(configProvider.getProviderKey(), configProvider.getDynamicKey(),
            configProvider.getDynamicKeyGroup(), data, configProvider.getLifeTimeMillis(),
            configProvider.isExpirable(), configProvider.isEncrypted());
        return new Reply(data, Source.CLOUD, configProvider.isEncrypted());
      }
    }).onErrorReturn(new Func1() {
      @Override public Object call(Object o) {
        clearKeyIfNeeded(configProvider);

        boolean useExpiredData = configProvider.useExpiredDataIfNotLoaderAvailable() != null ?
            configProvider.useExpiredDataIfNotLoaderAvailable()
            : useExpiredDataIfLoaderNotAvailable;

        if (useExpiredData && record != null) {
          return new Reply(record.getData(), record.getSource(), configProvider.isEncrypted());
        }

        throw new RxCacheException(Locale.NOT_DATA_RETURN_WHEN_CALLING_OBSERVABLE_LOADER
            + " "
            + configProvider.getProviderKey(), (Throwable) o);
      }
    });
  }

  private void clearKeyIfNeeded(ConfigProvider configProvider) {
    if (!configProvider.evictProvider().evict()) return;

    if (configProvider.evictProvider() instanceof EvictDynamicKeyGroup) {
      twoLayersCache.evictDynamicKeyGroup(configProvider.getProviderKey(),
          configProvider.getDynamicKey().toString(),
          configProvider.getDynamicKeyGroup().toString());
    } else if (configProvider.evictProvider() instanceof EvictDynamicKey) {
      twoLayersCache.evictDynamicKey(configProvider.getProviderKey(),
          configProvider.getDynamicKey().toString());
    } else {
      twoLayersCache.evictProviderKey(configProvider.getProviderKey());
    }
  }

  private Object getReturnType(ConfigProvider configProvider, Reply reply) {
    Object data = getDeepCopy.deepCopy(reply.getData());

    if (configProvider.requiredDetailedResponse()) {
      return new Reply<>(data, reply.getSource(), configProvider.isEncrypted());
    } else {
      return data;
    }
  }

  @Override public Observable<Void> evictAll() {
    return Observable.defer(new Func0<Observable<Void>>() {
      @Override public Observable<Void> call() {
        twoLayersCache.evictAll();
        return Observable.just(null);
      }
    });
  }
}
