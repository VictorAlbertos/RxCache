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
import io.rx_cache.internal.common.BaseTest;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
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
        upgradeCacheVersionUT.with(migrations()).react().subscribe(upgradeTestSubscriber);
        upgradeTestSubscriber.awaitTerminalEvent();
        upgradeTestSubscriber.assertNoErrors();
        upgradeTestSubscriber.assertCompleted();

        getCacheVersion.react().subscribe(versionTestSubscriber);
        versionTestSubscriber.awaitTerminalEvent();
        int currentVersion = versionTestSubscriber.getOnNextEvents().get(0);

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
