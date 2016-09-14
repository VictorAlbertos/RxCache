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

package io.rx_cache2.internal.encrypt;

import java.io.File;

/**
 * Interface to Encrypt/Decrypt the file data
 */
public interface Encryptor {

  /**
   * Encrypts the {@code decryptedFile} data and saves it into {@code encryptedFile}
   *
   * @param key Key used by the algorithm to encrypt/decrypt the data
   * @param decryptedFile Input file with the data to encrypt
   * @param encryptedFile Output file with the encrypted data
   */
  void encrypt(String key, File decryptedFile, File encryptedFile);

  /**
   * Decrypts the {@code encryptedFile} data and saves it into {@code decryptedFile}
   *
   * @param key Key used by the algorithm to encrypt/decrypt the data
   * @param encryptedFile Input file with the encrypted data
   * @param decryptedFile Output file with the data without encrypt
   */
  void decrypt(String key, File encryptedFile, File decryptedFile);
}
