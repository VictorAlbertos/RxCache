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

import io.rx_cache.internal.common.BaseTest;
import rx.observers.TestSubscriber;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class GetCacheVersionTest extends BaseTest {
    private GetCacheVersion getCacheVersionUT;
    private TestSubscriber<Integer> versionTestSubscriber;

    @Override public void setUp() {
        super.setUp();
        getCacheVersionUT = new GetCacheVersion(disk);
        versionTestSubscriber = new TestSubscriber<>();
    }

    @Test public void When_No_Version_Specified_Then_Return_0() {
        getCacheVersionUT.react().subscribe(versionTestSubscriber);
        versionTestSubscriber.awaitTerminalEvent();
        int currentVersion = versionTestSubscriber.getOnNextEvents().get(0);

        assertThat(currentVersion, is(0));
    }

    @Test public void When_Version_Specified_Then_Get_It() {
        disk.save(GetCacheVersion.KEY_CACHE_VERSION, 5, false, null);

        getCacheVersionUT.react().subscribe(versionTestSubscriber);
        versionTestSubscriber.awaitTerminalEvent();
        int currentVersion = versionTestSubscriber.getOnNextEvents().get(0);

        assertThat(currentVersion, is(5));
    }
}
