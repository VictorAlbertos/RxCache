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

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import io.rx_cache.Migration;
import io.rx_cache.SchemeMigration;
import io.rx_cache.internal.Mock;
import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GetPendingMigrationsTest {
    private GetPendingMigrations getPendingMigrationsUT;
    private TestSubscriber<List<Migration>> testSubscriber;

    @Before public void setUp() {
        testSubscriber = new TestSubscriber<>();
    }

    @Test public void When_No_Scheme_Migration_Supplied_Then_Retrieve_Empty() {
        getPendingMigrationsUT = new GetPendingMigrations(NoSchemeMigration.class);
        getPendingMigrationsUT.react().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        List<Migration> migrations = testSubscriber.getOnNextEvents().get(0);
        assertThat(migrations.size(), is(0));
    }

    @Test public void When_Scheme_Migration_With_No_Migrations_Supplied_Then_Retrieve_Empty() {
        getPendingMigrationsUT = new GetPendingMigrations(SchemeMigrationWithNoMigrations.class);
        getPendingMigrationsUT.react().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        List<Migration> migrations = testSubscriber.getOnNextEvents().get(0);
        assertThat(migrations.size(), is(0));
    }

    @Test public void When_Migrations_Supplied_Then_Retrieve_Them() {
        getPendingMigrationsUT = new GetPendingMigrations(Migrations.class);
        getPendingMigrationsUT.react().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        List<Migration> migrations = testSubscriber.getOnNextEvents().get(0);
        assertThat(migrations.size(), is(1));
    }

    @Test public void When_Migrations_Supplied_Are_Sorted_Then_Retrieve_Them_Sorted_By_Version() {
        getPendingMigrationsUT = new GetPendingMigrations(MigrationsSorted.class);
        getPendingMigrationsUT.react().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        List<Migration> migrations = testSubscriber.getOnNextEvents().get(0);
        assertThat(migrations.get(0).version(), is(1));
        assertThat(migrations.get(1).version(), is(2));
        assertThat(migrations.get(2).version(), is(3));
        assertThat(migrations.get(3).version(), is(4));
    }

    @Test public void When_Migrations_Supplied_Are_Not_Sorted_Then_Retrieve_Them_Sorted_By_Version() {
        getPendingMigrationsUT = new GetPendingMigrations(MigrationsNoSorted.class);
        getPendingMigrationsUT.react().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        List<Migration> migrations = testSubscriber.getOnNextEvents().get(0);
        assertThat(migrations.get(0).version(), is(1));
        assertThat(migrations.get(1).version(), is(2));
        assertThat(migrations.get(2).version(), is(3));
        assertThat(migrations.get(3).version(), is(4));
    }


    @Test public void When_Migrations_Supplied_And_Version_Cache_Then_Get_Only_Pending_Migrations() {
        getPendingMigrationsUT = new GetPendingMigrations(MigrationsSorted.class);
        getPendingMigrationsUT.with(2).react().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        List<Migration> migrations = testSubscriber.getOnNextEvents().get(0);
        assertThat(migrations.get(0).version(), is(3));
        assertThat(migrations.get(1).version(), is(4));

        testSubscriber = new TestSubscriber<>();
        getPendingMigrationsUT = new GetPendingMigrations(MigrationsSorted.class);
        getPendingMigrationsUT.with(0).react().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        migrations = testSubscriber.getOnNextEvents().get(0);
        assertThat(migrations.get(0).version(), is(1));
        assertThat(migrations.get(1).version(), is(2));
        assertThat(migrations.get(2).version(), is(3));
        assertThat(migrations.get(3).version(), is(4));

        testSubscriber = new TestSubscriber<>();
        getPendingMigrationsUT = new GetPendingMigrations(MigrationsSorted.class);
        getPendingMigrationsUT.with(4).react().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        migrations = testSubscriber.getOnNextEvents().get(0);
        assertThat(migrations.size(), is(0));
    }

    private class NoSchemeMigration {}

    @SchemeMigration(value = {})
    private class SchemeMigrationWithNoMigrations {}

    @SchemeMigration(@Migration(version = 1, evictClasses = {Mock.class}))
    private class Migrations {}

    @SchemeMigration({
            @Migration(version = 1, evictClasses = {Mock.class}),
            @Migration(version = 2, evictClasses = {Mock.class}),
            @Migration(version = 3, evictClasses = {Mock.class}),
            @Migration(version = 4, evictClasses = {Mock.class})
    })
    private class MigrationsSorted {}


    @SchemeMigration({
            @Migration(version = 1, evictClasses = {Mock.class}),
            @Migration(version = 2, evictClasses = {Mock.class}),
            @Migration(version = 3, evictClasses = {Mock.class}),
            @Migration(version = 4, evictClasses = {Mock.class})
    })
    private interface MigrationsProviders {}


    @SchemeMigration({
            @Migration(version = 4, evictClasses = {Mock.class}),
            @Migration(version = 2, evictClasses = {Mock.class}),
            @Migration(version = 1, evictClasses = {Mock.class}),
            @Migration(version = 3, evictClasses = {Mock.class})
    })
    private class MigrationsNoSorted {}
}
