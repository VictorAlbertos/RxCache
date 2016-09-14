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

package io.rx_cache2.internal.migration;

import io.reactivex.observers.TestObserver;
import io.rx_cache2.MigrationCache;
import io.rx_cache2.internal.common.BaseTest;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class UpgradeCacheVersionTest extends BaseTest {
    private io.rx_cache2.internal.migration.UpgradeCacheVersion upgradeCacheVersionUT;
    private io.rx_cache2.internal.migration.GetCacheVersion getCacheVersion;
    private TestObserver<Integer> upgradeTestObserver;
    private TestObserver<Integer> versionTestObserver;

    @Override public void setUp() {
        super.setUp();
        upgradeCacheVersionUT = new io.rx_cache2.internal.migration.UpgradeCacheVersion(disk);
        getCacheVersion = new io.rx_cache2.internal.migration.GetCacheVersion(disk);

        upgradeTestObserver = new TestObserver<>();
        versionTestObserver = new TestObserver<>();
    }

    @Test public void When_Upgrade_Version_Upgrade_It() {
        upgradeCacheVersionUT.with(migrations()).react().subscribe(upgradeTestObserver);
        upgradeTestObserver.awaitTerminalEvent();
        upgradeTestObserver.assertNoErrors();
        upgradeTestObserver.assertComplete();

        getCacheVersion.react().subscribe(versionTestObserver);
        versionTestObserver.awaitTerminalEvent();
        int currentVersion = versionTestObserver.values().get(0);

        assertThat(currentVersion, is(5));
    }

    private List<MigrationCache> migrations() {
        return Arrays.asList(
            new MigrationCache(1, new Class[] {Mock1.class}),
            new MigrationCache(2, new Class[] {Mock1.class}),
            new MigrationCache(3, new Class[] {Mock1.class}),
            new MigrationCache(4, new Class[] {Mock1.class}),
            new MigrationCache(5, new Class[] {Mock1.class})
        );
    }

    private static class Mock1 {}
}
