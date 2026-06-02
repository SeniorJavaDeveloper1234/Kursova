package ua.lpnu.deposits.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import ua.lpnu.deposits.util.AppContext;
import ua.lpnu.deposits.util.UserSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * GUI automation tests for the Login screen.
 * Validates form input handling, error display, and error clearing behaviour.
 */
@ExtendWith(ApplicationExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoginViewTest {

    @Start
    void start(Stage stage) throws Exception {
        AppContext.initialize();
        UserSession.logout();

        FXMLLoader loader = new FXMLLoader(
                LoginController.class.getResource("/fxml/LoginView.fxml"));
        Scene scene = new Scene(loader.load(), 520, 480);
        scene.getStylesheets().add(
                LoginController.class.getResource("/css/style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    void clearFields(FxRobot robot) {
        robot.interact(() -> {
            robot.lookup("#usernameField").queryAs(TextField.class).clear();
            robot.lookup("#passwordField").queryAs(TextField.class).clear();
            robot.lookup("#errorLabel").queryAs(Label.class).setText("");
        });
    }

    @Test
    @Order(1)
    void givenBothFieldsEmpty_whenLoginClicked_thenValidationError(FxRobot robot) {
        robot.clickOn("#loginButton");

        String error = robot.lookup("#errorLabel").queryAs(Label.class).getText();
        assertEquals("Заповніть усі поля", error);
    }

    @Test
    @Order(2)
    void givenUsernameFilledPasswordEmpty_whenLoginClicked_thenValidationError(FxRobot robot) {
        robot.clickOn("#usernameField").write("admin");
        robot.clickOn("#loginButton");

        String error = robot.lookup("#errorLabel").queryAs(Label.class).getText();
        assertEquals("Заповніть усі поля", error);
    }

    @Test
    @Order(3)
    void givenWrongPassword_whenLoginClicked_thenAuthError(FxRobot robot) {
        robot.clickOn("#usernameField").write("admin");
        robot.clickOn("#passwordField").write("wrongpassword");
        robot.clickOn("#loginButton");

        String error = robot.lookup("#errorLabel").queryAs(Label.class).getText();
        assertEquals("Невірний логін або пароль", error);
    }

    @Test
    @Order(4)
    void givenWrongPassword_whenLoginClicked_thenPasswordFieldCleared(FxRobot robot) {
        robot.clickOn("#usernameField").write("admin");
        robot.clickOn("#passwordField").write("wrongpassword");
        robot.clickOn("#loginButton");

        String passwordText = robot.lookup("#passwordField").queryAs(TextField.class).getText();
        assertTrue(passwordText.isEmpty(), "Password field must be cleared after a failed login attempt");
    }

    @Test
    @Order(5)
    void givenErrorShown_whenUsernameFieldEdited_thenErrorClears(FxRobot robot) {
        robot.clickOn("#loginButton");
        assertFalse(robot.lookup("#errorLabel").queryAs(Label.class).getText().isEmpty());

        robot.clickOn("#usernameField").write("a");

        String error = robot.lookup("#errorLabel").queryAs(Label.class).getText();
        assertEquals("", error, "Error label must clear as soon as the user starts typing");
    }

    @Test
    @Order(6)
    void givenErrorShown_whenPasswordFieldEdited_thenErrorClears(FxRobot robot) {
        robot.clickOn("#loginButton");
        assertFalse(robot.lookup("#errorLabel").queryAs(Label.class).getText().isEmpty());

        robot.clickOn("#passwordField").write("x");

        String error = robot.lookup("#errorLabel").queryAs(Label.class).getText();
        assertEquals("", error, "Error label must clear as soon as the user starts typing");
    }

    @Test
    @Order(7)
    void givenNonExistentUser_whenLoginClicked_thenAuthError(FxRobot robot) {
        robot.clickOn("#usernameField").write("nobody");
        robot.clickOn("#passwordField").write("nopassword");
        robot.clickOn("#loginButton");

        String error = robot.lookup("#errorLabel").queryAs(Label.class).getText();
        assertEquals("Невірний логін або пароль", error);
    }
}
