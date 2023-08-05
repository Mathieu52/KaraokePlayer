package net.xz3ra.www.karaokeplayer.karaoke.control;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import net.xz3ra.www.karaokeplayer.App;
import net.xz3ra.www.karaokeplayer.karaoke.KaraokePlayer;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class KaraokePlayerControl extends StackPane implements Initializable {
    public static final String FXML_PATH = "/net/xz3ra/www/karaokeplayer/fxml/playerUI.fxml";
    public static final String DURATION_FORMAT = "%02d:%02d"; //%02d:%02d
    private static final Duration SPEED_UP_DURATION = Duration.seconds(2.5);
    private KaraokePlayer karaokePlayer;

    private Parent eventRoot;

    private boolean playerWasPlaying;

    private boolean sliderChanging;

    @FXML
    private Label durationLabel;

    @FXML
    private Button rightButton;

    @FXML
    private Button playButton;

    @FXML
    private Button leftButton;

    @FXML
    private Label timeLabel;

    @FXML
    private Slider timeSlider;

    @FXML
    private ImageView volumeLevelImage;

    @FXML
    private Slider volumeSlider;

    public KaraokePlayerControl() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(FXML_PATH));
        fxmlLoader.setController(this);
        Parent root = fxmlLoader.load();

        this.getChildren().add(root);
        this.setAlignment(Pos.CENTER);
    }

    //  ********************* PUBLIC *********************

    public KaraokePlayerControl(KaraokePlayer karaokePlayer) throws IOException {
        this();
        karaokePlayer.onReadyProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                setKaraokePlayer(karaokePlayer);
            }
        }));
    }

    public void setKaraokePlayer(KaraokePlayer karaokePlayer) {
        if (karaokePlayer != null) {
            this.karaokePlayer = karaokePlayer;

            if (karaokePlayer.getStatus() != null && karaokePlayer.getStatus() == MediaPlayer.Status.READY) {
                initBindings();
            }


            karaokePlayer.statusProperty().addListener(((observable, oldValue, newValue) -> {
                if (newValue == MediaPlayer.Status.READY) {
                    Platform.runLater(this::initBindings);
                }
            }));
        }
    }

    public Parent getEventRoot() {
        return eventRoot;
    }

    public void setEventRoot(Parent eventRoot) {
        this.eventRoot = eventRoot;
    }

    //  ********************* PROTECTED *********************

    protected void initBindings() {
        karaokePlayer.volumeProperty().bindBidirectional(volumeSlider.valueProperty());

        karaokePlayer.currentTimeProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null && !sliderChanging) {
                updateTime(newValue);
            }
        }));

        karaokePlayer.cycleDurationProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateDuration(newValue);
            }
        }));

        initTimeSliderListener();

        updateTime(karaokePlayer.getCurrentTime());
        updateDuration(karaokePlayer.getTotalDuration());

        initKeyEvents();
    }

    //  ********************* PRIVATE *********************

    private void initTimeSliderListener() {
        ChangeListener<MediaPlayer.Status>[] listenerHolder = new ChangeListener[1];
        ChangeListener<MediaPlayer.Status> onPlayingUpdateValue = (observableValue, oldStatus, newStatus) -> {
            if (newStatus == MediaPlayer.Status.PLAYING) {
                Platform.runLater(() -> karaokePlayer.seek(Duration.seconds(timeSlider.getValue())));

                // Remove the listener using the stored reference
                karaokePlayer.statusProperty().removeListener(listenerHolder[0]);
            }
        };

        // Store the lambda reference in the array
        listenerHolder[0] = onPlayingUpdateValue;

        timeSlider.valueChangingProperty().addListener((observableValue, oldBoolean, newBoolean) -> {
            if (newBoolean != null) {
                sliderChanging = newBoolean;
                MediaPlayer.Status status = karaokePlayer.getStatus();
                if (newBoolean) {
                    playerWasPlaying = status == MediaPlayer.Status.PLAYING;
                    if (status == MediaPlayer.Status.PLAYING) {
                        karaokePlayer.pause();
                    }
                } else if (playerWasPlaying) {
                    karaokePlayer.play();
                    karaokePlayer.statusProperty().addListener(onPlayingUpdateValue);
                }

            }
        });
        timeSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            if (sliderChanging && newValue != null) {
                karaokePlayer.seek(Duration.seconds(newValue.doubleValue()));
                if (karaokePlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    karaokePlayer.pause();
                }
            }
        });
    }

    private void initKeyEvents() {
        if (eventRoot != null) {
            eventRoot.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
                boolean consumeEvent = true;
                switch (event.getCode()) {
                    case SPACE -> playButton.arm();
                    case LEFT -> leftButton.arm();
                    case RIGHT -> rightButton.arm();
                    default -> consumeEvent = false;
                }
                if (consumeEvent) {
                    event.consume();
                }
            });

            eventRoot.addEventFilter(KeyEvent.KEY_RELEASED, (KeyEvent event) -> {
                boolean consumeEvent = true;
                switch (event.getCode()) {
                    case SPACE -> {
                        playButton.disarm();
                        playButton.fire();
                    }
                    case LEFT -> {
                        leftButton.disarm();
                        leftButton.fire();
                    }
                    case RIGHT -> {
                        rightButton.disarm();
                        rightButton.fire();
                    }
                    default -> consumeEvent = false;
                }
                if (consumeEvent) {
                    event.consume();
                }
            });
        }
    }

    private void updateTime(Duration time) {
        if (time != null) {
            timeLabel.setText(formatDuration(time));
            timeSlider.setValue(time.toSeconds());
        }
    }

    private void updateDuration(Duration duration) {
        if (duration != null) {
            durationLabel.setText(formatDuration(duration));
            timeSlider.setMax(duration.toSeconds());
        }
    }

    @FXML
    void leftButtonAction(ActionEvent event) {
        if (karaokePlayer != null) {
            Duration time = karaokePlayer.getCurrentTime();
            karaokePlayer.seek(time.subtract(SPEED_UP_DURATION));
        }
    }

    @FXML
    void playButtonAction(ActionEvent event) {
        if (karaokePlayer != null) {
            MediaPlayer.Status status = karaokePlayer.getStatus();
            if (status == MediaPlayer.Status.PLAYING) {
                karaokePlayer.pause();
            } else if (status == MediaPlayer.Status.PAUSED || status == MediaPlayer.Status.READY) {
                karaokePlayer.play();
            }
        }
    }

    @FXML
    void rightButtonAction(ActionEvent event) {
        if (karaokePlayer != null) {
            Duration time = karaokePlayer.getCurrentTime();
            karaokePlayer.seek(time.add(SPEED_UP_DURATION));
        }
    }

    //  ********************* STATIC *********************

    protected static String formatDuration(Duration duration) {
        long minutes = (long) duration.toMinutes() % 60;
        long seconds = (long) duration.toSeconds() % 60;
        long milliseconds = (long) duration.toMillis() % 1000;

        return String.format(DURATION_FORMAT, minutes, seconds, milliseconds);
    }

    //  ********************* INITIALIZE *********************
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
