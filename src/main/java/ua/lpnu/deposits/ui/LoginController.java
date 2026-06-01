package ua.lpnu.deposits.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ua.lpnu.deposits.repository.UserRepository;
import ua.lpnu.deposits.util.AppContext;
import ua.lpnu.deposits.util.AppLogger;
import ua.lpnu.deposits.util.UserSession;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Controller for the login screen.
 * Authenticates the user against the {@code users} table and opens the main window on success.
 */
public class LoginController implements Initializable {

    private static final AppLogger logger = AppLogger.getLogger(LoginController.class);

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;
    @FXML private Button        loginButton;

    private UserRepository userRepository;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userRepository = AppContext.getInstance().getUserRepository();

        // Allow submitting with Enter from the password field
        passwordField.setOnAction(e -> onLogin());
        usernameField.setOnAction(e -> passwordField.requestFocus());

        // Clear error when typing starts
        usernameField.textProperty().addListener((o, ov, nv) -> clearError());
        passwordField.textProperty().addListener((o, ov, nv) -> clearError());
    }

    // -------------------------------------------------------------------------
    // FXML handlers
    // -------------------------------------------------------------------------

    /**
     * Attempts to log in with the entered credentials.
     * Opens the main window on success, or shows an error on failure.
     */
    @FXML
    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Заповніть усі поля");
            return;
        }

        loginButton.setDisable(true);
        try {
            boolean ok = userRepository.authenticate(username, password);
            if (ok) {
                String role = userRepository.findByUsername(username)
                        .map(u -> u.getRole())
                        .orElse("USER");
                UserSession.login(username, role);
                logger.info("User '{}' logged in with role '{}'", username, role);
                openMainWindow();
            } else {
                logger.warn("Failed login attempt for username '{}'", username);
                showError("Невірний логін або пароль");
                passwordField.clear();
                passwordField.requestFocus();
            }
        } catch (SQLException e) {
            logger.error("Login DB error", e);
            showError("Помилка бази даних: " + e.getMessage());
        } finally {
            loginButton.setDisable(false);
        }
    }

    // -------------------------------------------------------------------------
    // Window management
    // -------------------------------------------------------------------------

    private void openMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/MainView.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 760);
            scene.getStylesheets().add(
                    getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Скарбниця");
            stage.setScene(scene);
            stage.setMinWidth(960);
            stage.setMinHeight(640);
            stage.show();

            // Close the login window
            ((Stage) loginButton.getScene().getWindow()).close();
        } catch (Exception e) {
            logger.error("Failed to open main window", e);
            showError("Не вдалося відкрити головне вікно:\n" + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void showError(String msg) {
        errorLabel.setText(msg);
    }

    private void clearError() {
        errorLabel.setText("");
    }
}
