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

package io.rx_cache2.internal.encrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import javax.inject.Inject;

public final class FileEncryptor {
  private static final String SUFFIX_TMP = "-tmp";
  private final Encryptor encryptor;

  @Inject public FileEncryptor(Encryptor encryptor) {
    this.encryptor = encryptor;
  }

  public File encrypt(String key, File file) {
    if (!file.exists()) return file;

    String filenameInput = file.getAbsolutePath();
    file = rename(file, new File(filenameInput + SUFFIX_TMP));
    File fileOutput = new File(filenameInput);

    encryptor.encrypt(key, file, fileOutput);
    file.delete();

    return fileOutput;
  }

  public File decrypt(String key, File file) {
    if (!file.exists()) return file;

    String filenameInput = file.getAbsolutePath();
    File fileOutput = new File(filenameInput + SUFFIX_TMP);
    encryptor.decrypt(key, file, fileOutput);

    return fileOutput;
  }

  private File rename(File fileSrc, File fileDst) {
    FileChannel inputChannel = null;
    FileChannel outputChannel = null;

    try {
      inputChannel = new FileInputStream(fileSrc).getChannel();
      outputChannel = new FileOutputStream(fileDst).getChannel();

      inputChannel.transferTo(0, inputChannel.size(), outputChannel);
      fileSrc.delete();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (inputChannel != null) {
          inputChannel.close();
        }
        if (outputChannel != null) {
          outputChannel.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return fileDst;
  }
}
