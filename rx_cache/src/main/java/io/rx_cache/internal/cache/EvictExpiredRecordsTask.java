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

package io.rx_cache.internal.cache;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.rx_cache.Persistence;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

@Singleton public final class EvictExpiredRecordsTask {
    private final Persistence persistence;
    private boolean finished;
    private List<String> allKeys;
    private int currentIndex;
    private final int WINDOW_TO_KEEP_FILE_SYSTEM_NOT_LOCK = 100;

    @Inject public EvictExpiredRecordsTask(Persistence persistence) {
        this.persistence = persistence;
    }

    public void startEvictingExpiredRecords(final Subscriber subscriber) {
        Observable.interval(WINDOW_TO_KEEP_FILE_SYSTEM_NOT_LOCK, TimeUnit.MILLISECONDS).observeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    @Override public void call() {
                        allKeys = persistence.allKeys();
                        currentIndex = -1;
                    }
                })
                .takeWhile(new Func1<Long, Boolean>() {
                    @Override public Boolean call(Long interval) {
                        return !finished;
                    }
                })
                .map(new Func1<Long, Integer>() {
                    @Override public Integer call(Long interval) {
                        return currentIndex=currentIndex+1;
                    }
                })
                .subscribe(new Action1<Integer>() {
                    @Override public void call(Integer index) {
                        try {
                            persistence.evict(allKeys.get(index));
                        } catch (IndexOutOfBoundsException exception) {
                            finished = true;
                            subscriber.onCompleted();
                        }
                    }
                });
    }
}
