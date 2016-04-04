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

import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import io.rx_cache.Migration;
import io.rx_cache.SchemeMigration;
import io.rx_cache.internal.common.BaseTest;
import rx.observers.TestSubscriber;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class UpgradeCacheVersionTest extends BaseTest {
    private UpgradeCacheVersion upgradeCacheVersionUT;
    private GetCacheVersion getCacheVersion;
    private TestSubscriber<Void> upgradeTestSubscriber;
    private TestSubscriber<Integer> versionTestSubscriber;

    @Override public void setUp() {
        super.setUp();
        upgradeCacheVersionUT = new UpgradeCacheVersion(disk);
        getCacheVersion = new GetCacheVersion(disk);

        upgradeTestSubscriber = new TestSubscriber<>();
        versionTestSubscriber = new TestSubscriber<>();
    }

    @Test public void When_Upgrade_Version_Upgrade_It() {
        Annotation annotation = Migrations.class.getAnnotation(SchemeMigration.class);
        SchemeMigration schemeMigration = (SchemeMigration) annotation;
        List<Migration> migrations = Arrays.asList(schemeMigration.value());

        upgradeCacheVersionUT.with(migrations).react().subscribe(upgradeTestSubscriber);
        upgradeTestSubscriber.awaitTerminalEvent();
        upgradeTestSubscriber.assertNoValues();
        upgradeTestSubscriber.assertNoErrors();
        upgradeTestSubscriber.assertCompleted();

        getCacheVersion.react().subscribe(versionTestSubscriber);
        versionTestSubscriber.awaitTerminalEvent();
        int currentVersion = versionTestSubscriber.getOnNextEvents().get(0);

        assertThat(currentVersion, is(5));
    }


    @SchemeMigration({
            @Migration(version = 1, evictClasses = {Mock1.class}),
            @Migration(version = 3, evictClasses = {Mock1.class}),
            @Migration(version = 4, evictClasses = {Mock1.class}),
            @Migration(version = 5, evictClasses = {Mock1.class})
    })
    private interface Migrations {}

    private class Mock1 {

    }
}
