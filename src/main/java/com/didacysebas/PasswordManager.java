package com.didacysebas;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class PasswordManager {

    public static List<String> loadPasswords(File file) throws IOException {
        return Files.readAllLines(file.toPath());
    }

    public static void savePassword(File file, String password) throws IOException {
        Files.write(file.toPath(), (password + System.lineSeparator()).getBytes(),
                java.nio.file.StandardOpenOption.APPEND);
    }
}

