package net.xz3ra.www.karaokeplayer.exceptions;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.PrintStream;
import java.io.PrintWriter;

public class UnsupportedFileTypeException extends Exception {
    private String message;

    public UnsupportedFileTypeException(String message) {
        this.message = message;
    }

    public void showAlert() {
        showAlert(this);
    }

    public static void showAlert(UnsupportedFileTypeException exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Unsupported File Type");
        alert.setHeaderText("Unsupported File Type Exception");
        alert.setContentText(exception.message);
        alert.showAndWait();
    }
}
