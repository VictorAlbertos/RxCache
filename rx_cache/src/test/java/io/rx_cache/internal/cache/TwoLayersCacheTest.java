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

package io.rx_cache.internal.cache;

import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import io.rx_cache.PolicyHeapCache;
import io.rx_cache.Record;
import io.rx_cache.Source;
import io.rx_cache.internal.GuavaMemory;
import io.rx_cache.internal.Memory;
import io.rx_cache.internal.Mock;
import io.rx_cache.internal.SimpleMemory;
import io.rx_cache.internal.common.BaseTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

/**
 * Created by victor on 20/12/15.
 */
@RunWith(Theories.class)
public class TwoLayersCacheTest extends BaseTest {
    private TwoLayersCache twoLayersCacheUT;
    private static final long ONE_SECOND_LIFE = 1000, THREE_SECOND_LIFE = 3000, MORE_THAN_ONE_SECOND_LIFE = 1250;
    private static final String PROVIDER_KEY = "get_mocks",  MOCK_VALUE = "mock_value";

    @DataPoint public static Memory GUAVA_MEMORY = new GuavaMemory(PolicyHeapCache.CONSERVATIVE);
    @DataPoint public static Memory SIMPLE_MEMORY = new SimpleMemory();


    @Theory @Test public void When_Save_And_Object_Not_Expired_And_Memory_Not_Destroyed_Retrieve_It_From_Memory(Memory memory) {
        twoLayersCacheUT = new TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "", "", new Mock(MOCK_VALUE));

