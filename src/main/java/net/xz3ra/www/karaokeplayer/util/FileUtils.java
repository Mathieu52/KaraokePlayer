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
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (String line : stringList) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file: " + file.getName(), e);
        }
    }

    public static void saveString(File file, String string) {
        try {
            Files.write(file.toPath(), string.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file: " + file.getName(), e);
        }
    }

    public static String removeExtension(String fileName) {
        return fileName.replaceFirst("[.][^.]+$", "");
    }
}
