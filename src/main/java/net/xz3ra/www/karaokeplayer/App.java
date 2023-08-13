package net.xz3ra.www.karaokeplayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.xz3ra.www.karaokeplayer.exceptions.ExceptionAlertHandler;
import net.xz3ra.www.karaokeplayer.karaoke.Karaoke;

import java.awt.*;
import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.OpenFilesHandler;
import java.io.IOException;

/**
 * JavaFX App
 */

public class App extends Application {
    static PlayerController playerController;

    static Runnable onStartComplete;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("fxml/main.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, 320, 240);

        playerController = fxmlLoader.getController();
        playerController.setRoot(root);
        playerController.setScene(scene);

        stage.setTitle("Karaoke Player");
        stage.setScene(scene);
        stage.show();

        //playerController.setKaraoke(Karaoke.EMPTY);

        if (onStartComplete != null) {
            onStartComplete.run();
        }
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            onStartComplete = () -> playerController.loadFile(args[0]);
        }

        launch();
    }

    static {
        try {
            Desktop.getDesktop().setOpenFileHandler(new FileHandler());
        } catch (Exception e) {}
    }

    public static class FileHandler implements OpenFilesHandler {

        @Override
        public void openFiles(OpenFilesEvent e) {
            if (playerController == null) {
                onStartComplete = () -> playerController.loadFile(e.getFiles().get(0).getAbsolutePath());
            } else {
                playerController.loadFile(e.getFiles().get(0).getAbsolutePath());
            }
        }
    }
}
