package net.xz3ra.www.karaokeplayer.util;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static List<String> loadStringList(File file) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
            throw new RuntimeException("Error reading file: " + file.getName(), e);
        }
        return lines;
    }

    public static String loadString(File file) {
        List<String> lines = loadStringList(file);
        return String.join("\n", lines);
    }

    public static void saveStringList(File file, List<String> stringList) {
        try {
            if (!file.exists()) {
                Files.createFile(file.toPath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error creating file: " + file.getName(), e);
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            boolean firstLine = true;
            for (String line : stringList) {
                if (!firstLine) {
                    bw.newLine();
                }
                bw.write(line);
                firstLine = false;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file: " + file.getName(), e);
        }
    }

    public static void saveString(File file, String string) {
        try {
            if (!file.exists()) {
                Files.createFile(file.toPath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error creating file: " + file.getName(), e);
        }

        try {
            Files.write(file.toPath(), string.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file: " + file.getName(), e);
        }
    }

    public static String removeExtension(String fileName) {
        return fileName.replaceFirst("[.][^.]+$", "");
    }

    public static void deleteDirectory(File directory) {
        if (directory.exists()) {
            clearDirectory(directory);
            directory.delete();
        }
    }

    public static void clearDirectory(File directory) {
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
