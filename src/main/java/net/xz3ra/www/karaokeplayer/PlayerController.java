package net.xz3ra.www.karaokeplayer;

import java.io.IOException;
import java.nio.file.Path;

import java.util.logging.*;

import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuBar;
import javafx.scene.media.MediaException;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import net.xz3ra.www.karaokeplayer.exceptions.ExceptionAlertHandler;
import net.xz3ra.www.karaokeplayer.exceptions.InvalidFormatException;
import net.xz3ra.www.karaokeplayer.exceptions.MissingFilesException;
import net.xz3ra.www.karaokeplayer.exceptions.UnsupportedFileTypeException;
import net.xz3ra.www.karaokeplayer.karaoke.Karaoke;
import net.xz3ra.www.karaokeplayer.karaoke.KaraokePlayer;
import net.xz3ra.www.karaokeplayer.karaoke.KaraokeView;
import net.xz3ra.www.karaokeplayer.media.MediaPlayerControl;
import net.xz3ra.www.karaokeplayer.ressource.RessourceManager;
import net.xz3ra.www.karaokeplayer.util.AlertUtils;
import net.xz3ra.www.karaokeplayer.util.FileUIUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class PlayerController {
    private static final Path LOG_FILE = RessourceManager.LOG_FILE;
    private static final Logger logger = Logger.getLogger(PlayerController.class.getName());

    static {
        logger.addHandler(new ConsoleHandler());
        try {
            RessourceManager.createLogFile();
            logger.addHandler(new FileHandler(LOG_FILE.toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    MenuBar menuBar;
    @FXML
    private KaraokeView karaokeView;
    @FXML
    private MediaPlayerControl playerControl;

    public SimpleObjectProperty<Karaoke> karaoke = new SimpleObjectProperty<>();

    private SimpleObjectProperty<KaraokePlayer> karaokePlayer = new SimpleObjectProperty<>();

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

    public Karaoke getKaraoke() {
        return karaoke.get();
    }

    public SimpleObjectProperty<Karaoke> karaokeProperty() {
        return karaoke;
    }

    public void setKaraoke(Karaoke karaoke) {
        this.karaoke.set(karaoke);
    }

    public void loadFile(String path) {
        try {
            Karaoke karaoke = Karaoke.load(path);
            setKaraoke(karaoke);
        } catch (UnsupportedFileTypeException e) {
            ExceptionAlertHandler.showAlert(e);
            logger.log(Level.WARNING, "Tried to load Karaoke with unsupported file type: " + ExceptionUtils.getStackTrace(e));
        } catch (MissingFilesException e) {
            ExceptionAlertHandler.showAlert(e);
            logger.log(Level.WARNING, "Tried to load Karaoke but some expected files where missing: " + ExceptionUtils.getStackTrace(e));
        } catch (IOException e) {
            ExceptionAlertHandler.showAlert(e);
            logger.log(Level.WARNING, "Failed to load Karaoke (IOException): " + ExceptionUtils.getStackTrace(e));
        }  catch (InvalidFormatException e) {
            ExceptionAlertHandler.showAlert(e);
            logger.log(Level.WARNING, "Failed to load Karaoke because of invalid format",  ExceptionUtils.getStackTrace(e));
        }
    }
    private void onKaraokeChange(Observable observable, Karaoke oldKaraoke, Karaoke newKaraoke) {
        if (newKaraoke == null) {
            return;
        }

        if (newKaraoke.isEmpty()) {
            logger.log(Level.INFO, "An empty karaoke was received");
        }

        karaokePlayer.set(newKaraoke.isEmpty() ? null : new KaraokePlayer(newKaraoke));
        playerControl.setDisable(newKaraoke.isEmpty());
        try {
            karaokeView.setKaraokePlayer(karaokePlayer.get());
            playerControl.setMediaPlayer(karaokePlayer.get() == null ? null : karaokePlayer.get().getMediaPlayer());
        } catch (MediaException e) {
            handleMediaException(e);
        }
    }

    private void handleMediaException(MediaException exception) {
        AlertUtils.showAlert(Alert.AlertType.ERROR, exception.getClass().getSimpleName(), "Unable to load media (sound or video)", exception.getMessage());
        logger.log(Level.SEVERE, "Failed to properly load medaia (sound or video)", exception.getStackTrace());
    }

    @FXML
    void createBlankKaraoke() {
        FileUIUtils.FileCreator creator = (file) -> Karaoke.saveToFolder(file.toPath().toString(), Karaoke.EMPTY);
        FileUIUtils.saveFile(creator, "Create new blank karaoke", null);
    }

    @FXML
    void exportKaraoke() {
        FileUIUtils.FileCreator creator = (file) -> {
            try {
                Karaoke.save(file.toPath().toString(), getKaraoke(), true);
            } catch (UnsupportedFileTypeException e) {
                ExceptionAlertHandler.showAlert(e);
                logger.log(Level.WARNING, "Tried to save Karaoke to unsupported file type: " + ExceptionUtils.getStackTrace(e));
            }
        };

        FileUIUtils.saveFile(creator, "Export karaoke", Karaoke.ALLOWED_SAVING_TYPES.toArray(FileChooser.ExtensionFilter[]::new));
    }

    @FXML
    void openKaraokeFile() {
        FileUIUtils.FileLoader loader = (file) -> loadFile(file.toPath().toString());
        FileUIUtils.loadFile(loader, "Open karaoke file", Karaoke.ALLOWED_LOADING_TYPES.toArray(FileChooser.ExtensionFilter[]::new));
    }

    @FXML
    void openKaraokeDirectory() {
        FileUIUtils.FileLoader loader = (file) -> loadFile(file.toPath().toString());
        FileUIUtils.loadDirectory(loader, "Select destination");
    }

    @FXML
    void initialize() {
        playerControl.eventRootProperty().bind(rootProperty());
        playerControl.fadedProperty().addListener((observable, oldFadedValue, newFadedValue) -> {
            if (newFadedValue != null && sceneProperty().get() != null) {
                sceneProperty().get().setCursor(newFadedValue ? Cursor.NONE : Cursor.DEFAULT);
            }
        });

        menuBar.visibleProperty().bind(playerControl.fadedProperty().not());

        //  Handle Media error and dispose of old player
        karaokePlayer.addListener((observable, oldKaraokePlayer, newKaraokePlayer) -> {
            if (newKaraokePlayer != null) {
                newKaraokePlayer.setOnError(() -> {
                    MediaException exception = newKaraokePlayer.getError();
                    handleMediaException(exception);
                });
            }

            if (oldKaraokePlayer != null) {
                oldKaraokePlayer.dispose();
            }
        });

        karaokeProperty().addListener(this::onKaraokeChange);
    }

}
