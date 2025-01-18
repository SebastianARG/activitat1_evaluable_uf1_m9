package com.didacysebas.commands;

import javax.crypto.SecretKey;

public class CommandContext {
    private static SecretKey foundKey;

    public static SecretKey getFoundKey() {
        return foundKey;
    }

    public static void setFoundKey(SecretKey key) {
        foundKey = key;
    }
}

