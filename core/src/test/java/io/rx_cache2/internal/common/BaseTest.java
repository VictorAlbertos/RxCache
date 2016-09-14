/*
 * Copyright 2016 Victor Albertos
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

package io.rx_cache2.internal.common;

import io.rx_cache2.internal.Jolyglot$;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import io.rx_cache2.internal.Disk;
import io.rx_cache2.internal.encrypt.BuiltInEncryptor;
import io.rx_cache2.internal.encrypt.FileEncryptor;


public class BaseTest {
    protected Disk disk;
    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before public void setUp() {
        disk = new Disk(temporaryFolder.getRoot(),
                new FileEncryptor(new BuiltInEncryptor()), Jolyglot$.newInstance());
    }

    protected void waitTime(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
