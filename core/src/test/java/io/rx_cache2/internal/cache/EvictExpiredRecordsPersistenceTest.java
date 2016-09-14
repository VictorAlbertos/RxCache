package io.rx_cache2.internal.cache;

import io.reactivex.observers.TestObserver;
import io.rx_cache2.internal.Memory;
import io.rx_cache2.internal.Mock;
import io.rx_cache2.internal.Record;
import io.rx_cache2.internal.cache.memory.ReferenceMapMemory;
import io.rx_cache2.internal.common.BaseTest;
import java.util.List;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by victor on 03/03/16.
 */
public class EvictExpiredRecordsPersistenceTest extends BaseTest {
    private io.rx_cache2.internal.cache.EvictExpiredRecordsPersistence
        evictExpiredRecordsPersistenceUT;
    private io.rx_cache2.internal.cache.HasRecordExpired hasRecordExpired;
    private io.rx_cache2.internal.cache.TwoLayersCache twoLayersCache;
    private Memory memory;
    private static final long ONE_SECOND_LIFE = 1000, THIRTY_SECOND_LIFE = 30000, MORE_THAN_ONE_SECOND_LIFE = 1250;

    @Override public void setUp() {
        super.setUp();

        memory = new ReferenceMapMemory();
        twoLayersCache = new TwoLayersCache(evictRecord(memory), retrieveRecord(memory), saveRecord(memory));
        hasRecordExpired = new io.rx_cache2.internal.cache.HasRecordExpired();
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

        TestObserver<Integer> testObserver = evictExpiredRecordsPersistenceUT.startEvictingExpiredRecords().test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();

        List<String> allKeys = disk.allKeys();
        assertThat(allKeys.size(), is(recordsCount / 2));

        for (String key : allKeys) {
            key = key.substring(0, key.indexOf("$"));
            Record<Mock> record = twoLayersCache.retrieve(key, "", "", false, THIRTY_SECOND_LIFE, false);
            assert(record.getData().getMessage().contains("live"));
            assert(!record.getData().getMessage().contains("expired"));
        }
    }

    private io.rx_cache2.internal.cache.SaveRecord saveRecord(Memory memory) {
        return new SaveRecord(memory, disk, 100, new EvictExpirableRecordsPersistence(memory, disk, 100, null), null);
    }

    private io.rx_cache2.internal.cache.EvictRecord evictRecord(Memory memory) {
        return new io.rx_cache2.internal.cache.EvictRecord(memory, disk);
    }

    private io.rx_cache2.internal.cache.RetrieveRecord retrieveRecord(Memory memory) {
        return new io.rx_cache2.internal.cache.RetrieveRecord(memory, disk, new EvictRecord(memory, disk), new HasRecordExpired(), null);
    }
}
