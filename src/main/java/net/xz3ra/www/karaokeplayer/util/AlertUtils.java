package net.xz3ra.www.karaokeplayer.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.util.Optional;

public class AlertUtils {

    public static void showAlert(Alert.AlertType type, String title, String content) {
        showAlert(type, title, null, content, null);
    }

    public static void showAlert(Alert.AlertType type, String title, String header, String content) {
        showAlert(type, title, header, content, null);
    }

    public static void showAlert(Alert.AlertType type, String title, String header, String content, Window owner) {
        Alert alert = createAlert(type, title, header, content);
        initOwnerAndShow(alert, owner);
    }

    public static boolean showConfirmationAlert(String title, String content) {
        return showConfirmationAlert(title, null, content);
    }

    public static boolean showConfirmationAlert(String title, String header, String content) {
        return showConfirmationAlert(title, header, content, null);
    }

    public static boolean showConfirmationAlert(String title, String header, String content, Window owner) {
        Alert alert = createAlert(Alert.AlertType.CONFIRMATION, title, header, content);
        initOwnerAndShow(alert, owner);
        return alert.getResult() == ButtonType.OK;
    }

    public static String showTextInputDialog(String title, String content) {
        return showTextInputDialog(title, null, content);
    }

    public static String showTextInputDialog(String title, String header, String content) {
        return showTextInputDialog(title, header, content, null);
    }

    public static String showTextInputDialog(String title, String header, String content, Window owner) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        initOwnerAndShow(dialog, owner);
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private static Alert createAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert;
    }

    private static void initOwnerAndShow(Dialog stage, Window owner) {
        if (owner != null) {
            stage.initOwner(owner);
            stage.initStyle(StageStyle.UTILITY);
        }
        stage.showAndWait();
    }
}
