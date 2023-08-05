package net.xz3ra.www.karaokeplayer;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.util.Duration;
import net.xz3ra.www.karaokeplayer.exceptions.MissingFilesException;
import net.xz3ra.www.karaokeplayer.exceptions.UnsupportedFileTypeException;
import net.xz3ra.www.karaokeplayer.karaoke.Karaoke;
import net.xz3ra.www.karaokeplayer.karaoke.KaraokePlayer;
import net.xz3ra.www.karaokeplayer.karaoke.KaraokeView;
import net.xz3ra.www.karaokeplayer.karaoke.control.KaraokePlayerControl;
import net.xz3ra.www.karaokeplayer.manager.TimeManager;

public class PlayerController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private KaraokeView karaokeView;
    @FXML
    private KaraokePlayerControl playerControl;

    private TimeManager timeManager = new TimeManager();

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
            karaoke = new Karaoke(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedFileTypeException e) {
            throw new RuntimeException(e);
        } catch (MissingFilesException e) {
            throw new RuntimeException(e);
        }

        karaokePlayer.dispose();
        karaokePlayer = new KaraokePlayer(karaoke);

        playerControl.setDisable(false);

        karaokeView.setKaraokePlayer(karaokePlayer);

        playerControl.setKaraokePlayer(karaokePlayer);
    }

    @FXML
    void initialize() {
        playerControl.setEventRoot(karaokeView.getParent());
    }

}
