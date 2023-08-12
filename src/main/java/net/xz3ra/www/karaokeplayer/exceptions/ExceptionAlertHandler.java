package net.xz3ra.www.karaokeplayer.exceptions;

import javafx.scene.control.Alert;

public class ExceptionAlertHandler {
    public static void showAlert(Exception exception) {
        if (exception instanceof AlertThrowingException) {
            showAlert((AlertThrowingException) exception);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);

            String simpleName = exception.getClass().getSimpleName();
            alert.setTitle(addSpacesToExceptionName(simpleName));

            alert.setHeaderText(simpleName);

            alert.setContentText(exception.getMessage());
            alert.showAndWait();
        }
    }

    public static String addSpacesToExceptionName(String input) {
        StringBuilder formattedName = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            if (Character.isUpperCase(currentChar) && i != 0) {
                formattedName.append(' ');
                formattedName.append(Character.toLowerCase(currentChar));
            } else {
                formattedName.append(currentChar);
            }
        }

        return formattedName.toString();
    }

    public static void showAlert(AlertThrowingException exception) {
        exception.showAlert();
    }
}
