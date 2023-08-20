package net.xz3ra.www.karaokeplayer.util;

import javafx.event.ActionEvent;
import javafx.scene.control.Dialog;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;

public class FileUIUtils {

    public static void saveFile(FileCreator creator, String title, FileChooser.ExtensionFilter... filter) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        if (filter != null) {
            fileChooser.getExtensionFilters().addAll(filter);
        }

        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            creator.create(file);
        }
    }

    public static File loadDirectory(String title) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(title);

        return directoryChooser.showDialog(new Stage());
    }

    public static void loadDirectory(FileLoader loader, String title) {
        File file = loadDirectory(title);

        if (file != null) {
            loader.load(file);
        }
    }

    public static File loadFile(String title, FileChooser.ExtensionFilter... filter) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        if (filter != null) {
            fileChooser.getExtensionFilters().addAll(filter);
        }

        return fileChooser.showOpenDialog(new Stage());
    }

    public static void loadFile(FileLoader loader, String title, FileChooser.ExtensionFilter... filter) {
        File file = loadFile(title, filter);

        if (file != null) {
            loader.load(file);
        }
    }

    public interface FileCreator {
        void create(File file);
    }

    public interface FileLoader {
        void load(File file);
    }
}
