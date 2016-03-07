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

package io.rx_cache.internal;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.MethodSorters;

import java.util.List;

import io.rx_cache.DynamicKey;
import io.rx_cache.PolicyHeapCache;
import io.rx_cache.internal.common.BaseTestEvictingTask;
import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by victor on 03/03/16.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProvidersRxCacheEvictExpiredRecordsTest extends BaseTestEvictingTask {
    @ClassRule public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    private ProvidersRxCache providersRxCache;

    @Before public void setUp() {
        providersRxCache = new RxCache.Builder()
                .withPolicyCache(PolicyHeapCache.MODERATE)
                .persistence(temporaryFolder.getRoot())
                .using(ProvidersRxCache.class);
    }

    @Test public void _1_Populate_Disk_With_Expired_Records_And_No_Retrievable_Keys() {
        assert getSizeMB(temporaryFolder.getRoot()) == 0;

        for (int i = 0; i < 50; i++) {
            waitTime(50);
            TestSubscriber<List<Mock>> subscriber = new TestSubscriber<>();
            String key = System.currentTimeMillis() + i + "";
            providersRxCache.getEphemeralMocksPaginate(createObservableMocks(), new DynamicKey(key)).subscribe(subscriber);
            subscriber.awaitTerminalEvent();
        }

        assert getSizeMB(temporaryFolder.getRoot()) > 0;
    }

    @Test public void _2_Perform_Evicting_Task_And_Check_Results() {
        waitTime(1000);
        assertThat(getSizeMB(temporaryFolder.getRoot()), is(0));
    }

    @Test public void _3_Populate_Disk_With_No_Expired_Records_And_No_Retrievable_Keys() {
        assertThat(getSizeMB(temporaryFolder.getRoot()), is(0));

        for (int i = 0; i < 50; i++) {
            waitTime(50);
            TestSubscriber<List<Mock>> subscriber = new TestSubscriber<>();
            String key = System.currentTimeMillis() + i + "";
            providersRxCache.getMocksPaginate(createObservableMocks(), new DynamicKey(key)).subscribe(subscriber);
            subscriber.awaitTerminalEvent();
        }

        assert getSizeMB(temporaryFolder.getRoot()) > 0;
    }

    @Test public void _4_Perform_Evicting_Task_And_Check_Results() {
        waitTime(1000);
        assert getSizeMB(temporaryFolder.getRoot()) > 0;
    }
}
