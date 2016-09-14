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

package io.rx_cache2.internal.cache;

import io.rx_cache2.internal.Memory;
import io.rx_cache2.internal.Mock;
import io.rx_cache2.internal.cache.memory.ReferenceMapMemory;
import io.rx_cache2.internal.common.BaseTest;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(Theories.class)
public class SaveRecordTest extends BaseTest {
    private Memory memory;
    private io.rx_cache2.internal.cache.SaveRecord saveRecordUT;

    @DataPoint public static Integer _10_MB = 10;
    @DataPoint public static Integer _20_MB = 20;
    @DataPoint public static Integer _30_MB = 30;

    @Override public void setUp() {
        super.setUp();
        memory = new ReferenceMapMemory();
    }

    @Test @Theory public void When_Max_Persistence_Exceed_Do_Not_Persists_Data(Integer maxMB) {
        saveRecordUT = new SaveRecord(memory, disk, maxMB, new io.rx_cache2.internal.cache.EvictExpirableRecordsPersistence(memory, disk, 100, null), null);

        int records = 250;

        //39 megabytes of memory
        for (int i = 0; i < records; i++) {
            saveRecordUT.save(i+"", "", "", createMocks(records), null, true, false);
        }

        assertTrue("storedMB minor or equal than " + maxMB, disk.storedMB() <= maxMB);
        assertThat(memory.keySet().size(), is(records));
    }

    private List<Mock> createMocks(int size) {
        List<Mock> mocks = new ArrayList(size);

        for (int i = 0; i < size; i++) {
            mocks.add(new Mock("Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC," +
                    "making it over 2000 years old.Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, " +
                    "making it over 2000 years old. Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, " +
                    "making it over 2000 years old. Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC."));
        }

        return mocks;
    }
}