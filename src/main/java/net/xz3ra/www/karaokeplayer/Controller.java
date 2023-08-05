package net.xz3ra.www.karaokeplayer;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import net.xz3ra.www.karaokeplayer.exceptions.MissingFilesException;
import net.xz3ra.www.karaokeplayer.exceptions.UnsupportedFileTypeException;
import net.xz3ra.www.karaokeplayer.karaoke.Karaoke;
import net.xz3ra.www.karaokeplayer.karaoke.KaraokePlayer;
import net.xz3ra.www.karaokeplayer.karaoke.KaraokeView;
import net.xz3ra.www.karaokeplayer.karaoke.control.KaraokePlayerControl;
import net.xz3ra.www.karaokeplayer.manager.TimeManager;

public class Controller {

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
        playerControl.setEventRoot(root);
    }

    @FXML
    void initialize() {

        try {
            karaoke = new Karaoke("/Users/mathieudurand/Documents/EmboZone - Sky/My Flower - vocal");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedFileTypeException e) {
            throw new RuntimeException(e);
        } catch (MissingFilesException e) {
            throw new RuntimeException(e);
        }

        karaokePlayer = new KaraokePlayer(karaoke);

        karaokeView.setKaraokePlayer(karaokePlayer);

        playerControl.setKaraokePlayer(karaokePlayer);

        karaokePlayer.play();

        //  Skip the first part of the song (Testing only)
        Duration duration = Duration.seconds(18); //18: start of song
        karaokePlayer.setOnPlaying(() -> {
            karaokePlayer.seek(duration);
            karaokePlayer.seek(Duration.seconds(5));
        });
    }

}
