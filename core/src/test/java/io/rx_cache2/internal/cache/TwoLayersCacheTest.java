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

package io.rx_cache2.internal.cache;

import io.rx_cache2.Source;
import io.rx_cache2.internal.Memory;
import io.rx_cache2.internal.Mock;
import io.rx_cache2.internal.Record;
import io.rx_cache2.internal.cache.memory.ReferenceMapMemory;
import io.rx_cache2.internal.common.BaseTest;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

/**
 * Created by victor on 20/12/15.
 */
public class TwoLayersCacheTest extends BaseTest {
    private io.rx_cache2.internal.cache.TwoLayersCache twoLayersCacheUT;
    private Memory memory;

    private static final long ONE_SECOND_LIFE = 1000, THREE_SECOND_LIFE = 3000, MORE_THAN_ONE_SECOND_LIFE = 1250, DUMMY_LIFE_TIME = -1;
    private static final String PROVIDER_KEY = "get_mocks",  MOCK_VALUE = "mock_value";

    @Override public void setUp() {
        super.setUp();
        memory = new ReferenceMapMemory();
    }

    @Test public void When_Save_And_Object_Not_Expired_And_Memory_Not_Destroyed_Retrieve_It_From_Memory() {
        twoLayersCacheUT = new io.rx_cache2.internal.cache.TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "", "", new Mock(MOCK_VALUE), DUMMY_LIFE_TIME, true, false);

