package io.rx_cache.internal;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.rx_cache.DynamicKey;
import io.rx_cache.PolicyHeapCache;
import io.rx_cache.internal.common.BaseTest;
import rx.Observable;
import rx.observers.TestSubscriber;

/**
 * Created by victor on 03/03/16.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProvidersRxCacheEvictTask extends BaseTest {
    @ClassRule public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    private ProvidersRxCache providersRxCache;

    @Before public void setUp() {
        providersRxCache = new RxCache.Builder()
                .withPolicyCache(PolicyHeapCache.MODERATE)
                .persistence(temporaryFolder.getRoot())
                .using(ProvidersRxCache.class);
    }

    @Test public void _1_Populate_Disk_With_Expired_Records_But_No_Retrievable_Keys() {
        assert getSizeInBytes() == 0;

        for (int i = 0; i < 100; i++) {
            TestSubscriber<List<Mock>> subscriber = new TestSubscriber<>();
            String key = System.currentTimeMillis() + i + "";
            providersRxCache.getEphemeralMocksPaginate(createObservableMocks(100), new DynamicKey(key)).subscribe(subscriber);
            subscriber.awaitTerminalEvent();
        }

        assert getSizeInBytes() > 0;
    }

    @Test public void _2_Perform_Evicting_Task_And_Check_Results() {
        waitTime(1000);
        assert getSizeInBytes() == 0;
    }

    @Test public void _3_Populate_Disk_With_No_Expired_Records_But_No_Retrievable_Keys() {
        assert getSizeInBytes() == 0;

        for (int i = 0; i < 100; i++) {
            TestSubscriber<List<Mock>> subscriber = new TestSubscriber<>();
            String key = System.currentTimeMillis() + i + "";
            providersRxCache.getMocksPaginate(createObservableMocks(10), new DynamicKey(key)).subscribe(subscriber);
            subscriber.awaitTerminalEvent();
        }

        assert getSizeInBytes() > 0;
    }

    @Test public void _4_Perform_Evicting_Task_And_Check_Results() {
        waitTime(1000);
        assert getSizeInBytes() > 0;
    }

    private long getSizeInBytes() {
        long bytes = 0;

        File[] files = temporaryFolder.getRoot().listFiles();
        for (File file: files) {
            bytes += file.length();
        }

        return bytes;
    }

    private Observable<List<Mock>> createObservableMocks(int size) {
        List<Mock> mocks = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            mocks.add(new Mock("Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old."));
        }

        return Observable.just(mocks);
    }
}
