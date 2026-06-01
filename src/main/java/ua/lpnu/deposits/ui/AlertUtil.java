package ua.lpnu.deposits.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Static helpers for showing styled JavaFX alert dialogs in Ukrainian.
 */
public final class AlertUtil {

    private static final String STYLESHEET = "/css/style.css";

    private AlertUtil() {}

    /**
     * Shows a blocking error alert.
     *
     * @param title   dialog title
     * @param message error description shown to the user
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        applyStyle(alert);
        alert.showAndWait();
    }

    /**
     * Shows a blocking information alert.
     *
     * @param title   dialog title
     * @param message informational text
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        applyStyle(alert);
        alert.showAndWait();
    }

    /**
     * Shows a blocking confirmation alert.
     *
     * @param title   dialog title
     * @param message question text
     * @return {@code true} if the user clicked OK
     */
    public static boolean showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        applyStyle(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void applyStyle(Alert alert) {
        var url = AlertUtil.class.getResource(STYLESHEET);
        if (url != null) {
            alert.getDialogPane().getStylesheets().add(url.toExternalForm());
        }
    }
}
