package io.rx_cache.internal.cache;

import io.rx_cache.internal.Memory;
import io.rx_cache.internal.Mock;
import io.rx_cache.internal.Record;
import io.rx_cache.internal.cache.memory.ReferenceMapMemory;
import io.rx_cache.internal.common.BaseTest;
import java.util.List;
import org.junit.Test;
import rx.observers.TestSubscriber;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by victor on 03/03/16.
 */
public class EvictExpiredRecordsPersistenceTest extends BaseTest {
    private EvictExpiredRecordsPersistence evictExpiredRecordsPersistenceUT;
    private HasRecordExpired hasRecordExpired;
    private TwoLayersCache twoLayersCache;
    private Memory memory;
    private static final long ONE_SECOND_LIFE = 1000, THIRTY_SECOND_LIFE = 30000, MORE_THAN_ONE_SECOND_LIFE = 1250;

    @Override public void setUp() {
        super.setUp();

        memory = new ReferenceMapMemory();
        twoLayersCache = new TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));
        hasRecordExpired = new HasRecordExpired();
        evictExpiredRecordsPersistenceUT = new EvictExpiredRecordsPersistence(memory, disk, hasRecordExpired, null);
    }

    @Test public void Evict_Just_Expired_Records() {
        int recordsCount = 100;

        for (int i = 0; i < recordsCount/2; i++) {
            twoLayersCache.save(i+"_expired", "", "", new Mock(i+"_expired"), ONE_SECOND_LIFE, true, false);
            twoLayersCache.save(i+"_live", "", "", new Mock(i+"_live"), THIRTY_SECOND_LIFE, true, false);
        }

        waitTime(MORE_THAN_ONE_SECOND_LIFE);

        assertThat(disk.allKeys().size(), is(recordsCount));

        TestSubscriber<Void> testSubscriber = new TestSubscriber();
        evictExpiredRecordsPersistenceUT.startEvictingExpiredRecords().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();

        List<String> allKeys = disk.allKeys();
        assertThat(allKeys.size(), is(recordsCount / 2));

        for (String key : allKeys) {
            key = key.substring(0, key.indexOf("$"));
            Record<Mock> record = twoLayersCache.retrieve(key, "", "", false, THIRTY_SECOND_LIFE, false);
            assert(record.getData().getMessage().contains("live"));
            assert(!record.getData().getMessage().contains("expired"));
        }
    }

    private SaveRecord saveRecord(Memory memory) {
        return new SaveRecord(memory, disk, 100, new EvictExpirableRecordsPersistence(memory, disk, 100, null), null);
    }

    private EvictRecord evictRecord(Memory memory) {
        return new EvictRecord(memory, disk);
    }

    private RetrieveRecord retrieveRecord(Memory memory) {
        return new RetrieveRecord(memory, disk, new EvictRecord(memory, disk), new HasRecordExpired(), null);
    }
}
