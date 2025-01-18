package com.didacysebas.logmanager;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogManager {
    private static Logger logger;

    public static Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger("MyAppLogger");
            setupLogger();
        }
        return logger;
    }

    private static void setupLogger() {
        try {
            //Sin modificar
            String logFilePath = "src/main/resources/app.log";

            // Desactivar el manejador por defecto (consola)
            Logger rootLogger = Logger.getLogger("");
            rootLogger.setLevel(Level.OFF); // Desactiva cualquier handler padre

            // Configura un FileHandler
            FileHandler fileHandler = new FileHandler(logFilePath, true); // 'true' para agregar al archivo existente
            fileHandler.setFormatter(new SimpleFormatter()); // Formato simple para los logs

            // Configurar el logger
            logger.setUseParentHandlers(false); // Desactiva los manejadores predeterminados
            logger.setLevel(Level.ALL); // Permite registrar todos los niveles de log
            logger.addHandler(fileHandler); // Añade el manejador de archivo

        } catch (IOException e) {
            System.err.println("No se pudo configurar el logger: " + e.getMessage());
        }
    }
}