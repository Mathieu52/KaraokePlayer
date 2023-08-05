package net.xz3ra.www.karaokeplayer.karaoke.control;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

    private boolean playerWasPlaying = false;

    private boolean sliderChanging = false;

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

    //  Listeners
    private ChangeListener<Duration> playerCurrentTimeListener;
    private ChangeListener<Duration> playerCycleDurationListener;
    private ChangeListener<MediaPlayer.Status> playerStatusListener;
    private EventHandler<KeyEvent> keyPressedHandler;
    private EventHandler<KeyEvent> keyReleasedHandler;

    public KaraokePlayerControl() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(FXML_PATH));
        fxmlLoader.setController(this);
        Parent root = fxmlLoader.load();

        this.getChildren().add(root);
        this.setAlignment(Pos.CENTER);

        initKaraokePlayerListener();
        initTimeSliderListener();
        initKeyHandlers();
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
            removeKaraokePlayerListener(this.karaokePlayer);
            transferKaraokePlayerValues(this.karaokePlayer, karaokePlayer);

            if (karaokePlayer.getStatus() != null && karaokePlayer.getStatus() == MediaPlayer.Status.READY) {
                addKaraokePlayerListener(karaokePlayer);
            }

            if (this.karaokePlayer != null) {
                this.karaokePlayer.statusProperty().removeListener(playerStatusListener);
            }

            playerStatusListener = (observable, oldValue, newValue) -> {
                if (newValue == MediaPlayer.Status.READY) {
                    Platform.runLater(() -> {
                        addKaraokePlayerListener(karaokePlayer);
                        updateTime(karaokePlayer.getCurrentTime());
                        updateDuration(karaokePlayer.getTotalDuration());
                    });
                }
            };
            karaokePlayer.statusProperty().addListener(playerStatusListener);

            this.karaokePlayer = karaokePlayer;
        }
    }

    public Parent getEventRoot() {
        return eventRoot;
    }

    public void setEventRoot(Parent eventRoot) {
        if (eventRoot != null) {
            updateKeyHandlers(this.eventRoot, eventRoot);
            this.eventRoot = eventRoot;
        }
    }

    //  ********************* PROTECTED *********************

    protected void initKaraokePlayerListener() {
        playerCurrentTimeListener = (observable, oldValue, newValue) -> {
            System.out.println("test");
            if (newValue != null) {
                updateTime(newValue);
            }
        };

        playerCycleDurationListener = (observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateDuration(newValue);
            }
        };
    }

    protected void addKaraokePlayerListener(KaraokePlayer karaokePlayer) {
        if (karaokePlayer != null) {
            karaokePlayer.volumeProperty().bindBidirectional(volumeSlider.valueProperty());
            karaokePlayer.currentTimeProperty().addListener(playerCurrentTimeListener);
            karaokePlayer.cycleDurationProperty().addListener(playerCycleDurationListener);
        }
    }

    protected void transferKaraokePlayerValues(KaraokePlayer oldKaraokePlayer, KaraokePlayer newKaraokePlayer) {
        if (oldKaraokePlayer != null && newKaraokePlayer != null) {
            newKaraokePlayer.setVolume(oldKaraokePlayer.getVolume());
        }
    }

    protected void removeKaraokePlayerListener(KaraokePlayer karaokePlayer) {
        if (karaokePlayer != null) {
            karaokePlayer.volumeProperty().unbindBidirectional(volumeSlider.valueProperty());
            karaokePlayer.currentTimeProperty().removeListener(playerCurrentTimeListener);
            karaokePlayer.cycleDurationProperty().removeListener(playerCycleDurationListener);
        }
    }

    //  ********************* PRIVATE *********************

    private void initTimeSliderListener() {
        ChangeListener<MediaPlayer.Status>[] listenerHolder = new ChangeListener[1];
        ChangeListener<MediaPlayer.Status> onPlayingUpdateValue = (observableValue, oldStatus, newStatus) -> {
            if (karaokePlayer != null && newStatus == MediaPlayer.Status.PLAYING) {
                Platform.runLater(() -> karaokePlayer.seek(Duration.seconds(timeSlider.getValue())));

                // Remove the listener using the stored reference
                karaokePlayer.statusProperty().removeListener(listenerHolder[0]);
            }
        };

        // Store the lambda reference in the array
        listenerHolder[0] = onPlayingUpdateValue;

        ChangeListener<Boolean> sliderValueChangingListener = (observableValue, oldBoolean, newBoolean) -> {
            if (karaokePlayer != null && newBoolean != null) {
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
        };

        ChangeListener<Number> sliderValueListener = (observableValue, oldValue, newValue) -> {
            if (karaokePlayer != null && newValue != null) {
                if (karaokePlayer.getStatus() == MediaPlayer.Status.READY) {
                    updateTimeLabel(Duration.seconds(newValue.doubleValue()));
                }
                if (sliderChanging) {

                    karaokePlayer.seek(Duration.seconds(newValue.doubleValue()));
                    if (karaokePlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                        karaokePlayer.pause();
                    }
                }
            }
        };

        timeSlider.valueChangingProperty().addListener(sliderValueChangingListener);
        timeSlider.valueProperty().addListener(sliderValueListener);
    }

    private void initKeyHandlers() {
        keyPressedHandler = (KeyEvent event) -> {
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
        };

        keyReleasedHandler = (KeyEvent event) -> {
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
        };
    }

    private void updateKeyHandlers(Parent oldEventRoot, Parent newEventRoot) {
        if (oldEventRoot != null) {
            oldEventRoot.removeEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
            oldEventRoot.removeEventFilter(KeyEvent.KEY_RELEASED, keyReleasedHandler);
        }

        if (newEventRoot != null) {
            newEventRoot.addEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
            newEventRoot.addEventFilter(KeyEvent.KEY_RELEASED, keyReleasedHandler);
        }
    }

    private void updateTime(Duration time) {
        if (time != null) {
            updateTimeLabel(time);
            if (!sliderChanging) {
                timeSlider.setValue(time.toSeconds());
            }
        }
    }

    private void updateTimeLabel(Duration time) {
        timeLabel.setText(formatDuration(time));
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
