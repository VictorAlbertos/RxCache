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

import org.junit.Test;

import io.rx_cache.PolicyHeapCache;
import io.rx_cache.Record;
import io.rx_cache.Source;
import io.rx_cache.internal.common.BaseTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

/**
 * Created by victor on 20/12/15.
 */
public class TwoLayersCacheTest extends BaseTest {
    private TwoLayersCache twoLayersCacheUT;
    private static final long ONE_SECOND_LIFE = 1000, THREE_SECOND_LIFE = 3000, MORE_THAN_ONE_SECOND_LIFE = 1250;
    private static final String KEY = "mock_key", PAGE = "", MOCK_VALUE = "mock_value";

    @Override public void setUp() {
        super.setUp();
        twoLayersCacheUT = new TwoLayersCache(PolicyHeapCache.CONSERVATIVE, disk);
    }

    @Test public void When_Save_And_Object_Not_Expired_And_Memory_Not_Destroyed_Retrieve_It_From_Memory() {
        twoLayersCacheUT.save(KEY, PAGE, new Mock(MOCK_VALUE));

        Record<Mock> record = twoLayersCacheUT.retrieve(KEY, PAGE, false, ONE_SECOND_LIFE + 1000);
        Mock mock = record.getData();

        assertThat(mock.getMessage(), is(MOCK_VALUE));
        assertThat(record.getSource(), is(Source.MEMORY));
    }

    @Test public void When_Save_And_Record_Has_Not_Expired_And_Memory_Destroyed_Retrieve_It_From_Disk() {
        twoLayersCacheUT.save(KEY, PAGE, new Mock(MOCK_VALUE));
        twoLayersCacheUT.mockMemoryDestroyed();

        Record<Mock> record = twoLayersCacheUT.retrieve(KEY, PAGE, false, ONE_SECOND_LIFE);
        Mock mock = record.getData();

        assertThat(mock.getMessage(), is(MOCK_VALUE));
        assertThat(record.getSource(), is(Source.PERSISTENCE));

        record = twoLayersCacheUT.retrieve(KEY, PAGE, false, ONE_SECOND_LIFE);
        assertThat(mock.getMessage(), is(MOCK_VALUE));
        assertThat(record.getSource(), is(Source.MEMORY));
    }

    @Test public void When_Save_And_Record_Has_Expired_Get_Null() {
        twoLayersCacheUT.save(KEY, PAGE, new Mock(MOCK_VALUE));
        waitTime(MORE_THAN_ONE_SECOND_LIFE);
        Record<Mock> record = twoLayersCacheUT.retrieve(KEY, PAGE, false, ONE_SECOND_LIFE);

        assertThat(record, is(nullValue()));
    }

    @Test public void When_Save_And_Record_Has_Not_Expired_Date_Do_Not_Get_Null() {
        twoLayersCacheUT.save(KEY, PAGE, new Mock(MOCK_VALUE));
        waitTime(MORE_THAN_ONE_SECOND_LIFE);
        Record<Mock> record = twoLayersCacheUT.retrieve(KEY, PAGE, false, 0);

        assertThat(record.getData().getMessage(), is(MOCK_VALUE));
        assertThat(record.getSource(), is(Source.MEMORY));

        twoLayersCacheUT.mockMemoryDestroyed();
        record = twoLayersCacheUT.retrieve(KEY, PAGE, false, 0);
        assertThat(record.getData().getMessage(), is(MOCK_VALUE));
        assertThat(record.getSource(), is(Source.PERSISTENCE));
    }

    @Test public void When_Save_And_Clear_Get_Null() {
        twoLayersCacheUT.save(KEY, PAGE, new Mock(MOCK_VALUE));
        twoLayersCacheUT.clear(KEY);
        Record<Mock> record = twoLayersCacheUT.retrieve(KEY, PAGE, false, ONE_SECOND_LIFE);

        assertThat(record, is(nullValue()));
    }

    @Test public void When_Save_And_Not_Clear_Pages_Get_All() {
        twoLayersCacheUT.save(KEY, "1", new Mock(MOCK_VALUE+1));
        twoLayersCacheUT.save(KEY, "2", new Mock(MOCK_VALUE+2));
        twoLayersCacheUT.save(KEY, "3", new Mock(MOCK_VALUE + 3));

        Record<Mock> record1 = twoLayersCacheUT.retrieve(KEY, "1", false, ONE_SECOND_LIFE);
        Record<Mock> record2 = twoLayersCacheUT.retrieve(KEY, "2", false, ONE_SECOND_LIFE);
        Record<Mock> record3 = twoLayersCacheUT.retrieve(KEY, "3", false, ONE_SECOND_LIFE);

        assertThat(record1.getData().getMessage(), is(MOCK_VALUE + 1));
        assertThat(record2.getData().getMessage(), is(MOCK_VALUE + 2));
        assertThat(record3.getData().getMessage(), is(MOCK_VALUE+3));
    }