        Record<Mock> record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "", "", false, ONE_SECOND_LIFE + 1000);
        Mock mock = record.getData();

        assertThat(mock.getMessage(), is(MOCK_VALUE));
        assertThat(record.getSource(), is(Source.MEMORY));
    }

    @Theory
    @Test public void When_Save_And_Record_Has_Not_Expired_And_Memory_Destroyed_Retrieve_It_From_Disk(Memory memory) {
        twoLayersCacheUT = new TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "", "", new Mock(MOCK_VALUE));
        twoLayersCacheUT.mockMemoryDestroyed();

        Record<Mock> record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "", "", false, ONE_SECOND_LIFE);
        Mock mock = record.getData();

        assertThat(mock.getMessage(), is(MOCK_VALUE));
        assertThat(record.getSource(), is(Source.PERSISTENCE));

        record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "", "", false, ONE_SECOND_LIFE);
        assertThat(mock.getMessage(), is(MOCK_VALUE));
        assertThat(record.getSource(), is(Source.MEMORY));
    }

    @Theory
    @Test public void When_Save_And_Record_Has_Expired_Get_Null(Memory memory) {
        twoLayersCacheUT = new TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "", "", new Mock(MOCK_VALUE));
        waitTime(MORE_THAN_ONE_SECOND_LIFE);
        Record<Mock> record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "", "", false, ONE_SECOND_LIFE);

        assertThat(record, is(nullValue()));
    }

    @Theory
    @Test public void When_Save_And_Record_Has_Not_Expired_Date_Do_Not_Get_Null(Memory memory) {
        twoLayersCacheUT = new TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "", "", new Mock(MOCK_VALUE));
        waitTime(MORE_THAN_ONE_SECOND_LIFE);
        Record<Mock> record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "", "", false, 0);

        assertThat(record.getData().getMessage(), is(MOCK_VALUE));
        assertThat(record.getSource(), is(Source.MEMORY));

        twoLayersCacheUT.mockMemoryDestroyed();
        record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "", "", false, 0);
        assertThat(record.getData().getMessage(), is(MOCK_VALUE));
        assertThat(record.getSource(), is(Source.PERSISTENCE));
    }

    @Theory
    @Test public void When_Save_And_Evict_Get_Null(Memory memory) {
        twoLayersCacheUT = new TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "", "", new Mock(MOCK_VALUE));
        twoLayersCacheUT.evictProviderKey(PROVIDER_KEY);
        Record<Mock> record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "", "", false, ONE_SECOND_LIFE);

        assertThat(record, is(nullValue()));
    }

    @Theory
    @Test public void When_Save_And_Evict_All_Get_Null(Memory memory) {
        twoLayersCacheUT = new TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "", "", new Mock(MOCK_VALUE));
        twoLayersCacheUT.save(PROVIDER_KEY, "" +1, "", new Mock(MOCK_VALUE));
        twoLayersCacheUT.save(PROVIDER_KEY +1, "", "", new Mock(MOCK_VALUE));
        twoLayersCacheUT.save(PROVIDER_KEY +1, "" +1, "", new Mock(MOCK_VALUE));

        twoLayersCacheUT.evictAll();

        Record<Mock> record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "", "", false, ONE_SECOND_LIFE);
        assertThat(record, is(nullValue()));
        record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "", "", false, ONE_SECOND_LIFE);
        assertThat(record, is(nullValue()));
        record = twoLayersCacheUT.retrieve(PROVIDER_KEY +1, "", "", false, ONE_SECOND_LIFE);
        assertThat(record, is(nullValue()));
        record = twoLayersCacheUT.retrieve(PROVIDER_KEY +1, "", "", false, ONE_SECOND_LIFE);
        assertThat(record, is(nullValue()));
    }

    @Theory
    @Test public void When_Save_And_Not_Evict_Dynamic_Keys_Get_All(Memory memory) {
        twoLayersCacheUT = new TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "filter_1", "", new Mock(MOCK_VALUE+1));
        twoLayersCacheUT.save(PROVIDER_KEY, "filter_2", "", new Mock(MOCK_VALUE + 2));
        twoLayersCacheUT.save(PROVIDER_KEY, "filter_3", "", new Mock(MOCK_VALUE + 3));

        Record<Mock> record1 = twoLayersCacheUT.retrieve(PROVIDER_KEY, "filter_1", "", false, ONE_SECOND_LIFE);
        Record<Mock> record2 = twoLayersCacheUT.retrieve(PROVIDER_KEY, "filter_2", "", false, ONE_SECOND_LIFE);
        Record<Mock> record3 = twoLayersCacheUT.retrieve(PROVIDER_KEY, "filter_3", "", false, ONE_SECOND_LIFE);

        assertThat(record1.getData().getMessage(), is(MOCK_VALUE + 1));
        assertThat(record2.getData().getMessage(), is(MOCK_VALUE + 2));
        assertThat(record3.getData().getMessage(), is(MOCK_VALUE+3));
    }

    @Theory
    @Test public void When_Save_Dynamic_Key_And_Re_Save_Dynamic_Key_Get_Last_Value(Memory memory) {
        twoLayersCacheUT = new TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "1", "", new Mock(MOCK_VALUE + 1));
        twoLayersCacheUT.save(PROVIDER_KEY, "1", "", new Mock(MOCK_VALUE + 2));

        Record<Mock> record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "1", "", false, ONE_SECOND_LIFE);

        assertThat(record.getData().getMessage(), is(MOCK_VALUE+2));
    }

    @Theory
    @Test public void When_Save_Dynamic_Keys_And_Evict_Provider_Key_Get_All_Null(Memory memory) {
        twoLayersCacheUT = new TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "filer_1", "", new Mock(MOCK_VALUE));
        twoLayersCacheUT.save(PROVIDER_KEY, "filer_2", "", new Mock(MOCK_VALUE));
        twoLayersCacheUT.save(PROVIDER_KEY, "filer_3", "", new Mock(MOCK_VALUE));

        twoLayersCacheUT.evictProviderKey(PROVIDER_KEY);

        assertThat(twoLayersCacheUT.retrieve(PROVIDER_KEY, "filer_1", "", false, ONE_SECOND_LIFE), is(nullValue()));
        assertThat(twoLayersCacheUT.retrieve(PROVIDER_KEY, "filer_2", "", false, ONE_SECOND_LIFE), is(nullValue()));
        assertThat(twoLayersCacheUT.retrieve(PROVIDER_KEY, "filer_3", "", false, ONE_SECOND_LIFE), is(nullValue()));
    }

    @Theory
    @Test public void When_Save_Dynamic_Key_And_Evict_One_Dynamic_Key_Get_Others(Memory memory) {
        twoLayersCacheUT = new TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "filer_1", "", new Mock(MOCK_VALUE));
        twoLayersCacheUT.save(PROVIDER_KEY, "filer_2", "", new Mock(MOCK_VALUE + 1));
        twoLayersCacheUT.save(PROVIDER_KEY, "filer_3", "", new Mock(MOCK_VALUE + 2));

        twoLayersCacheUT.evictDynamicKey(PROVIDER_KEY, "filer_1");

        assertThat(twoLayersCacheUT.retrieve(PROVIDER_KEY, "filer_1", "", false, ONE_SECOND_LIFE), is(nullValue()));

        Record<Mock> record1 = twoLayersCacheUT.retrieve(PROVIDER_KEY, "filer_2", "", false, ONE_SECOND_LIFE);
        assertThat(record1.getData().getMessage(), is(MOCK_VALUE + 1));

        Record<Mock> record2 = twoLayersCacheUT.retrieve(PROVIDER_KEY, "filer_3", "", false, ONE_SECOND_LIFE);
        assertThat(record2.getData().getMessage(), is(MOCK_VALUE + 2));
    }

    @Theory
    @Test public void When_Save_Dynamic_Key_Group_And_Evict_One_Dynamic_Key_Group_Get_Others(Memory memory) {
        twoLayersCacheUT = new TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "filer_1", "page_1", new Mock(MOCK_VALUE));
        twoLayersCacheUT.save(PROVIDER_KEY, "filer_1", "page_2", new Mock(MOCK_VALUE + 1));

        twoLayersCacheUT.evictDynamicKeyGroup(PROVIDER_KEY, "filer_1", "page_2");

        assertThat(twoLayersCacheUT.retrieve(PROVIDER_KEY, "filer_1", "page_2", false, ONE_SECOND_LIFE), is(nullValue()));

        Record<Mock> record1 = twoLayersCacheUT.retrieve(PROVIDER_KEY, "filer_1", "page_1", false, ONE_SECOND_LIFE);
        assertThat(record1.getData().getMessage(), is(MOCK_VALUE));
    }

    @Theory
    @Test public void When_Expiration_Date_Has_Been_Modified_Then_Reflect_This_Change(Memory memory) {
        twoLayersCacheUT = new TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "1", "", new Mock(MOCK_VALUE));
        waitTime(MORE_THAN_ONE_SECOND_LIFE);

        Record<Mock> record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "1", "", false, THREE_SECOND_LIFE);
        assertThat(record.getData().getMessage(), is(MOCK_VALUE));

        record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "1", "", false, ONE_SECOND_LIFE);
        assertThat(record, is(nullValue()));

        record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "1", "", false, THREE_SECOND_LIFE);
        assertThat(record.getData().getMessage(), is(MOCK_VALUE));

        record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "1", "", false, ONE_SECOND_LIFE);
        assertThat(record, is(nullValue()));

        record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "1", "", false, 0);
        assertThat(record.getData().getMessage(), is(MOCK_VALUE));
    }

    @Theory
    @Test public void When_Expired_Date_But_Use_ExpiredDataIfLoaderNotAvailable_Then_GetMock(Memory memory) {
        twoLayersCacheUT = new TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "1", "", new Mock(MOCK_VALUE));
        waitTime(MORE_THAN_ONE_SECOND_LIFE);

        Record<Mock> record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "1", "", false, ONE_SECOND_LIFE);
        assertThat(record, is(nullValue()));

        record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "1", "", true, ONE_SECOND_LIFE);
        assertThat(record.getData().getMessage(), is(MOCK_VALUE));
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
        GuavaMemory guavaMemory = new GuavaMemory(policy);

        twoLayersCacheUT = new TwoLayersCache(evictRecord(guavaMemory), retrieveRecord(guavaMemory), saveRecord(guavaMemory));
        long maxCacheSizeBytes = guavaMemory.maxCacheSizeBytes(policy);

        long amountMemoryBytes  = Runtime.getRuntime().totalMemory();
        long expectedMemory = (long) (amountMemoryBytes * policy.getPercentageReserved());
        assertThat(maxCacheSizeBytes, is(expectedMemory));
    }

    protected SaveRecord saveRecord(Memory memory) {
        return new SaveRecord(memory, disk);
    }

    protected EvictRecord evictRecord(Memory memory) {
        return new EvictRecord(memory, disk);
    }

    protected RetrieveRecord retrieveRecord(Memory memory) {
        return new RetrieveRecord(memory, disk, new EvictRecord(memory, disk));
    }
}
