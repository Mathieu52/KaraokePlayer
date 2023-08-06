package net.xz3ra.www.karaokeplayer;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.Parent;
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

    public Parent getRoot() {
        return root;
    }

    public void setRoot(Parent root) {
        this.root = root;
    }

    void loadFile(String path) {
        try {
            karaoke = Karaoke.load(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedFileTypeException e) {
            e.showAlert();
            //throw new RuntimeException(e);
        } catch (MissingFilesException e) {
            e.showAlert();
            throw new RuntimeException(e);
        }

        KaraokePlayer oldKaraokePlayer = karaokePlayer;
        try {
            karaokePlayer = new KaraokePlayer(karaoke);

            playerControl.setDisable(false);

            karaokeView.setKaraokePlayer(karaokePlayer);

            playerControl.setMediaPlayer(karaokePlayer.getMediaPlayer());
        } finally {
            if (oldKaraokePlayer != null) {
                oldKaraokePlayer.dispose();
            }
        }
    }

    @FXML
    void initialize() {
        playerControl.setEventRoot(karaokeView.getParent());
    }

}
