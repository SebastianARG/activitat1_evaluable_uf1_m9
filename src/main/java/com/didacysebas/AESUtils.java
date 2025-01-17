package com.didacysebas;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;

public class AESUtils {

    public static SecretKey deriveKey(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA3-256");
        byte[] keyBytes = digest.digest(password.getBytes("UTF-8"));
        // Ens quedem amb els primers 16 bytes (128 bits)
        return new SecretKeySpec(keyBytes, 0, 16, "AES");
    }

    public static String encrypt(String plaintext, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(ciphertext);
    }



    public static String decrypt(String base64Ciphertext, SecretKey key, byte[] iv) throws Exception {

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(base64Ciphertext));

        return new String(decryptedBytes, "UTF-8");
    }

}
