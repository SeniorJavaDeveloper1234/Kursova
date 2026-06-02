package ua.lpnu.deposits.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
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
 * GUI automation tests for the main navigation shell.
 * Verifies sidebar navigation, topbar title updates, and role-based visibility.
 */
@ExtendWith(ApplicationExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MainNavigationTest {

    @Start
    void start(Stage stage) throws Exception {
        AppContext.initialize();
        UserSession.login("admin", "ADMIN");

        FXMLLoader loader = new FXMLLoader(
                MainController.class.getResource("/fxml/MainView.fxml"));
        Scene scene = new Scene(loader.load(), 1280, 760);
        scene.getStylesheets().add(
                MainController.class.getResource("/css/style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    @Test
    @Order(1)
    void onStart_dashboardIsActiveAndTopbarTitleIsCorrect(FxRobot robot) {
        String title = robot.lookup("#topbarTitle").queryAs(Label.class).getText();
        assertEquals("Головна панель", title);
    }

    @Test
    @Order(2)
    void whenDepositsNavClicked_topbarTitleChangesToDeposits(FxRobot robot) {
        robot.clickOn("#navDeposits");

        String title = robot.lookup("#topbarTitle").queryAs(Label.class).getText();
        assertEquals("Депозити", title);
    }

    @Test
    @Order(3)
    void whenBanksNavClicked_topbarTitleChangesToBanks(FxRobot robot) {
        robot.clickOn("#navBanks");

        String title = robot.lookup("#topbarTitle").queryAs(Label.class).getText();
        assertEquals("Банки", title);
    }

    @Test
    @Order(4)
    void whenClientsNavClicked_topbarTitleChangesToClients(FxRobot robot) {
        robot.clickOn("#navClients");

        String title = robot.lookup("#topbarTitle").queryAs(Label.class).getText();
        assertEquals("Клієнти", title);
    }

    @Test
    @Order(5)
    void whenSearchNavClicked_topbarTitleChangesToSearch(FxRobot robot) {
        robot.clickOn("#navSearch");

        String title = robot.lookup("#topbarTitle").queryAs(Label.class).getText();
        assertEquals("Пошук та фільтр", title);
    }

    @Test
    @Order(6)
    void whenDashboardNavClicked_topbarTitleChangesBackToDashboard(FxRobot robot) {
        robot.clickOn("#navDeposits");
        robot.clickOn("#navDashboard");

        String title = robot.lookup("#topbarTitle").queryAs(Label.class).getText();
        assertEquals("Головна панель", title);
    }

    @Test
    @Order(7)
    void asAdmin_banksAndClientsNavButtonsAreVisible(FxRobot robot) {
        Button navBanks   = robot.lookup("#navBanks").queryAs(Button.class);
        Button navClients = robot.lookup("#navClients").queryAs(Button.class);

        assertTrue(navBanks.isVisible(),   "Admin must see the Banks nav button");
        assertTrue(navClients.isVisible(), "Admin must see the Clients nav button");
    }

    @Test
    @Order(8)
    void topbarUserChipDisplaysLoggedInUsername(FxRobot robot) {
        String chip = robot.lookup("#topbarUserChip").queryAs(Label.class).getText();
        assertTrue(chip.contains("admin"), "Topbar chip must display the current username");
    }

    @Test
    @Order(9)
    void sidebarUserLabelDisplaysLoggedInUsername(FxRobot robot) {
        String label = robot.lookup("#sidebarUserLabel").queryAs(Label.class).getText();
        assertEquals("admin", label);
    }

    @Test
    @Order(10)
    void asUser_banksAndClientsNavButtonsAreHidden(FxRobot robot) {
        Stage[] secondStage = {null};

        robot.interact(() -> {
            UserSession.logout();
            UserSession.login("user", "USER");
            try {
                FXMLLoader loader = new FXMLLoader(
                        MainController.class.getResource("/fxml/MainView.fxml"));
                Scene scene = new Scene(loader.load(), 1280, 760);
                scene.getStylesheets().add(
                        MainController.class.getResource("/css/style.css").toExternalForm());
                secondStage[0] = new Stage();
                secondStage[0].setScene(scene);
                secondStage[0].show();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        robot.interact(() -> {
            Button navBanks   = (Button) secondStage[0].getScene().lookup("#navBanks");
            Button navClients = (Button) secondStage[0].getScene().lookup("#navClients");
            assertFalse(navBanks.isVisible(),   "USER role must not see Banks nav button");
            assertFalse(navClients.isVisible(), "USER role must not see Clients nav button");
            secondStage[0].close();
            UserSession.logout();
            UserSession.login("admin", "ADMIN");
        });
    }
}
