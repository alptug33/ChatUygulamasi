package com.alp2app.chatuygulamasi;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MessageEncryption {
    private static final String ALGORITHM = "AES";
    private static final String KEY = "1234567890123456";

    public static String encrypt(String message) throws  Exception {
        SecretKeySpec key = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8),ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE,key);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        return android.util.Base64.encodeToString(encryptedBytes,android.util.Base64.NO_WRAP);
    }
    public static String decrypt(String encrypted) throws  Exception {
        SecretKeySpec key = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8),ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE,key);
        byte [] decodedBytes = android.util.Base64.decode(encrypted,android.util.Base64.NO_WRAP);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);



    }
}
