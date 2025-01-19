package com.didacysebas.commands;
import com.didacysebas.utilities.*;
import picocli.CommandLine;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;

/**
 * @author sebastian y dídac
 */
@CommandLine.Command(
        name = "decrypt",
        mixinStandardHelpOptions = true,
        description = "Desxifra un missatge utilitzant paraules de pas conegudes."
)
public class DecryptCommand implements Runnable {

    @CommandLine.Parameters(index = "0", description = "El fitxer d'entrada xifrat.")
    private File inputFile;

    @CommandLine.Option(names = "--out", description = "Fitxer de sortida per al missatge desxifrat.")
    private File outputFile;

    @CommandLine.Option(names = "--pwdfile", description = "Fitxer de paraules de pas conegudes.")
    private File passwordFile = new File("passwords.txt");

    private static boolean bruteForceMessagePrinted = false; // Variable para controlar el mensaje

    // Métodos setter para configurar las propiedades
    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public void setPasswordFile(File passwordFile) {
        this.passwordFile = passwordFile;
    }

    @Override
    public void run() {
        try {
            // Leer el IV y el texto cifrado
            byte[] iv;
            byte[] ciphertextBytes;

            List<String> lines = Files.readAllLines(inputFile.toPath(), StandardCharsets.UTF_8);
            if (lines.size() < 2) {
                throw new IllegalArgumentException("El archivo no contiene un IV y un texto cifrado válidos.");
            }
            iv = Base64.getDecoder().decode(lines.get(0).trim());
            ciphertextBytes = Base64.getDecoder().decode(lines.get(1).trim());
            String ciphertextBase64 = Base64.getEncoder().encodeToString(ciphertextBytes);

            // 2. Cargar contraseñas conocidas
            List<String> knownPasswords = PasswordManager.loadPasswords(passwordFile);

            // Verificar si el archivo de contraseñas está vacío
            if (knownPasswords.isEmpty() && !bruteForceMessagePrinted) {
                System.out.println("El archivo de contraseñas está vacío. Se procederá a fuerza bruta.");
                bruteForceMessagePrinted = true;
            }

            // 3. Intentar descifrar con cada contraseña
            String decryptedMessage = null;
            String validPassword = null;
            for (String pwd : knownPasswords) {
                try {
                    SecretKey key = AESUtils.deriveKey(pwd);
                    String candidatePlain = AESUtils.decrypt(ciphertextBase64, key, iv);
                    if (isPlausibleText(candidatePlain)) {
                        System.out.println("Text desxifrat:\n" + candidatePlain);
                        System.out.print("Confirmes que és correcte? (y/n): ");
                        String resp = new java.util.Scanner(System.in).nextLine().trim();
                        if ("y".equalsIgnoreCase(resp)) {
                            decryptedMessage = candidatePlain;
                            validPassword = pwd;
                            break;
                        }
                    }
                } catch (Exception e) {
                }
            }

            // 4. Fuerza bruta si no se encontró una contraseña válida
            if (decryptedMessage == null) {
                validPassword = bruteForcePassword(iv, ciphertextBase64);
                if (validPassword != null) {
                    SecretKey key = AESUtils.deriveKey(validPassword);
                    decryptedMessage = AESUtils.decrypt(ciphertextBase64, key, iv);
                }
            }

            if (decryptedMessage == null) {
                System.err.println("No s'ha pogut desxifrar el missatge amb cap contrasenya!");
                return;
            }

            // 5. Guardar nueva contraseña si es necesario
            if (!knownPasswords.contains(validPassword)) {
                PasswordManager.savePassword(passwordFile, validPassword);
            }

            // 6. Guardar o mostrar el mensaje descifrado
            if (outputFile != null) {
                Files.writeString(outputFile.toPath(), decryptedMessage);
                System.out.println("Missatge desxifrat guardat a: " + outputFile.getAbsolutePath());
            }

            // Almacenar la clave encontrada en CommandContext
            SecretKey keyFound = AESUtils.deriveKey(validPassword);
            CommandContext.setFoundKey(keyFound);

        } catch (Exception e) {
            System.err.println("Error al descifrar el mensaje: " + e.getMessage());
        }
    }

    private boolean isPlausibleText(String text) {
        if (text == null || text.isEmpty()) {
            return false; // No se puede considerar plausible un texto vacío o nulo
        }

        // Contar caracteres válidos: letras, números, espacios y ciertos signos
        long validCharCount = text.chars()
                .filter(c -> Character.isLetter(c) || Character.isDigit(c) || Character.isSpaceChar(c) ||
                        c == '_' || c == '?' || c == '%' || c == '!')
                .count();

        // Calcular el ratio de caracteres válidos sobre la longitud total del texto
        double ratio = (double) validCharCount / text.length();

        // Considerar plausible si más del 80% de los caracteres son válidos
        return ratio > 0.8;
    }

    private String bruteForcePassword(byte[] iv, String ciphertextBase64) {
        char[] alphabet = "abcdefghijklmnopqrstuvwxyz0123456789_?%!".toCharArray();
        for (char a : alphabet) {
            for (char b : alphabet) {
                for (char c : alphabet) {
                    for (char d : alphabet) {
                        String candidate = "" + a + b + c + d;
                        if (tryDecrypt(candidate, iv, ciphertextBase64)) {
                            return candidate;
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean tryDecrypt(String password, byte[] iv, String ciphertextBase64) {
        try {
            SecretKey key = AESUtils.deriveKey(password);
            String plain = AESUtils.decrypt(ciphertextBase64, key, iv);
            if (isPlausibleText(plain)) {
                System.out.println("Possiblement la contrasenya és: " + password);
                System.out.println("Text desxifrat:\n" + plain);
                if (!alreadySaved(password)) {
                    PasswordManager.savePassword(passwordFile, password);
                }

                return true; // Asume automáticamente que es correcto
            }
        } catch (Exception e) {
        }

        return false;
    }

    private boolean alreadySaved(String password) {
        try {
            List<String> existingPasswords = PasswordManager.loadPasswords(passwordFile);
            return existingPasswords.contains(password);
        } catch (IOException e) {
            System.out.printf("LOG: Error loading password file\n%s\n"+e.getMessage());
            return false;
        }
    }

    private void printBruteForceMessage() {
        if (!bruteForceMessagePrinted) {
            System.out.println("El archivo de contraseñas está vacío. Se procederá a fuerza bruta.");
            bruteForceMessagePrinted = true;
        }
    }
}