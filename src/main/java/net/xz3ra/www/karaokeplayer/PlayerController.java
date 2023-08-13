package net.xz3ra.www.karaokeplayer;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import net.xz3ra.www.karaokeplayer.exceptions.ExceptionAlertHandler;
import net.xz3ra.www.karaokeplayer.exceptions.MissingFilesException;
import net.xz3ra.www.karaokeplayer.exceptions.UnsupportedFileTypeException;
import net.xz3ra.www.karaokeplayer.karaoke.Karaoke;
import net.xz3ra.www.karaokeplayer.karaoke.KaraokePlayer;
import net.xz3ra.www.karaokeplayer.karaoke.KaraokeView;
import net.xz3ra.www.karaokeplayer.media.MediaPlayerControl;

public class PlayerController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private KaraokeView karaokeView;
    @FXML
    private MediaPlayerControl playerControl;

    private KaraokePlayer karaokePlayer;

    private Karaoke karaoke;

    private Parent root;

    private Scene scene;

    public Parent getRoot() {
        return root;
    }

    public void setRoot(Parent root) {
        this.root = root;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    void loadFile(String path) {
        try {
            karaoke = Karaoke.load(path);
        } catch (Exception e) {
            ExceptionAlertHandler.showAlert(e);
        }

        KaraokePlayer oldKaraokePlayer = karaokePlayer;
        try {
            if (karaoke != null) {
                karaokePlayer = new KaraokePlayer(karaoke);

                karaokeView.setKaraokePlayer(null);

                playerControl.setMediaPlayer(karaokePlayer.getMediaPlayer());

                playerControl.setDisable(false);
            }
        } catch (Exception e) {
            //throw new RuntimeException(e);
            ExceptionAlertHandler.showAlert(e);
        } finally {
            if (oldKaraokePlayer != null) {
                oldKaraokePlayer.dispose();
            }
        }
    }

    @FXML
    void initialize() {
        playerControl.setEventRoot(karaokeView.getParent());
        playerControl.fadedProperty().addListener((observable, oldFadedValue, newFadedValue) -> {
            if (newFadedValue != null && scene != null) {
                scene.setCursor(newFadedValue ? Cursor.NONE : Cursor.DEFAULT);
            }
        });
    }

}
