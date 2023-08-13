package net.xz3ra.www.karaokeplayer;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import net.xz3ra.www.karaokeplayer.exceptions.ExceptionAlertHandler;
import net.xz3ra.www.karaokeplayer.karaoke.Karaoke;
import net.xz3ra.www.karaokeplayer.karaoke.KaraokePlayer;
import net.xz3ra.www.karaokeplayer.karaoke.KaraokeView;
import net.xz3ra.www.karaokeplayer.media.MediaPlayerControl;

public class PlayerController {

    @FXML
    private KaraokeView karaokeView;
    @FXML
    private MediaPlayerControl playerControl;

    private KaraokePlayer karaokePlayer;

    private Karaoke karaoke;

    private SimpleObjectProperty<Parent> root = new SimpleObjectProperty<>();

    private SimpleObjectProperty<Scene> scene = new SimpleObjectProperty<>();

    public Parent getRoot() {
        return root.get();
    }

    public SimpleObjectProperty<Parent> rootProperty() {
        return root;
    }

    public void setRoot(Parent root) {
        this.root.set(root);
    }

    public Scene getScene() {
        return scene.get();
    }

    public SimpleObjectProperty<Scene> sceneProperty() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene.set(scene);
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

                karaokeView.setKaraokePlayer(karaokePlayer);

                playerControl.setMediaPlayer(karaokePlayer.getMediaPlayer());

                playerControl.setDisable(false);
            }
        } catch (Exception e) {
            ExceptionAlertHandler.showAlert(e);
        } finally {
            if (oldKaraokePlayer != null) {
                oldKaraokePlayer.dispose();
            }
        }
    }

    @FXML
    void initialize() {
        playerControl.eventRootProperty().bind(rootProperty());
        playerControl.fadedProperty().addListener((observable, oldFadedValue, newFadedValue) -> {
            if (newFadedValue != null && sceneProperty().get() != null) {
                sceneProperty().get().setCursor(newFadedValue ? Cursor.NONE : Cursor.DEFAULT);
            }
        });
    }

}
