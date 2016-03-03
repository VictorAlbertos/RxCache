package io.rx_cache.internal.cache;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.rx_cache.Persistence;
import io.rx_cache.Record;
import io.rx_cache.internal.common.BaseTest;
import rx.observers.TestSubscriber;

/**
 * Created by victor on 03/03/16.
 */
public class EvictExpiredRecordsTaskTest extends BaseTest {
    private EvictExpiredRecordsTask evictExpiredRecordsTaskUT;
    private Persistence persistence;

    @Override public void setUp() {
        super.setUp();
        persistence = new MockPersistence();
        evictExpiredRecordsTaskUT = new EvictExpiredRecordsTask(persistence);
    }

    @Test public void test() {
        TestSubscriber testSubscriber = new TestSubscriber();
        evictExpiredRecordsTaskUT.startEvictingExpiredRecords(testSubscriber);
        testSubscriber.awaitTerminalEvent();
    }

    private static class MockPersistence implements Persistence {
        @Override public void saveRecord(String key, Record record) {}

        @Override public void evict(String key) {
            try {Thread.sleep(100);}
            catch (InterruptedException e) { e.printStackTrace();}

        }

        @Override public void evictAll() {}

        @Override public List<String> allKeys() {
            List<String> keys = new ArrayList<>(1000);
            for (int i = 0; i < 1000; i++) {
                keys.add("i");
            }
            return keys;
        }

        @Override public <T> Record<T> retrieveRecord(String key) {
            return null;
        }
    }
}
