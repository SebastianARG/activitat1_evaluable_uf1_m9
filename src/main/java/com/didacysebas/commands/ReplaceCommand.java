package com.didacysebas.commands;

import picocli.CommandLine;
import com.didacysebas.utilities.AESUtils;
import javax.crypto.SecretKey;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

@CommandLine.Command(
        name = "replace",
        mixinStandardHelpOptions = true,
        description = "Desxifra un missatge i permet generar-ne un de nou xifrat."
)
public class ReplaceCommand implements Runnable {

    @CommandLine.Parameters(index = "0", description = "El fitxer d'entrada xifrat.")
    private File inputFile;

    @CommandLine.Option(names = "--out", description = "Fitxer de sortida per al missatge re-xifrat.")
    private File outputFile;

    @CommandLine.Option(names = "--pwdfile", description = "Fitxer de paraules de pas conegudes.")
    private File passwordFile = new File("passwords.txt");

    @Override
    public void run() {
        try {
            // Validar archivos
            if (!inputFile.exists()) {
                System.err.println("El archivo de entrada no existe: " + inputFile.getAbsolutePath());
                return;
            }
            if (!passwordFile.exists()) {
                System.err.println("El archivo de contraseñas no existe: " + passwordFile.getAbsolutePath());
                return;
            }

            // Descifrar el mensaje primero
            DecryptCommand decryptCommand = new DecryptCommand();
            decryptCommand.setInputFile(inputFile);
            decryptCommand.setPasswordFile(passwordFile);
            decryptCommand.run();

            // Recuperar la clave encontrada desde CommandContext
            SecretKey keyTrobat = CommandContext.getFoundKey();
            if (keyTrobat == null) {
                System.err.println("No s’ha pogut desxifrar; no es pot replace.");
                return;
            }

            // Pedir un nuevo mensaje al usuario
            System.out.println("Introdueix el nou missatge en clar (línia única): ");
            String nouMissatge = new java.util.Scanner(System.in).nextLine();

            // Generar un nuevo IV
            byte[] newIv = new byte[16];
            new java.security.SecureRandom().nextBytes(newIv);

            // Cifrar el nuevo mensaje
            String ciphertextBase64 = AESUtils.encrypt(nouMissatge, keyTrobat, newIv);

            // Determinar el archivo de salida
            File out = (outputFile != null) ? outputFile : inputFile;

            // Escribir el IV y el ciphertext en líneas separadas
            Files.writeString(out.toPath(),
                    Base64.getEncoder().encodeToString(newIv) + "\n" + ciphertextBase64,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("Missatge re-xifrat guardat a: " + out.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("Error al reemplaçar el missatge: " + e.getMessage());
        }
    }
}