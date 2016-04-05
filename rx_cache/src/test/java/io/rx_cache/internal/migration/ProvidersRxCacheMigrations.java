package io.rx_cache.internal.migration;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.ArrayList;
import java.util.List;

import io.rx_cache.Migration;
import io.rx_cache.PolicyHeapCache;
import io.rx_cache.SchemeMigration;
import io.rx_cache.internal.RxCache;
import rx.Observable;
import rx.exceptions.CompositeException;
import rx.observers.TestSubscriber;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ProvidersRxCacheMigrations {
    @ClassRule static public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test public void _1_Populate_Mocks() {
        populateMocks();
    }

    @Test public void _2_When_Migrations_Working_Request_Will_Be_Hold_Until_Finish() {
        int countFiles = temporaryFolder.getRoot().listFiles().length;
        assert countFiles > 0;

        ProvidersMigrations providersMigrations = new RxCache.Builder()
                .withPolicyCache(PolicyHeapCache.MODERATE)
                .persistence(temporaryFolder.getRoot())
                .using(ProvidersMigrations.class);

        TestSubscriber<List<Mock1>> testSubscriber = new TestSubscriber<>();
        providersMigrations.getMocks(Observable.<List<Mock1>>just(null)).subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoValues();
        testSubscriber.assertError(CompositeException.class);
    }

    private void populateMocks() {
        Providers providers = new RxCache.Builder()
                .withPolicyCache(PolicyHeapCache.MODERATE)
                .persistence(temporaryFolder.getRoot())
                .using(Providers.class);

        TestSubscriber<List<Mock1>> testSubscriber = new TestSubscriber<>();
        providers.getMocks(getMocks()).subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        assertThat(testSubscriber.getOnNextEvents().get(0).size(), is(SIZE_MOCKS));
    }

    private static int SIZE_MOCKS = 1000;
    private Observable<List<Mock1>> getMocks() {
        List<Mock1> mocks = new ArrayList<>();

        for (int i = 0; i < SIZE_MOCKS; i++) {
            mocks.add(new Mock1());
        }

        return Observable.just(mocks);
    }

    private interface Providers {
        Observable<List<Mock1>> getMocks(Observable<List<Mock1>> mocks);
    }

    @SchemeMigration({@Migration(version = 1, evictClasses = Mock1.class)})
    private interface ProvidersMigrations {
        Observable<List<Mock1>> getMocks(Observable<List<Mock1>> mocks);
    }

    private class Mock1 {
        private final String payload = "Lorem Ipsum is simply dummy text of the printing and " +
                "typesetting industry. Lorem Ipsum has been the industry's standard dummy text " +
                "ever since the 1500s, when an unknown printer took a galley of type and scrambled " +
                "it to make a type specimen book. It has survived not only five centuries";
    }
}