        Record<Mock> record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "", "", false, ONE_SECOND_LIFE + 1000, false);
        Mock mock = record.getData();

        assertThat(mock.getMessage(), is(MOCK_VALUE));
        assertThat(record.getSource(), is(Source.MEMORY));
    }

    @Test public void When_Save_And_Record_Has_Not_Expired_And_Memory_Destroyed_Retrieve_It_From_Disk() {
        twoLayersCacheUT = new io.rx_cache2.internal.cache.TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "", "", new Mock(MOCK_VALUE), DUMMY_LIFE_TIME, true, false);
        twoLayersCacheUT.mockMemoryDestroyed();

        Record<Mock> record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "", "", false, ONE_SECOND_LIFE, false);
        Mock mock = record.getData();

        assertThat(mock.getMessage(), is(MOCK_VALUE));
        assertThat(record.getSource(), is(Source.PERSISTENCE));

        record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "", "", false, ONE_SECOND_LIFE, false);
        assertThat(mock.getMessage(), is(MOCK_VALUE));
        assertThat(record.getSource(), is(Source.MEMORY));
    }

    @Test public void When_Save_And_Provider_Record_Has_Expired_Get_Null() {
        twoLayersCacheUT = new io.rx_cache2.internal.cache.TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "", "", new Mock(MOCK_VALUE), DUMMY_LIFE_TIME, true, false);
        waitTime(MORE_THAN_ONE_SECOND_LIFE);
        Record<Mock> record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "", "", false, ONE_SECOND_LIFE, false);
        assertThat(record, is(nullValue()));

        record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "", "", false, THREE_SECOND_LIFE, false);
        assertThat(record, is(nullValue()));
    }

    @Test public void When_Save_And_Dynamic_Key_Record_Has_Expired_Only_Get_Null_For_Dynamic_Key() {
        twoLayersCacheUT = new io.rx_cache2.internal.cache.TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "1", "", new Mock(MOCK_VALUE), ONE_SECOND_LIFE, true, false);
        twoLayersCacheUT.save(PROVIDER_KEY, "2", "", new Mock(MOCK_VALUE), ONE_SECOND_LIFE, true, false);

        waitTime(MORE_THAN_ONE_SECOND_LIFE);
        Record<Mock> record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "1", "", false, ONE_SECOND_LIFE, false);
        assertThat(record, is(nullValue()));

        record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "1", "", false, THREE_SECOND_LIFE, false);
        assertThat(record, is(nullValue()));

        record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "2", "", false, THREE_SECOND_LIFE, false);
        assertNotNull(record);
    }

    @Test public void When_Save_And_Dynamic_Key_Group_Record_Has_Expired_Only_Get_Null_For_Dynamic_Key() {
        twoLayersCacheUT = new io.rx_cache2.internal.cache.TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "1", "1", new Mock(MOCK_VALUE), ONE_SECOND_LIFE, true, false);
        twoLayersCacheUT.save(PROVIDER_KEY, "1", "2", new Mock(MOCK_VALUE), DUMMY_LIFE_TIME, true, false);
        twoLayersCacheUT.save(PROVIDER_KEY, "2", "1", new Mock(MOCK_VALUE), DUMMY_LIFE_TIME, true, false);
        twoLayersCacheUT.save(PROVIDER_KEY, "2", "2", new Mock(MOCK_VALUE), DUMMY_LIFE_TIME, true, false);

        waitTime(MORE_THAN_ONE_SECOND_LIFE);
        Record<Mock> record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "1", "1", false, ONE_SECOND_LIFE, false);
        assertThat(record, is(nullValue()));

        record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "1", "1", false, THREE_SECOND_LIFE, false);
        assertThat(record, is(nullValue()));

        record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "1", "2", false, THREE_SECOND_LIFE, false);
        assertNotNull(record);

        record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "2", "1", false, THREE_SECOND_LIFE, false);
        assertNotNull(record);

        record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "2", "2", false, THREE_SECOND_LIFE, false);
        assertNotNull(record);
    }

    @Test public void When_Save_And_Record_Has_Not_Expired_Date_Do_Not_Get_Null() {
        twoLayersCacheUT = new io.rx_cache2.internal.cache.TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "", "", new Mock(MOCK_VALUE), DUMMY_LIFE_TIME, true, false);
        waitTime(MORE_THAN_ONE_SECOND_LIFE);
        Record<Mock> record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "", "", false, null, false);

        assertThat(record.getData().getMessage(), is(MOCK_VALUE));
        assertThat(record.getSource(), is(Source.MEMORY));

        twoLayersCacheUT.mockMemoryDestroyed();
        record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "", "", false, null, false);
        assertThat(record.getData().getMessage(), is(MOCK_VALUE));
        assertThat(record.getSource(), is(Source.PERSISTENCE));
    }

    @Test public void When_Save_And_Evict_Get_Null() {
        twoLayersCacheUT = new io.rx_cache2.internal.cache.TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "", "", new Mock(MOCK_VALUE), DUMMY_LIFE_TIME, true, false);
        twoLayersCacheUT.evictProviderKey(PROVIDER_KEY);
        Record<Mock> record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "", "", false, ONE_SECOND_LIFE, false);

        assertThat(record, is(nullValue()));
    }

    @Test public void When_Save_And_Evict_All_Get_Null() {
        twoLayersCacheUT = new io.rx_cache2.internal.cache.TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "", "", new Mock(MOCK_VALUE), DUMMY_LIFE_TIME, true, false);
        twoLayersCacheUT.save(PROVIDER_KEY, "" +1, "", new Mock(MOCK_VALUE), DUMMY_LIFE_TIME, true, false);
        twoLayersCacheUT.save(PROVIDER_KEY +1, "", "", new Mock(MOCK_VALUE), DUMMY_LIFE_TIME, true, false);
        twoLayersCacheUT.save(PROVIDER_KEY +1, "" +1, "", new Mock(MOCK_VALUE), DUMMY_LIFE_TIME, true, false);

        twoLayersCacheUT.evictAll();

        Record<Mock> record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "", "", false, ONE_SECOND_LIFE, false);
        assertThat(record, is(nullValue()));
        record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "", "", false, ONE_SECOND_LIFE, false);
        assertThat(record, is(nullValue()));
        record = twoLayersCacheUT.retrieve(PROVIDER_KEY +1, "", "", false, ONE_SECOND_LIFE, false);
        assertThat(record, is(nullValue()));
        record = twoLayersCacheUT.retrieve(PROVIDER_KEY +1, "", "", false, ONE_SECOND_LIFE, false);
        assertThat(record, is(nullValue()));
    }

    @Test public void When_Save_And_Not_Evict_Dynamic_Keys_Get_All() {
        twoLayersCacheUT = new io.rx_cache2.internal.cache.TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "filter_1", "", new Mock(MOCK_VALUE+1), DUMMY_LIFE_TIME, true, false);
        twoLayersCacheUT.save(PROVIDER_KEY, "filter_2", "", new Mock(MOCK_VALUE + 2), DUMMY_LIFE_TIME, true, false);
        twoLayersCacheUT.save(PROVIDER_KEY, "filter_3", "", new Mock(MOCK_VALUE + 3), DUMMY_LIFE_TIME, true, false);

        Record<Mock> record1 = twoLayersCacheUT.retrieve(PROVIDER_KEY, "filter_1", "", false, ONE_SECOND_LIFE, false);
        Record<Mock> record2 = twoLayersCacheUT.retrieve(PROVIDER_KEY, "filter_2", "", false, ONE_SECOND_LIFE, false);
        Record<Mock> record3 = twoLayersCacheUT.retrieve(PROVIDER_KEY, "filter_3", "", false, ONE_SECOND_LIFE, false);

        assertThat(record1.getData().getMessage(), is(MOCK_VALUE + 1));
        assertThat(record2.getData().getMessage(), is(MOCK_VALUE + 2));
        assertThat(record3.getData().getMessage(), is(MOCK_VALUE+3));
    }

    @Test public void When_Save_Dynamic_Key_And_Re_Save_Dynamic_Key_Get_Last_Value() {
        twoLayersCacheUT = new io.rx_cache2.internal.cache.TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "1", "", new Mock(MOCK_VALUE + 1), DUMMY_LIFE_TIME, true, false);
        twoLayersCacheUT.save(PROVIDER_KEY, "1", "", new Mock(MOCK_VALUE + 2), DUMMY_LIFE_TIME, true, false);

        Record<Mock> record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "1", "", false, ONE_SECOND_LIFE, false);

        assertThat(record.getData().getMessage(), is(MOCK_VALUE+2));
    }

    @Test public void When_Save_Dynamic_Keys_And_Evict_Provider_Key_Get_All_Null() {
        twoLayersCacheUT = new io.rx_cache2.internal.cache.TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "filer_1", "", new Mock(MOCK_VALUE), DUMMY_LIFE_TIME, true, false);
        twoLayersCacheUT.save(PROVIDER_KEY, "filer_2", "", new Mock(MOCK_VALUE), DUMMY_LIFE_TIME, true, false);
        twoLayersCacheUT.save(PROVIDER_KEY, "filer_3", "", new Mock(MOCK_VALUE), DUMMY_LIFE_TIME, true, false);

        twoLayersCacheUT.evictProviderKey(PROVIDER_KEY);

        assertThat(twoLayersCacheUT.retrieve(PROVIDER_KEY, "filer_1", "", false, ONE_SECOND_LIFE, false), is(nullValue()));
        assertThat(twoLayersCacheUT.retrieve(PROVIDER_KEY, "filer_2", "", false, ONE_SECOND_LIFE, false), is(nullValue()));
        assertThat(twoLayersCacheUT.retrieve(PROVIDER_KEY, "filer_3", "", false, ONE_SECOND_LIFE, false), is(nullValue()));
    }

    @Test public void When_Save_Dynamic_Key_And_Evict_One_Dynamic_Key_Get_Others() {
        twoLayersCacheUT = new io.rx_cache2.internal.cache.TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "filer_1", "", new Mock(MOCK_VALUE), DUMMY_LIFE_TIME, true, false);
        twoLayersCacheUT.save(PROVIDER_KEY, "filer_2", "", new Mock(MOCK_VALUE + 1), DUMMY_LIFE_TIME, true, false);
        twoLayersCacheUT.save(PROVIDER_KEY, "filer_3", "", new Mock(MOCK_VALUE + 2), DUMMY_LIFE_TIME, true, false);

        twoLayersCacheUT.evictDynamicKey(PROVIDER_KEY, "filer_1");

        assertThat(twoLayersCacheUT.retrieve(PROVIDER_KEY, "filer_1", "", false, ONE_SECOND_LIFE, false), is(nullValue()));

        Record<Mock> record1 = twoLayersCacheUT.retrieve(PROVIDER_KEY, "filer_2", "", false, ONE_SECOND_LIFE, false);
        assertThat(record1.getData().getMessage(), is(MOCK_VALUE + 1));

        Record<Mock> record2 = twoLayersCacheUT.retrieve(PROVIDER_KEY, "filer_3", "", false, ONE_SECOND_LIFE, false);
        assertThat(record2.getData().getMessage(), is(MOCK_VALUE + 2));
    }

    @Test public void When_Save_Dynamic_Key_Group_And_Evict_One_Dynamic_Key_Group_Get_Others() {
        twoLayersCacheUT = new io.rx_cache2.internal.cache.TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "filer_1", "page_1", new Mock(MOCK_VALUE), DUMMY_LIFE_TIME, true, false);
        twoLayersCacheUT.save(PROVIDER_KEY, "filer_1", "page_2", new Mock(MOCK_VALUE + 1), DUMMY_LIFE_TIME, true, false);

        twoLayersCacheUT.evictDynamicKeyGroup(PROVIDER_KEY, "filer_1", "page_2");

        assertThat(twoLayersCacheUT.retrieve(PROVIDER_KEY, "filer_1", "page_2", false, ONE_SECOND_LIFE, false), is(nullValue()));

        Record<Mock> record1 = twoLayersCacheUT.retrieve(PROVIDER_KEY, "filer_1", "page_1", false, ONE_SECOND_LIFE, false);
        assertThat(record1.getData().getMessage(), is(MOCK_VALUE));
    }

    @Test public void When_Expiration_Date_Has_Been_Modified_Then_Reflect_This_Change() {
        twoLayersCacheUT = new io.rx_cache2.internal.cache.TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "1", "", new Mock(MOCK_VALUE), DUMMY_LIFE_TIME, true, false);
        waitTime(MORE_THAN_ONE_SECOND_LIFE);

        Record<Mock> record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "1", "", false, THREE_SECOND_LIFE, false);
        assertThat(record.getData().getMessage(), is(MOCK_VALUE));

        record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "1", "", false, ONE_SECOND_LIFE, false);
        assertThat(record, is(nullValue()));

        twoLayersCacheUT.save(PROVIDER_KEY, "1", "", new Mock(MOCK_VALUE), DUMMY_LIFE_TIME, true, false);
        waitTime(MORE_THAN_ONE_SECOND_LIFE);

        record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "1", "", false, THREE_SECOND_LIFE, false);
        assertThat(record.getData().getMessage(), is(MOCK_VALUE));

        record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "1", "", false, ONE_SECOND_LIFE, false);
        assertThat(record, is(nullValue()));

        twoLayersCacheUT.save(PROVIDER_KEY, "1", "", new Mock(MOCK_VALUE), DUMMY_LIFE_TIME, true, false);
        waitTime(MORE_THAN_ONE_SECOND_LIFE);

        record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "1", "", false, null, false);
        assertThat(record.getData().getMessage(), is(MOCK_VALUE));
    }

    @Test public void When_Expired_Date_And_Not_Use_ExpiredDataIfLoaderNotAvailable_Then_Get_Null() {
        twoLayersCacheUT = new io.rx_cache2.internal.cache.TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "1", "", new Mock(MOCK_VALUE), DUMMY_LIFE_TIME, true, false);
        waitTime(MORE_THAN_ONE_SECOND_LIFE);

        Record<Mock> record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "1", "", false, ONE_SECOND_LIFE, false);
        assertThat(record, is(nullValue()));
    }

    @Test public void When_Expired_Date_But_Use_ExpiredDataIfLoaderNotAvailable_Then_GetMock() {
        twoLayersCacheUT = new TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));

        twoLayersCacheUT.save(PROVIDER_KEY, "1", "", new Mock(MOCK_VALUE), DUMMY_LIFE_TIME, true, false);
        waitTime(MORE_THAN_ONE_SECOND_LIFE);

        Record<Mock> record = twoLayersCacheUT.retrieve(PROVIDER_KEY, "1", "", true, ONE_SECOND_LIFE, false);
        assertThat(record.getData().getMessage(), is(MOCK_VALUE));
    }


    protected io.rx_cache2.internal.cache.SaveRecord saveRecord(Memory memory) {
        return new SaveRecord(memory, disk, 100, new EvictExpirableRecordsPersistence(memory, disk, 100, null), null);
    }

    protected io.rx_cache2.internal.cache.EvictRecord evictRecord(Memory memory) {
        return new io.rx_cache2.internal.cache.EvictRecord(memory, disk);
    }

    protected io.rx_cache2.internal.cache.RetrieveRecord retrieveRecord(Memory memory) {
        return new io.rx_cache2.internal.cache.RetrieveRecord(memory, disk, new EvictRecord(memory, disk), new HasRecordExpired(), null);
    }
}
