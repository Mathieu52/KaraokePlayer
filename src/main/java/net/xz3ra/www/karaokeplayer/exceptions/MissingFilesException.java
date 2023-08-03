package net.xz3ra.www.karaokeplayer.exceptions;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.PrintStream;
import java.io.PrintWriter;

public class MissingFilesException extends Exception {
    private String message;

    public MissingFilesException(String message) {
        this.message = message;
    }

    public void showAlert() {
        showAlert(this);
    }

    public static void showAlert(MissingFilesException exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("File Not Found");
        alert.setHeaderText("Missing Files Exception");
        alert.setContentText(exception.message);
        alert.showAndWait();
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "MissingFilesException: " + message;
    }
}



