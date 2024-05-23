package com.example.signingoogle2.Config;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class AESEncryptionUtil {
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16; // Độ dài của Initialization Vector (IV)

    public static SecretKey generateRandomAESKey() throws NoSuchAlgorithmException {
        // Tạo một đối tượng KeyGenerator cho AES
        KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);

        // Khai báo độ dài khóa (128, 192 hoặc 256 bit)
        int keyLength = 128; // Độ dài mặc định là 128 bit (16 byte)

        // Thiết lập độ dài khóa
        keyGenerator.init(keyLength);

        // Tạo khóa ngẫu nhiên
        SecretKey secretKey = keyGenerator.generateKey();

        return secretKey;
    }

    public static byte[] encrypt(byte[] data, SecretKey aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);

        // Tạo IV ngẫu nhiên
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        cipher.init(Cipher.ENCRYPT_MODE, aesKey,new IvParameterSpec(iv));

        byte[] encryptedData = cipher.doFinal(data);

        // Kết hợp IV và dữ liệu mã hóa thành một mảng duy nhất
        byte[] combined = new byte[IV_LENGTH + encryptedData.length];
        System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
        System.arraycopy(encryptedData, 0, combined, IV_LENGTH, encryptedData.length);

        return combined;
    }
    public static byte[] decrypt(byte[] encryptedData, SecretKey keyAES) throws GeneralSecurityException {
        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(encryptedData, 0, iv, 0, IV_LENGTH);

        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, keyAES, new IvParameterSpec(iv));
        byte[] decryptedData = cipher.doFinal(encryptedData, IV_LENGTH, encryptedData.length - IV_LENGTH);

        return decryptedData;
    }
}

