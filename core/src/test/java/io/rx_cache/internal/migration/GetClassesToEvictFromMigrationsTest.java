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

package io.rx_cache.internal.migration;


import io.rx_cache.MigrationCache;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import rx.observers.TestSubscriber;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class GetClassesToEvictFromMigrationsTest {
    private GetClassesToEvictFromMigrations getClassesToEvictFromMigrationsUT;
    private TestSubscriber<List<Class>> testSubscriber;

    @Before public void setUp() {
        getClassesToEvictFromMigrationsUT = new GetClassesToEvictFromMigrations();
        testSubscriber = new TestSubscriber<>();
    }

    @Test public void When_Migration_Contain_One_Class_To_Evict_Get_It() {
        List<MigrationCache> migrations = oneMigration();

        getClassesToEvictFromMigrationsUT.with(migrations).react().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        List<Class> classes = testSubscriber.getOnNextEvents().get(0);
        assertThat(classes.size(), is(1));
    }

    @Test public void When_Migrations_Contains_Classes_To_Evict_Get_Them() {
        List<MigrationCache> migrations = migrations();

        getClassesToEvictFromMigrationsUT.with(migrations).react().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        List<Class> classes = testSubscriber.getOnNextEvents().get(0);
        assertThat(classes.size(), is(2));
    }

    @Test public void When_Several_Classes_To_Evict_With_Same_Type_Only_Keep_One() {
        List<MigrationCache> migrations = migrationsRepeated();

        getClassesToEvictFromMigrationsUT.with(migrations).react().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        List<Class> classes = testSubscriber.getOnNextEvents().get(0);
        assertThat(classes.size(), is(3));
    }

    private List<MigrationCache> oneMigration() {
        return Arrays.asList(new MigrationCache(1, new Class[] {Mock1.class}));
    }

    private List<MigrationCache> migrations() {
        return Arrays.asList(
            new MigrationCache(1, new Class[] {Mock1.class}),
            new MigrationCache(2, new Class[] {Mock2.class})
        );
    }

    private List<MigrationCache> migrationsRepeated() {
        return Arrays.asList(
            new MigrationCache(1, new Class[] {Mock1.class}),
            new MigrationCache(2, new Class[] {Mock2.class}),
            new MigrationCache(3, new Class[] {Mock1.class}),
            new MigrationCache(4, new Class[] {Mock2.class}),
            new MigrationCache(5, new Class[] {Mock3.class})
        );
    }
    private class Mock1 {}
    private class Mock2 {}
    private class Mock3 {}
}
