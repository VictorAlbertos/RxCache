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

import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import io.rx_cache.Record;
import io.rx_cache.internal.Locale;
import io.rx_cache.internal.Memory;
import io.rx_cache.internal.Mock;
import io.rx_cache.internal.SimpleMemory;
import io.rx_cache.internal.common.BaseTest;
import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Theories.class)
public class EvictExpirableRecordsPersistenceTest extends BaseTest {
    private EvictExpirableRecordsPersistence evictExpirableRecordsPersistenceUT;
    private final Memory memory = new SimpleMemory();
    private static final long ONE_MILLI_LIFE = 1, NOT_EXPIRABLE_LIFE = 0;

    @Override public void setUp() {
        super.setUp();
    }

    @Test public void When_Task_Is_Running_Do_Not_Start_Again() {
        evictExpirableRecordsPersistenceUT = new EvictExpirableRecordsPersistence(memory, disk, 10);

        for (int i = 0; i < 10; i++) {
            evictExpirableRecordsPersistenceUT.startTaskIfNeeded();
        }

        assert(evictExpirableRecordsPersistenceUT.runningTasks() < 5);
    }

    @Test public void When_Task_Is_Not_Running_Start_Again() {
        evictExpirableRecordsPersistenceUT = new EvictExpirableRecordsPersistence(memory, disk, 10);

        for (int i = 0; i < 10; i++) {
            waitTime(100);
            evictExpirableRecordsPersistenceUT.startTaskIfNeeded();
        }

        assertThat(evictExpirableRecordsPersistenceUT.runningTasks(), is(10));
    }

    @Test public void When_Not_Reached_Memory_Threshold_Not_Emit() {
        evictExpirableRecordsPersistenceUT = new EvictExpirableRecordsPersistence(memory, disk, 10);

        populate(true);
        assertThat(disk.allKeys().size(), is(100));

        TestSubscriber testSubscriber = new TestSubscriber();
        evictExpirableRecordsPersistenceUT.startTaskIfNeeded().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();
        testSubscriber.assertNoValues();
    }

    @DataPoint public static Integer _3_MB = 3;
    @DataPoint public static Integer _5_MB = 5;
    @DataPoint public static Integer _7_MB = 7;
    @Theory @Test public void When_Reached_Memory_Threshold_Perform_Task(int maxMgPersistenceCache) {
        evictExpirableRecordsPersistenceUT = new EvictExpirableRecordsPersistence(memory, disk, maxMgPersistenceCache);

        populate(true);
        assertThat(disk.allKeys().size(), is(mocksCount()));

        TestSubscriber testSubscriber = new TestSubscriber();
        evictExpirableRecordsPersistenceUT.startTaskIfNeeded().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();

        int expectedStoredMB = (int) (maxMgPersistenceCache * EvictExpirableRecordsPersistence.PERCENTAGE_MEMORY_STORED_TO_STOP);
        assertThat(expectedStoredMB, is(disk.storedMB()));
    }

    @Test public void When_Reached_Memory_Threshold_But_Not_Expirable_Records_Do_Not_Evict() {
        int maxMgPersistenceCache = 5;

        evictExpirableRecordsPersistenceUT = new EvictExpirableRecordsPersistence(memory, disk, maxMgPersistenceCache);

        populate(false);
        assertThat(disk.allKeys().size(), is(mocksCount()));

        TestSubscriber testSubscriber = new TestSubscriber();
        evictExpirableRecordsPersistenceUT.startTaskIfNeeded().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();
        testSubscriber.assertNoValues();

        assertThat(sizeMbDataPopulated(), is(disk.storedMB()));

        //after first time does not start process again, just return warning message
        testSubscriber = new TestSubscriber();
        evictExpirableRecordsPersistenceUT.startTaskIfNeeded().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValue(Locale.RECORD_CAN_NOT_BE_EVICTED_BECAUSE_NO_ONE_IS_EXPIRABLE);
    }

    //7 mb
    private void populate(boolean expirable) {
        for (int i = 0; i < mocksCount(); i++) {
            List<Mock> mocks = new ArrayList(mocksCount());

            for (int z = 0; z < mocksCount(); z++) {
                Mock mock = new Mock("Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC," +
                        "making it over 2000 years old.Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, " +
                        "making it over 2000 years old. Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, " +
                        "making it over 2000 years old. Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC.");
                mocks.add(mock);
            }

            Record<List<Mock>> record = new Record<>(mocks, expirable? ONE_MILLI_LIFE: NOT_EXPIRABLE_LIFE);
            disk.saveRecord(String.valueOf(i), record);
        }
    }

    private int mocksCount() {
        return 100;
    }

    private int sizeMbDataPopulated() {
        return 7;
    }
}