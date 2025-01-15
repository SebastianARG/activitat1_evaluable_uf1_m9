package com.didacysebas;



import picocli.CommandLine;

import javax.crypto.SecretKey;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;

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

    @Override
    public void run() {
        try {
            // 1. Llegir el fitxer i separar IV i ciphertext
            String base64Content = Files.readString(inputFile.toPath());
            byte[] decoded = Base64.getDecoder().decode(base64Content);

            byte[] iv = new byte[16];
            System.arraycopy(decoded, 0, iv, 0, 16);

            byte[] ciphertextBytes = new byte[decoded.length - 16];
            System.arraycopy(decoded, 16, ciphertextBytes, 0, decoded.length - 16);

            String ciphertextBase64 = Base64.getEncoder().encodeToString(ciphertextBytes);

            // 2. Carregar paraules de pas conegudes
            List<String> knownPasswords = loadKnownPasswords(passwordFile);

            // 3. Intentar cadascuna de les contrasenyes conegudes
            String decryptedMessage = null;
            String validPassword = null;
            for (String pwd : knownPasswords) {
                try {
                    SecretKey key = AESUtils.deriveKey(pwd);
                    String candidatePlain = AESUtils.decrypt(ciphertextBase64, key, iv);
                    if (isPlausibleText(candidatePlain)) {
                        // Opcionalment, preguntar si realment és correcta
                        System.out.println("Text desxifrat:\n" + candidatePlain);
                        System.out.print("Confirmes que és correcte? (y/n): ");
                        String resp = new java.util.Scanner(System.in).nextLine().trim();
                        if ("y".equalsIgnoreCase(resp)) {
                            decryptedMessage = candidatePlain;
                            validPassword = pwd;
                            break;
                        }
                    }
                } catch (Exception ignored) {
                    // No era la contrasenya
                }
            }

            // 4. Si no hem trobat contrasenya, força bruta
            if (decryptedMessage == null) {
                // Exemple simplificat (veure el mètode bruteForcePassword més amunt)
                validPassword = bruteForcePassword(iv, ciphertextBase64);
                if (validPassword != null) {
                    // Tornem a desencriptar per obtenir el text final
                    SecretKey key = AESUtils.deriveKey(validPassword);
                    decryptedMessage = AESUtils.decrypt(ciphertextBase64, key, iv);
                }
            }

            if (decryptedMessage == null) {
                System.err.println("No s'ha pogut desxifrar el missatge amb cap contrasenya!");
                return;
            }

            // 5. Si hem trobat una contrasenya nova, guardar-la
            if (!knownPasswords.contains(validPassword)) {
                PasswordManager.savePassword(passwordFile, validPassword);
            }

            // 6. Guardar o mostrar el text desxifrat
            if (outputFile != null) {
                Files.writeString(outputFile.toPath(), decryptedMessage, StandardOpenOption.CREATE);
                System.out.println("Missatge desxifrat guardat a: " + outputFile.getAbsolutePath());
            } else {
                System.out.println("Missatge desxifrat:\n" + decryptedMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> loadKnownPasswords(File pwdFile) {
        try {
            if (!pwdFile.exists()) {
                return new ArrayList<>();
            }
            return PasswordManager.loadPasswords(pwdFile);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private boolean isPlausibleText(String text) {
        long count = text.chars()
                .filter(c -> Character.isLetter(c) || Character.isSpaceChar(c))
                .count();
        double ratio = (double) count / text.length();
        return ratio > 0.8;
    }

    private String bruteForcePassword(byte[] iv, String ciphertextBase64) {
        // Alfabet i generació (veure exemple a dalt)
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
                System.out.print("És correcte? (y/n): ");
                String resp = new java.util.Scanner(System.in).nextLine().trim();
                if ("y".equalsIgnoreCase(resp)) {
                    // guardar password
                    PasswordManager.savePassword(passwordFile, password);
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
