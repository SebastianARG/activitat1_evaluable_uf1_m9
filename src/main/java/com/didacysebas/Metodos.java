package com.didacysebas;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;

public class Metodos {
    public static boolean createFile(String fileName){
        boolean creado = false;
        try {
            File file = new File(fileName);
            if (file.createNewFile()) {
                System.out.println("Archivo '" + fileName + "' creado exitosamente.");
                creado = true;
            } else {
                //Si existe el archivo se borra y se llama recursivamente para que lo cree de nuevo
                //file.delete();
                //createFile(fileName);
            }
        } catch (IOException e) {
            System.err.println("Error al crear el archivo: " + e.getMessage());
        }
        return creado;
    }
    //Xifrat de dades dins d’un vector usant AES en mode ECB
    public static byte[] encryptData(byte[] data, SecretKey skey){
        byte[] encryptedData = null;
        try {
            Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, skey);
            encryptedData = c.doFinal(data);
        } catch (Exception e) {
        }
        return encryptedData;
    }
    //Xifrat de dades dins d’un vector usant AES en mode ECB
    public static byte[] decryptData(byte[] data, SecretKey skey){
        byte[] decryptedData = null;
        try {
            Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, skey);
            decryptedData = c.doFinal(data);
        } catch (Exception e) {
        }
        return decryptedData;
    }


}
