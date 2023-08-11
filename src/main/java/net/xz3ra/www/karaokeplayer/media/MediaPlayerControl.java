package net.xz3ra.www.karaokeplayer.media;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import net.xz3ra.www.karaokeplayer.App;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

public class MediaPlayerControl extends StackPane implements Initializable {
    public static final String FXML_PATH = "/net/xz3ra/www/karaokeplayer/fxml/playerUI.fxml";
    public static final String DURATION_FORMAT = "%02d:%02d"; //%02d:%02d
    private static final Duration SPEED_UP_DURATION = Duration.seconds(2.5);
    private SimpleObjectProperty<MediaPlayer> mediaPlayer = new SimpleObjectProperty<MediaPlayer>(null);

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

    public MediaPlayerControl() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(FXML_PATH));
        fxmlLoader.setController(this);
        Parent root = fxmlLoader.load();

        this.getChildren().add(root);
        this.setAlignment(Pos.CENTER);

        initMediaPlayerProperty();
        initKaraokePlayerListener();
        initTimeSliderListener();
        initKeyHandlers();
    }

    //  ********************* PUBLIC *********************

    public MediaPlayerControl(MediaPlayer player) throws IOException {
        this();
        setMediaPlayer(player);
    }

    private void initMediaPlayerProperty() {
        mediaPlayer.addListener((observable, oldMediaPlayer, newMediaPlayer) -> {
            removeKaraokePlayerListener(oldMediaPlayer);
            transferKaraokePlayerValues(oldMediaPlayer, newMediaPlayer);

            if (newMediaPlayer.getStatus() != null && newMediaPlayer.getStatus() == MediaPlayer.Status.READY) {
                addKaraokePlayerListener(newMediaPlayer);
            }

            if (oldMediaPlayer != null) {
                newMediaPlayer.statusProperty().removeListener(playerStatusListener);
            }

            playerStatusListener = (observableStatus, oldStatus, newStatus) -> {
                if (newStatus == MediaPlayer.Status.READY) {
                    Platform.runLater(() -> {
                        addKaraokePlayerListener(newMediaPlayer);
                        updateTime(newMediaPlayer.getCurrentTime());
                        updateDuration(newMediaPlayer.getTotalDuration());
                    });
                }
            };
            newMediaPlayer.statusProperty().addListener(playerStatusListener);
        });
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

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer.get();
    }

    public SimpleObjectProperty<MediaPlayer> playerMediaPropertyProperty() {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer.set(mediaPlayer);
    }

    //  ********************* PROTECTED *********************

    protected void initKaraokePlayerListener() {
        playerCurrentTimeListener = (observable, oldValue, newValue) -> {
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

    protected void addKaraokePlayerListener(MediaPlayer karaokePlayer) {
        if (karaokePlayer != null) {
            karaokePlayer.volumeProperty().bindBidirectional(volumeSlider.valueProperty());
            karaokePlayer.currentTimeProperty().addListener(playerCurrentTimeListener);
            karaokePlayer.cycleDurationProperty().addListener(playerCycleDurationListener);
        }
    }

    protected void transferKaraokePlayerValues(MediaPlayer oldKaraokePlayer, MediaPlayer newKaraokePlayer) {
        if (oldKaraokePlayer != null && newKaraokePlayer != null) {
            newKaraokePlayer.setVolume(oldKaraokePlayer.getVolume());
        }
    }

    protected void removeKaraokePlayerListener(MediaPlayer karaokePlayer) {
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
            if (mediaPlayer != null && newStatus == MediaPlayer.Status.PLAYING) {
                Platform.runLater(() -> getMediaPlayer().seek(Duration.seconds(timeSlider.getValue())));

                // Remove the listener using the stored reference
                getMediaPlayer().statusProperty().removeListener(listenerHolder[0]);
            }
        };

        // Store the lambda reference in the array
        listenerHolder[0] = onPlayingUpdateValue;

        ChangeListener<Boolean> sliderValueChangingListener = (observableValue, oldBoolean, newBoolean) -> {
            if (mediaPlayer != null && newBoolean != null) {
                sliderChanging = newBoolean;
                MediaPlayer.Status status = getMediaPlayer().getStatus();
                if (newBoolean) {
                    playerWasPlaying = status == MediaPlayer.Status.PLAYING;
                    if (status == MediaPlayer.Status.PLAYING) {
                        getMediaPlayer().pause();
                    }
                } else if (playerWasPlaying) {
                    getMediaPlayer().play();
                    getMediaPlayer().statusProperty().addListener(onPlayingUpdateValue);
                }

            }
        };

        ChangeListener<Number> sliderValueListener = (observableValue, oldValue, newValue) -> {
            if (mediaPlayer != null && newValue != null) {
                if (getMediaPlayer().getStatus() == MediaPlayer.Status.READY) {
                    updateTimeLabel(Duration.seconds(newValue.doubleValue()));
                }
                if (sliderChanging) {

                    getMediaPlayer().seek(Duration.seconds(newValue.doubleValue()));
                    if (getMediaPlayer().getStatus() == MediaPlayer.Status.PLAYING) {
                        getMediaPlayer().pause();
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
        if (mediaPlayer != null) {
            Duration time = getMediaPlayer().getCurrentTime();
            getMediaPlayer().seek(time.subtract(SPEED_UP_DURATION));
        }
    }

    @FXML
    void playButtonAction(ActionEvent event) {
        if (mediaPlayer != null) {
            MediaPlayer.Status status = getMediaPlayer().getStatus();
            if (status == MediaPlayer.Status.PLAYING) {
                getMediaPlayer().pause();
            } else if (status == MediaPlayer.Status.PAUSED || status == MediaPlayer.Status.READY) {
                getMediaPlayer().play();
            }
        }
    }

    @FXML
    void rightButtonAction(ActionEvent event) {
        if (mediaPlayer != null) {
            Duration time = getMediaPlayer().getCurrentTime();
            getMediaPlayer().seek(time.add(SPEED_UP_DURATION));
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
        initializeImageButton(playButton);
        initializeImageButton(leftButton);
        initializeImageButton(rightButton);

        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int index = (int) Math.ceil(newValue.doubleValue() * 4.0);
            volumeLevelImage.setImage(new Image(String.format("/net/xz3ra/www/karaokeplayer/media/volume_%d.png", index)));
        });
    }

    public void initializeImageButton(Button button) {
        ImageView imageView = (ImageView) button.getGraphic();
        button.setStyle("-fx-background-color: transparent");

        ColorAdjust effect = new ColorAdjust(0, 0, 0, 0);
        imageView.setEffect(effect);

        button.pressedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                effect.setBrightness(newValue ? -0.1 : 0);
            }
        });
    }
}
