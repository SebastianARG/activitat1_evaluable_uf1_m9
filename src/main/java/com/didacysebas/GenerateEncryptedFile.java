package com.didacysebas;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Base64;

public class GenerateEncryptedFile {

    public static void main(String[] args) throws Exception {
        // Datos para el cifrado
        String plaintext = "hola mundo"; // Mensaje a cifrar
        String password = "ua!?";        // Contraseña

        // Derivar clave de 128 bits (16 bytes) usando SHA3-256
        SecretKey key = deriveKey(password);

        // Generar un IV aleatorio de 16 bytes
        byte[] iv = new byte[16];
        new java.security.SecureRandom().nextBytes(iv);

        // Cifrar el mensaje
        String ciphertext = encrypt(plaintext, key, iv);

        // Codificar IV en Base64
        String ivBase64 = Base64.getEncoder().encodeToString(iv);

        // Guardar en archivo con formato IV en la primera línea, mensaje cifrado en la segunda
        Path outputPath = Path.of(System.getProperty("user.home") + "/Desktop/texto_cifrado.txt");
        Files.writeString(outputPath, ivBase64 + "\n" + ciphertext);

        System.out.println("Archivo cifrado generado correctamente en: " + outputPath.toAbsolutePath());
    }

    private static SecretKey deriveKey(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA3-256");
        byte[] hash = digest.digest(password.getBytes("UTF-8"));
        return new SecretKeySpec(hash, 0, 16, "AES");
    }

    private static String encrypt(String plaintext, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
}
