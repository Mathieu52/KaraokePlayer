package net.xz3ra.www.karaokeplayer.exceptions;

import javafx.scene.control.Alert;

public class InvalidFormatException extends AlertThrowingException {
    private String message;

    public InvalidFormatException(String message) {
        this.message = message;
    }

    public void showAlert() {
        showAlert(this);
    }

    public static void showAlert(InvalidFormatException exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid format");
        alert.setHeaderText("Invalid format exception");
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
