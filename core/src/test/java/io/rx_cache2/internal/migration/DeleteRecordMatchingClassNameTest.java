package io.rx_cache2.internal.migration;

import io.rx_cache2.internal.Record;
import io.rx_cache2.internal.common.BaseTest;
import java.util.Arrays;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class DeleteRecordMatchingClassNameTest extends BaseTest {
    private io.rx_cache2.internal.migration.DeleteRecordMatchingClassName
        deleteRecordMatchingClassNameUT;

    @Override public void setUp() {
        super.setUp();
        deleteRecordMatchingClassNameUT = new io.rx_cache2.internal.migration.DeleteRecordMatchingClassName(disk, null);
    }

    @Test public void When_Class_Matches_Delete_Record_1() {
        disk.saveRecord(Mock1.KEY, new Record(new Mock1(), true, 0l), false, null);
        disk.saveRecord(Mock2.KEY,  new Record(new Mock2(), true, 0l), false, null);

        assertThat(disk.allKeys().size(), is(2));

        deleteRecordMatchingClassNameUT.with(Arrays.<Class>asList(Mock1.class))
            .react()
            .test()
            .awaitTerminalEvent();

        assertThat(disk.allKeys().size(), is(1));
    }

    @Test public void When_Class_Matches_Delete_Record_2() {
        disk.saveRecord(Mock1.KEY, new Record(new Mock1(), true, 0l), false, null);
        disk.saveRecord(Mock2.KEY,  new Record(new Mock2(), true, 0l), false, null);

        assertThat(disk.allKeys().size(), is(2));

        deleteRecordMatchingClassNameUT.with(Arrays.<Class>asList(Mock1.class, Mock2.class))
            .react()
            .test()
            .awaitTerminalEvent();
        assertThat(disk.allKeys().size(), is(0));
    }

    @Test public void When_Class_Matches_Delete_Record_1_List() {
        disk.saveRecord(Mock1.KEY, new Record(Arrays.asList(new Mock1()), true, 0l), false, null);
        disk.saveRecord(Mock2.KEY,  new Record(Arrays.asList(new Mock2()), true, 0l), false, null);

        assertThat(disk.allKeys().size(), is(2));

        deleteRecordMatchingClassNameUT.with(Arrays.<Class>asList(Mock1.class))
            .react()
            .test()
            .awaitTerminalEvent();

        assertThat(disk.allKeys().size(), is(1));
    }

    @Test public void When_Class_Matches_Delete_Record_2_List() {
        disk.saveRecord(Mock1.KEY, new Record(Arrays.asList(new Mock1()), true, 0l), false, null);
        disk.saveRecord(Mock2.KEY,  new Record(Arrays.asList(new Mock2()), true, 0l), false, null);

        assertThat(disk.allKeys().size(), is(2));

        deleteRecordMatchingClassNameUT.with(Arrays.<Class>asList(Mock1.class, Mock2.class))
            .react()
            .test()
            .awaitTerminalEvent();

        assertThat(disk.allKeys().size(), is(0));
    }

    public static class Mock1 {
        private static final String KEY = "Mock1";
        private final String s1;

        public Mock1() {
            s1 = null;
        }

        public Mock1(String s1) {
            this.s1 = s1;
        }
    }

    public static class Mock2 {
        private static final String KEY = "Mock2";
        private final String s1;

        public Mock2() {
            s1 = null;
        }

        public String getS1() {
            return s1;
        }
    }
}
