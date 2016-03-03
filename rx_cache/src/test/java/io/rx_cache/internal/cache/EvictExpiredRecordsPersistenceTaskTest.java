package io.rx_cache.internal.cache;

import org.junit.Test;

import java.util.List;

import io.rx_cache.PolicyHeapCache;
import io.rx_cache.Record;
import io.rx_cache.internal.GuavaMemory;
import io.rx_cache.internal.Memory;
import io.rx_cache.internal.Mock;
import io.rx_cache.internal.common.BaseTest;
import rx.observers.TestSubscriber;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by victor on 03/03/16.
 */
public class EvictExpiredRecordsPersistenceTaskTest extends BaseTest {
    private EvictExpiredRecordsPersistenceTask evictExpiredRecordsPersistenceTaskUT;
    private HasRecordExpired hasRecordExpired;
    private TwoLayersCache twoLayersCache;
    private final Memory memory = new GuavaMemory(PolicyHeapCache.CONSERVATIVE);
    private static final long ONE_SECOND_LIFE = 1000, THIRTY_SECOND_LIFE = 30000, MORE_THAN_ONE_SECOND_LIFE = 1250;

    @Override public void setUp() {
        super.setUp();

        twoLayersCache = new TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));
        hasRecordExpired = new HasRecordExpired();
        evictExpiredRecordsPersistenceTaskUT = new EvictExpiredRecordsPersistenceTask(hasRecordExpired, disk);
    }

    @Test public void Evict_Just_Expired_Records() {
        int recordsCount = 100;

        for (int i = 0; i < recordsCount/2; i++) {
            twoLayersCache.save(i+"_expired", "", "", new Mock(i+"_expired"), ONE_SECOND_LIFE);
            twoLayersCache.save(i+"_live", "", "", new Mock(i+"_live"), THIRTY_SECOND_LIFE);
        }

        waitTime(MORE_THAN_ONE_SECOND_LIFE);

        assertThat(disk.allKeys().size(), is(recordsCount));

        TestSubscriber testSubscriber = new TestSubscriber();
        evictExpiredRecordsPersistenceTaskUT.startEvictingExpiredRecords().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(recordsCount / 2);

        List<String> allKeys = disk.allKeys();
        assertThat(allKeys.size(), is(recordsCount / 2));

        for (String key : allKeys) {
            key = key.substring(0, key.indexOf("$"));
            Record<Mock> record = twoLayersCache.retrieve(key, "", "", false, THIRTY_SECOND_LIFE);
            assert(record.getData().getMessage().contains("live"));
            assert(!record.getData().getMessage().contains("expired"));
        }
    }

    @Test public void Call_On_Complete_With_No_Records_To_Evict() {
        TestSubscriber testSubscriber = new TestSubscriber();
        evictExpiredRecordsPersistenceTaskUT.startEvictingExpiredRecords().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertNoErrors();
        testSubscriber.assertNoValues();
        testSubscriber.assertCompleted();
    }

    private SaveRecord saveRecord(Memory memory) {
        return new SaveRecord(memory, disk);
    }

    private EvictRecord evictRecord(Memory memory) {
        return new EvictRecord(memory, disk);
    }

    private RetrieveRecord retrieveRecord(Memory memory) {
        return new RetrieveRecord(memory, disk, new EvictRecord(memory, disk), new HasRecordExpired());
    }
}
