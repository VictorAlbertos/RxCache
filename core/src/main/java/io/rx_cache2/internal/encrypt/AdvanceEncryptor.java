package io.rx_cache2.internal.encrypt;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * https://stackoverflow.com/questions/13433529/android-4-2-broke-my-encrypt-decrypt-code-and-the-provided-solutions-dont-work#
 * <pre>
 * PackageName: io.rx_cache2.internal.encrypt
 * FileName: AdvanceEncryptor
 * Author: Yinyh
 * Date: 2019/9/25 10:34
 * </pre>
 */
public class AdvanceEncryptor implements Encryptor {

    private static final int ITERATION_COUNT = 100;
    private static final int KEY_LENGTH = 256;
    private static final String PBKDF2_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int PKCS5_SALT_LENGTH = 32;
    private static final String DELIMITER = "]";
    private static final int FILE_BUF = 1024;
    private static final SecureRandom random = new SecureRandom();

    @Override
    public void encrypt(String key, File decryptedFile, File encryptedFile) {
        byte[] salt = generateSalt();
        SecretKey secretKey = deriveKey(key, salt);
        if (null == secretKey) return;
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            byte[] iv = generateIv(cipher.getBlockSize());
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParams);
            CipherInputStream cis =
                    new CipherInputStream(new FileInputStream(decryptedFile), cipher);
            writeToEncrypt(cis, new FileOutputStream(encryptedFile), salt, iv);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void decrypt(String key, File encryptedFile, File decryptedFile) {
        try {
            String prefix = readKeyFromEncrypt(new FileInputStream(encryptedFile));
            if (null == prefix) return;
            String[] fields = prefix.split(DELIMITER);
            if (fields.length < 2) return;
            SecretKey secretKey = deriveKey(key, fromBase64(fields[0]));
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            IvParameterSpec ivParams = new IvParameterSpec(fromBase64(fields[1]));
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams);
            CipherOutputStream cos =
                    new CipherOutputStream(new FileOutputStream(decryptedFile), cipher);
            long skipByte = (prefix + "\r\n").getBytes(Charset.forName("UTF-8")).length;
            writeToDecrypt(new FileInputStream(encryptedFile), cos, skipByte);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeToEncrypt(InputStream origin, OutputStream encrypt, byte[] salt, byte[] iv) {
        String prefix = toBase64(salt) + DELIMITER + toBase64(iv) + "\r\n";
        byte[] buffer = new byte[FILE_BUF];
        int numBytes;
        try {
            encrypt.write(prefix.getBytes(Charset.forName("UTF-8")));
            while ((numBytes = origin.read(buffer, 0, buffer.length)) != -1) {
                encrypt.write(buffer, 0, numBytes);
            }
            encrypt.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(origin);
            close(encrypt);
        }
    }

    private String readKeyFromEncrypt(InputStream encrypt) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(encrypt));
        String key = null;
        try {
            key = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(encrypt);
        }
        return key;
    }

    private void writeToDecrypt(InputStream encrypt, OutputStream decrypt, long skip) {
        byte[] buffer = new byte[FILE_BUF];
        int numBytes;
        try {
            encrypt.skip(skip);
            while (-1 != (numBytes = encrypt.read(buffer, 0, buffer.length))) {
                decrypt.write(buffer, 0, numBytes);
            }
            decrypt.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(encrypt);
            close(decrypt);
        }
    }

    private void close(Closeable closeable) {
        try {
            if (null != closeable) {
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] generateSalt() {
        byte[] b = new byte[PKCS5_SALT_LENGTH];
        random.nextBytes(b);
        return b;
    }

    private static byte[] generateIv(int length) {
        byte[] b = new byte[length];
        random.nextBytes(b);
        return b;
    }

    private static SecretKey deriveKey(String password, byte[] salt) {
        try {
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PBKDF2_DERIVATION_ALGORITHM);
            byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String toBase64(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private static byte[] fromBase64(String base64) {
        return Base64.decode(base64, Base64.NO_WRAP);
    }
}
