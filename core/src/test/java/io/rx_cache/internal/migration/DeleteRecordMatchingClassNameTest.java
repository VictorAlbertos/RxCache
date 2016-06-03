package io.rx_cache.internal.migration;

import org.junit.Test;

import java.util.Arrays;

import io.rx_cache.internal.ProvidersRxCache;
import io.rx_cache.internal.Record;
import io.rx_cache.internal.common.BaseTest;
import io.rx_cache.internal.encrypt.GetEncryptKey;
import rx.observers.TestSubscriber;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class DeleteRecordMatchingClassNameTest extends BaseTest {
    private DeleteRecordMatchingClassName deleteRecordMatchingClassNameUT;
    private TestSubscriber<Void> testSubscriber;
    private GetEncryptKey getEncryptKey;

    @Override public void setUp() {
        super.setUp();
        getEncryptKey = new GetEncryptKey(ProvidersRxCache.class);
        deleteRecordMatchingClassNameUT = new DeleteRecordMatchingClassName(disk, getEncryptKey);
    }

    @Test public void When_Class_Matches_Delete_Record_1() {
        disk.saveRecord(Mock1.KEY, new Record(new Mock1(), true, 0l), false, null);
        disk.saveRecord(Mock2.KEY,  new Record(new Mock2(), true, 0l), false, null);

        assertThat(disk.allKeys().size(), is(2));

        testSubscriber = new TestSubscriber<>();
        deleteRecordMatchingClassNameUT.with(Arrays.<Class>asList(Mock1.class)).react().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        assertThat(disk.allKeys().size(), is(1));
    }

    @Test public void When_Class_Matches_Delete_Record_2() {
        disk.saveRecord(Mock1.KEY, new Record(new Mock1(), true, 0l), false, null);
        disk.saveRecord(Mock2.KEY,  new Record(new Mock2(), true, 0l), false, null);

        assertThat(disk.allKeys().size(), is(2));

        testSubscriber = new TestSubscriber<>();
        deleteRecordMatchingClassNameUT.with(Arrays.<Class>asList(Mock1.class, Mock2.class)).react().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        assertThat(disk.allKeys().size(), is(0));
    }

    @Test public void When_Class_Matches_Delete_Record_1_List() {
        disk.saveRecord(Mock1.KEY, new Record(Arrays.asList(new Mock1()), true, 0l), false, null);
        disk.saveRecord(Mock2.KEY,  new Record(Arrays.asList(new Mock2()), true, 0l), false, null);

        assertThat(disk.allKeys().size(), is(2));

        testSubscriber = new TestSubscriber<>();
        deleteRecordMatchingClassNameUT.with(Arrays.<Class>asList(Mock1.class)).react().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        assertThat(disk.allKeys().size(), is(1));
    }

    @Test public void When_Class_Matches_Delete_Record_2_List() {
        disk.saveRecord(Mock1.KEY, new Record(Arrays.asList(new Mock1()), true, 0l), false, null);
        disk.saveRecord(Mock2.KEY,  new Record(Arrays.asList(new Mock2()), true, 0l), false, null);

        assertThat(disk.allKeys().size(), is(2));

        testSubscriber = new TestSubscriber<>();
        deleteRecordMatchingClassNameUT.with(Arrays.<Class>asList(Mock1.class, Mock2.class)).react().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        assertThat(disk.allKeys().size(), is(0));
    }

    private class Mock1 {
        private static final String KEY = "Mock1";
    }

    private class Mock2 {
        private static final String KEY = "Mock2";
    }
}
