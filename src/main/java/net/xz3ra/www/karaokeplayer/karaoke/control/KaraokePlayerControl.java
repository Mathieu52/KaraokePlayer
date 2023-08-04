package net.xz3ra.www.karaokeplayer.karaoke.control;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import net.xz3ra.www.karaokeplayer.App;
import net.xz3ra.www.karaokeplayer.karaoke.KaraokePlayer;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class KaraokePlayerControl extends StackPane implements Initializable {

    private KaraokePlayer karaokePlayer;

    @FXML
    private Label durationLabel;

    @FXML
    private Button fastforwardButton;

    @FXML
    private Button playButton;

    @FXML
    private Button reverseButton;

    @FXML
    private Label timeLabel;

    @FXML
    private Slider timeSlider;

    @FXML
    private ImageView volumeLevelImage;

    @FXML
    private Slider volumeSlider;
    public static final String FXML_PATH = "/net/xz3ra/www/karaokeplayer/fxml/playerUI.fxml";
    public KaraokePlayerControl() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(FXML_PATH));
        fxmlLoader.setController(this);
        Parent root = fxmlLoader.load();

        this.getChildren().add(root);
        this.setAlignment(Pos.CENTER);
    }

    public KaraokePlayerControl(KaraokePlayer karaokePlayer) throws IOException {
        this();
        setKaraokePlayer(karaokePlayer);
    }

    public void setKaraokePlayer(KaraokePlayer karaokePlayer) {
        this.karaokePlayer = karaokePlayer;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        karaokePlayer.volumeProperty().bind(volumeSlider.valueProperty());
    }
}
