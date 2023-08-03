package net.xz3ra.www.karaokeplayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import net.xz3ra.www.karaokeplayer.exceptions.MissingFilesException;
import net.xz3ra.www.karaokeplayer.exceptions.UnsupportedFileTypeException;
import net.xz3ra.www.karaokeplayer.karaoke.Karaoke;
import net.xz3ra.www.karaokeplayer.karaoke.KaraokePlayer;

import java.io.IOException;

/**
 * JavaFX App
 */

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("fxml/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();

        /*
        try {
            Karaoke karaoke = new Karaoke("/Users/mathieudurand/Documents/EmboZone - Sky/My Flower.skf");
            KaraokePlayer karaokePlayer = new KaraokePlayer(karaoke);
            karaokePlayer.setOnReady(() -> karaokePlayer.play());
            karaokePlayer.lyricsIndexProperty().addListener(((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    System.out.println(newValue);
                }
            }));
           // karaokePlayer.setOnPlaying(() -> System.out.println(karaokePlayer.getActiveParagraph()));
        } catch (UnsupportedFileTypeException e) {
            e.showAlert();
            e.printStackTrace();
        } catch (MissingFilesException e) {
            e.showAlert();
            e.printStackTrace();
        }

         */
    }

    public static void main(String[] args) {
        launch();
    }
}