    @Test public void When_Save_Page_And_Re_Save_Page_Get_Last_Value() {
        twoLayersCacheUT.save(KEY, "1", new Mock(MOCK_VALUE + 1));
        twoLayersCacheUT.save(KEY, "1", new Mock(MOCK_VALUE + 2));

        Record<Mock> record = twoLayersCacheUT.retrieve(KEY, "1", false, ONE_SECOND_LIFE);

        assertThat(record.getData().getMessage(), is(MOCK_VALUE+2));
    }

    @Test public void When_Save_And_Clear_Pages_Get_All_Null() {
        twoLayersCacheUT.save(KEY, "9g34ye__w$$crvgwhq$", new Mock(MOCK_VALUE));
        twoLayersCacheUT.save(KEY, "83fgyewbuh", new Mock(MOCK_VALUE));
        twoLayersCacheUT.save(KEY, "3:973h2uewnsaj", new Mock(MOCK_VALUE));

        twoLayersCacheUT.clear(KEY);

        assertThat(twoLayersCacheUT.retrieve(KEY, "9g34ye__w$$crvgwhq$", false, ONE_SECOND_LIFE), is(nullValue()));
        assertThat(twoLayersCacheUT.retrieve(KEY, "83fgyewbuh", false, ONE_SECOND_LIFE), is(nullValue()));
        assertThat(twoLayersCacheUT.retrieve(KEY, "3:973h2uewnsaj", false, ONE_SECOND_LIFE), is(nullValue()));
    }

    @Test public void When_Save_And_Clear_One_Page_Get_Others() {
        twoLayersCacheUT.save(KEY, "9g34ye__w$$crvgwhq$", new Mock(MOCK_VALUE));
        twoLayersCacheUT.save(KEY, "83fgyewbuh", new Mock(MOCK_VALUE + 1));
        twoLayersCacheUT.save(KEY, "3:973h2uewnsaj", new Mock(MOCK_VALUE + 2));

        twoLayersCacheUT.clearDynamicKey(KEY, "9g34ye__w$$crvgwhq$");

        assertThat(twoLayersCacheUT.retrieve(KEY, "9g34ye__w$$crvgwhq$", false, ONE_SECOND_LIFE), is(nullValue()));

        Record<Mock> record1 = twoLayersCacheUT.retrieve(KEY, "83fgyewbuh", false, ONE_SECOND_LIFE);
        assertThat(record1.getData().getMessage(), is(MOCK_VALUE + 1));

        Record<Mock> record2 = twoLayersCacheUT.retrieve(KEY, "3:973h2uewnsaj", false, ONE_SECOND_LIFE);
        assertThat(record2.getData().getMessage(), is(MOCK_VALUE + 2));
    }

    @Test public void When_Expiration_Date_Has_Been_Modified_Then_Reflect_This_Change() {
        twoLayersCacheUT.save(KEY, "1", new Mock(MOCK_VALUE));
        waitTime(MORE_THAN_ONE_SECOND_LIFE);

        Record<Mock> record = twoLayersCacheUT.retrieve(KEY, "1", false, THREE_SECOND_LIFE);
        assertThat(record.getData().getMessage(), is(MOCK_VALUE));

        record = twoLayersCacheUT.retrieve(KEY, "1", false, ONE_SECOND_LIFE);
        assertThat(record, is(nullValue()));

        record = twoLayersCacheUT.retrieve(KEY, "1", false, THREE_SECOND_LIFE);
        assertThat(record.getData().getMessage(), is(MOCK_VALUE));

        record = twoLayersCacheUT.retrieve(KEY, "1", false, ONE_SECOND_LIFE);
        assertThat(record, is(nullValue()));
    }

    @Test public void Check_Policy_Conservative() {
        checkPolicy(PolicyHeapCache.CONSERVATIVE);
    }

    @Test public void Check_Policy_Moderate() {
        checkPolicy(PolicyHeapCache.MODERATE);
    }

    @Test public void Check_Policy_Aggressive() {
        checkPolicy(PolicyHeapCache.AGGRESSIVE);
    }

    private void checkPolicy(PolicyHeapCache policy) {
        twoLayersCacheUT = new TwoLayersCache(policy, disk);
        long maxCacheSizeBytes = twoLayersCacheUT.maxCacheSizeBytes();

        long amountMemoryBytes  = Runtime.getRuntime().totalMemory();
        long expectedMemory = (long) (amountMemoryBytes * policy.getPercentageReserved());
        assertThat(maxCacheSizeBytes, is(expectedMemory));
    }
}
