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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Encrypt/Decrypt the file data
 */
public final class BuiltInEncryptor implements Encryptor {
  private static final int KEY_LENGTH = 128;
      // Max 128 bits by default. See http://stackoverflow.com/a/24907555/5502014
  private static final int FILE_BUF = 1024;
  private Cipher encryptCipher;
  private Cipher decryptCipher;

  @Override public void encrypt(String key, File decryptedFile, File encryptedFile) {
    initCiphers(key);

    try {
      CipherInputStream cis =
          new CipherInputStream(new FileInputStream(decryptedFile), encryptCipher);
      write(cis, new FileOutputStream(encryptedFile));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override public void decrypt(String key, File encryptedFile, File decryptedFile) {
    initCiphers(key);

    try {
      CipherOutputStream cos =
          new CipherOutputStream(new FileOutputStream(decryptedFile), decryptCipher);
      write(new FileInputStream(encryptedFile), cos);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void initCiphers(String key) {
    try {
      SecretKeySpec secretKey = generateSecretKey(key);

      encryptCipher = Cipher.getInstance("AES");
      encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);

      decryptCipher = Cipher.getInstance("AES");
      decryptCipher.init(Cipher.DECRYPT_MODE, secretKey);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private SecretKeySpec generateSecretKey(String key) throws Exception {
    SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
    secureRandom.setSeed(key.getBytes("UTF-8"));
    KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
    keyGenerator.init(KEY_LENGTH, secureRandom);
    SecretKey secretKey = keyGenerator.generateKey();

    return new SecretKeySpec(secretKey.getEncoded(), "AES");
  }

  private void write(InputStream is, OutputStream os) {
    byte[] bytes = new byte[FILE_BUF];
    int numBytes;

    try {
      while ((numBytes = is.read(bytes)) != -1) {
        os.write(bytes, 0, numBytes);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        is.close();
        os.flush();
        os.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }
}
