package com.didacysebas;


import picocli.CommandLine;

import javax.crypto.SecretKey;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

@CommandLine.Command(
        name = "replace",
        mixinStandardHelpOptions = true,
        description = "Desxifra un missatge i permet generar-ne un de nou xifrat."
)
public class ReplaceCommand extends DecryptCommand {

    @CommandLine.Option(names = "--out", description = "Fitxer de sortida per al missatge re-xifrat.")
    private File outputFile;

    @Override
    public void run() {
        // 1) Fem primer el `run()` de la classe pare (DecryptCommand) per desxifrar
        super.run();

        // A `DecryptCommand.run()` has de preveure com "retornar" la contrasenya trobada i el missatge
        // Pots, per exemple, fer que `DecryptCommand` guardi en variables estàtiques
        // o fer-ho d’una altra manera més elegant (factoring out).
        SecretKey keyTrobat = getFoduundKey();    // L’hauries de "recuperar" d’alguna forma
        if (keyTrobat == null) {
            System.err.println("No s’ha pogut desxifrar; no es pot replace.");
            return;
        }

        // 2) Demanar text nou per consola
        System.out.println("Introdueix el nou missatge en clar (línia única): ");
        String nouMissatge = new java.util.Scanner(System.in).nextLine();

        try {
            // 3) Generar un nou IV o reutilitzar el vell (depenent de la teva estratègia)
            byte[] newIv = new byte[16];
            new java.security.SecureRandom().nextBytes(newIv);

            // 4) Xifrar el nou missatge
            String ciphertextBase64 = AESUtils.encrypt(nouMissatge, keyTrobat, newIv);

            // 5) Concatenar newIv + ciphertext i codificar tot en base64
            byte[] finalBytes = new byte[newIv.length + Base64.getDecoder().decode(ciphertextBase64).length];
            System.arraycopy(newIv, 0, finalBytes, 0, newIv.length);
            System.arraycopy(Base64.getDecoder().decode(ciphertextBase64), 0,
                    finalBytes, newIv.length,
                    Base64.getDecoder().decode(ciphertextBase64).length);

            String finalBase64 = Base64.getEncoder().encodeToString(finalBytes);

            // 6) Escriure el fitxer final
            File out = (outputFile != null) ? outputFile : getInputFile(); // si no s’especifica out, sobrescriu
            Files.writeString(out.toPath(), finalBase64, StandardOpenOption.CREATE);
            System.out.println("Missatge re-xifrat guardat a: " + out.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
