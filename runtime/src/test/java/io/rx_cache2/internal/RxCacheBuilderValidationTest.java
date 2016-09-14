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

package io.rx_cache2.internal;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.security.InvalidParameterException;

/**
 * Created by daemontus on 02/09/16.
 */
public class RxCacheBuilderValidationTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test(expected = InvalidParameterException.class)
  public void Cache_Directory_Null() {
    new RxCache.Builder()
        .persistence(null, io.rx_cache2.internal.Jolyglot$.newInstance());
  }

  @Test(expected = InvalidParameterException.class)
  public void Jolyglot_Null() {
    new RxCache.Builder()
        .persistence(temporaryFolder.getRoot(), null);
  }

  @Test(expected = InvalidParameterException.class)
  public void Cache_Directory_Not_Exist() {
    File cacheDir = new File(temporaryFolder.getRoot(), "non_existent_folder");
    new RxCache.Builder()
        .persistence(cacheDir, io.rx_cache2.internal.Jolyglot$.newInstance());
  }

  @Test(expected = InvalidParameterException.class)
  public void Cache_Directory_Not_Writable() {
    File cacheDir = new File(temporaryFolder.getRoot(), "non_existent_folder");
    if (!cacheDir.mkdirs()) {
      throw new IllegalStateException("Cannot create temporary directory");
    }
    if (!cacheDir.setWritable(false, false)) {
      throw new IllegalStateException("Cannot modify permissions");
    }
    new RxCache.Builder()
        .persistence(cacheDir, io.rx_cache2.internal.Jolyglot$.newInstance());
  }
}
