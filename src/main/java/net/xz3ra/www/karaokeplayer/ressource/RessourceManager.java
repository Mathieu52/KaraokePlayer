package net.xz3ra.www.karaokeplayer.ressource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;

public class RessourceManager {
    public static final Path APPLICATION_DIRECTORY = Paths.get(System.getProperty("user.home"), ".karaokePlayer");
    public static final Path TEMPORARY_DIRECTORY = Paths.get(APPLICATION_DIRECTORY.toString(), "temp");

    public static final Path LOG_DIRECTORY = Paths.get(APPLICATION_DIRECTORY.toString(), "log");
    public static final Path LOG_FILE = Paths.get(LOG_DIRECTORY.toString(), "karaokeplayer.log");

    public static void createLogFile() {
        try {
            if (!Files.exists(LOG_FILE)) {
                Files.createDirectories(LOG_FILE.getParent());
                Files.createFile(LOG_FILE);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void clearTempDirectory() {
        clearDirectory(TEMPORARY_DIRECTORY.toFile());
    }

    private static void clearDirectory(File directory) {
        for (File file : directory.listFiles()) {
            if (!file.exists()) {
                continue;
            }

            if (file.isDirectory()) {
                clearDirectory(file);
            } else {
                file.delete();
            }
        }
    }
}
