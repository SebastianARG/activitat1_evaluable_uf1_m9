package com.didacysebas.utilities;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class PasswordManager {

    public static List<String> loadPasswords(File file) throws IOException {
        if (!file.exists() || Files.size(file.toPath()) == 0) {
            return new ArrayList<>();
        }
        return Files.readAllLines(file.toPath());
    }


    public static void savePassword(File file, String password) throws IOException {
        List<String> passwords = loadPasswords(file);
        if (!passwords.contains(password)) {
            Files.write(file.toPath(), (password + System.lineSeparator()).getBytes(),
                    java.nio.file.StandardOpenOption.APPEND);
        }
    }

}

