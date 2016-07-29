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

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import io.rx_cache.MigrationCache;
import io.rx_cache.internal.Mock;
import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GetPendingMigrationsTest {
    private GetPendingMigrations getPendingMigrationsUT;
    private TestSubscriber<List<MigrationCache>> testSubscriber;

    @Before public void setUp() {
        testSubscriber = new TestSubscriber<>();
    }

    @Test public void When_No_Scheme_Migration_Supplied_Then_Retrieve_Empty() {
        getPendingMigrationsUT = new GetPendingMigrations();
        getPendingMigrationsUT.react().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        List<MigrationCache> migrations = testSubscriber.getOnNextEvents().get(0);
        assertThat(migrations.size(), is(0));
    }

    @Test public void When_Migrations_Supplied_Then_Retrieve_Them() {
        getPendingMigrationsUT = new GetPendingMigrations();
        getPendingMigrationsUT.with(0, migrations()).react().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        List<MigrationCache> migrations = testSubscriber.getOnNextEvents().get(0);
        assertThat(migrations.size(), is(1));
    }

    @Test public void When_Migrations_Supplied_Are_Sorted_Then_Retrieve_Them_Sorted_By_Version() {
        getPendingMigrationsUT = new GetPendingMigrations();
        getPendingMigrationsUT.with(0, migrationsSorted()).react().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        List<MigrationCache> migrations = testSubscriber.getOnNextEvents().get(0);
        assertThat(migrations.get(0).version(), is(1));
        assertThat(migrations.get(1).version(), is(2));
        assertThat(migrations.get(2).version(), is(3));
        assertThat(migrations.get(3).version(), is(4));
    }

    @Test public void When_Migrations_Supplied_Are_Not_Sorted_Then_Retrieve_Them_Sorted_By_Version() {
        getPendingMigrationsUT = new GetPendingMigrations();
        getPendingMigrationsUT.with(0, migrationsNoSorted()).react().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        List<MigrationCache> migrations = testSubscriber.getOnNextEvents().get(0);
        assertThat(migrations.get(0).version(), is(1));
        assertThat(migrations.get(1).version(), is(2));
        assertThat(migrations.get(2).version(), is(3));
        assertThat(migrations.get(3).version(), is(4));
    }


    @Test public void When_Migrations_Supplied_And_Version_Cache_Then_Get_Only_Pending_Migrations() {
        getPendingMigrationsUT = new GetPendingMigrations();
        getPendingMigrationsUT.with(2, migrationsSorted()).react().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        List<MigrationCache> migrations = testSubscriber.getOnNextEvents().get(0);
        assertThat(migrations.get(0).version(), is(3));
        assertThat(migrations.get(1).version(), is(4));

        testSubscriber = new TestSubscriber<>();
        getPendingMigrationsUT = new GetPendingMigrations();
        getPendingMigrationsUT.with(0, migrationsSorted()).react().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        migrations = testSubscriber.getOnNextEvents().get(0);
        assertThat(migrations.get(0).version(), is(1));
        assertThat(migrations.get(1).version(), is(2));
        assertThat(migrations.get(2).version(), is(3));
        assertThat(migrations.get(3).version(), is(4));

        testSubscriber = new TestSubscriber<>();
        getPendingMigrationsUT = new GetPendingMigrations();
        getPendingMigrationsUT.with(4, migrationsSorted()).react().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        migrations = testSubscriber.getOnNextEvents().get(0);
        assertThat(migrations.size(), is(0));
    }

    private List<MigrationCache> migrations() {
        return Arrays.asList(
            new MigrationCache(1, new Class[] {Mock.class})
        );
    }

    private List<MigrationCache> migrationsSorted() {
        return Arrays.asList(
            new MigrationCache(1, new Class[] {Mock.class}),
            new MigrationCache(2, new Class[] {Mock.class}),
            new MigrationCache(3, new Class[] {Mock.class}),
            new MigrationCache(4, new Class[] {Mock.class})
        );
    }

    private List<MigrationCache> migrationsNoSorted() {
        return Arrays.asList(
            new MigrationCache(4, new Class[] {Mock.class}),
            new MigrationCache(2, new Class[] {Mock.class}),
            new MigrationCache(1, new Class[] {Mock.class}),
            new MigrationCache(3, new Class[] {Mock.class})
        );
    }
}
