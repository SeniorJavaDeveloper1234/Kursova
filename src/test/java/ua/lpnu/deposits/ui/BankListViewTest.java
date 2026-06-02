package ua.lpnu.deposits.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
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
import ua.lpnu.deposits.model.Bank;
import ua.lpnu.deposits.util.AppContext;
import ua.lpnu.deposits.util.UserSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * GUI automation tests for the Banks CRUD screen.
 * Requires an active admin session; verifies table population, button state
 * management, the add-bank dialog, and the inline search.
 *
 * <p>All helper lookups are scoped to the bank-pane root (bankTable's parent VBox)
 * to avoid ID collisions with the identical fx:ids in ClientListView.</p>
 */
@ExtendWith(ApplicationExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BankListViewTest {

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

    /** Navigate to Banks and reset state (search field, selection) before each test. */
    @BeforeEach
    void goToBanksAndResetState(FxRobot robot) {
        robot.clickOn("#navBanks");
        robot.interact(() -> {
            Node banksPane = banksRoot(robot);
            ((TextField) banksPane.lookup("#searchField")).clear();
            ((TableView<?>) banksPane.lookup("#bankTable")).getSelectionModel().clearSelection();
        });
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    @Order(1)
    void onLoad_banksTableIsPopulatedWithSeededData(FxRobot robot) {
        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<Bank> table = (TableView<Bank>) banksRoot(robot).lookup("#bankTable");
            assertFalse(table.getItems().isEmpty(), "Bank table must have rows after seeding");
        });
    }

    @Test
    @Order(2)
    void onLoad_countLabelReflectsNumberOfBanks(FxRobot robot) {
        robot.interact(() -> {
            Node root = banksRoot(robot);
            @SuppressWarnings("unchecked")
            TableView<Bank> table = (TableView<Bank>) root.lookup("#bankTable");
            Label countLabel = (Label) root.lookup("#countLabel");
            assertTrue(countLabel.getText().contains(String.valueOf(table.getItems().size())),
                    "Count label must reflect the number of loaded rows, was: " + countLabel.getText());
        });
    }

    @Test
    @Order(3)
    void onLoad_editAndDeleteButtonsAreDisabledWithNoSelection(FxRobot robot) {
        robot.interact(() -> {
            Node root = banksRoot(robot);
            Button edit   = (Button) root.lookup("#editButton");
            Button delete = (Button) root.lookup("#deleteButton");
            assertTrue(edit.isDisabled(),   "Edit button must be disabled when nothing is selected");
            assertTrue(delete.isDisabled(), "Delete button must be disabled when nothing is selected");
        });
    }

    @Test
    @Order(4)
    void whenFirstBankSelected_editAndDeleteButtonsBecomeEnabled(FxRobot robot) {
        robot.interact(() -> {
            Node root = banksRoot(robot);
            @SuppressWarnings("unchecked")
            TableView<Bank> table = (TableView<Bank>) root.lookup("#bankTable");

            // Select the first data row programmatically (no ambiguity with other tables)
            table.getSelectionModel().select(0);

            // Listener fires synchronously on the FX thread — check immediately
            Button edit   = (Button) root.lookup("#editButton");
            Button delete = (Button) root.lookup("#deleteButton");
            assertFalse(edit.isDisabled(),   "Edit button must be enabled after selecting a row");
            assertFalse(delete.isDisabled(), "Delete button must be enabled after selecting a row");
        });
    }

    @Test
    @Order(5)
    void whenAddBankDialogOpened_dialogPaneIsPresent(FxRobot robot) {
        robot.clickOn("➕  Додати банк");

        assertTrue(robot.lookup(".dialog-pane").tryQuery().isPresent(),
                "Clicking 'Add bank' must open a dialog pane");

        robot.interact(() -> robot.lookup(".dialog-pane").query().getScene().getWindow().hide());
    }

    @Test
    @Order(6)
    void whenAddBankDialogDismissed_tableRemainsUnchanged(FxRobot robot) {
        int[] rowsBefore = {0};
        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<Bank> t = (TableView<Bank>) banksRoot(robot).lookup("#bankTable");
            rowsBefore[0] = t.getItems().size();
        });

        robot.clickOn("➕  Додати банк");
        robot.interact(() -> robot.lookup(".dialog-pane").query().getScene().getWindow().hide());

        int[] rowsAfter = {0};
        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<Bank> t = (TableView<Bank>) banksRoot(robot).lookup("#bankTable");
            rowsAfter[0] = t.getItems().size();
        });

        assertEquals(rowsBefore[0], rowsAfter[0],
                "Dismissing the add-bank dialog must not change the table row count");
    }

    @Test
    @Order(7)
    void whenSearchByExistingName_filteredResultsShown(FxRobot robot) {
        robot.clickOn("#navBanks");
        robot.interact(() -> banksRoot(robot).lookup("#searchField").requestFocus());
        robot.write("Приват");
        robot.clickOn("Знайти");

        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<Bank> table = (TableView<Bank>) banksRoot(robot).lookup("#bankTable");
            assertFalse(table.getItems().isEmpty(),
                    "Search for 'Приват' must return at least one result");
            table.getItems().forEach(b ->
                    assertTrue(b.getName().contains("Приват"),
                            "Every search result must contain the term 'Приват'"));
        });
    }

    @Test
    @Order(8)
    void whenSearchByNonExistingName_zeroResultsShown(FxRobot robot) {
        robot.interact(() -> banksRoot(robot).lookup("#searchField").requestFocus());
        robot.write("ZZZNOBANKXXX");
        robot.clickOn("Знайти");

        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<Bank> table = (TableView<Bank>) banksRoot(robot).lookup("#bankTable");
            assertTrue(table.getItems().isEmpty(),
                    "Search for a non-existent name must return an empty table");
        });
    }

    @Test
    @Order(9)
    void whenRefreshClicked_searchFieldClearedAndAllBanksReloaded(FxRobot robot) {
        robot.interact(() -> banksRoot(robot).lookup("#searchField").requestFocus());
        robot.write("Приват");
        robot.clickOn("Знайти");

        // Fire the refresh button from within the banks pane to avoid clicking
        // the identically-named button in ClientListView (which is elsewhere in the scene).
        robot.interact(() -> {
            Node banksPane = banksRoot(robot);
            for (Node n : banksPane.lookupAll("Button")) {
                if (n instanceof Button b && b.getText().contains("Оновити")) {
                    b.fire();
                    break;
                }
            }
        });

        robot.interact(() -> {
            Node root = banksRoot(robot);
            TextField sf = (TextField) root.lookup("#searchField");
            assertTrue(sf.getText().isEmpty(), "Refresh must clear the search field");
            @SuppressWarnings("unchecked")
            TableView<Bank> table = (TableView<Bank>) root.lookup("#bankTable");
            assertFalse(table.getItems().isEmpty(), "Refresh must reload the full bank list");
        });
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    /**
     * Returns the root VBox of BankListView by walking up from bankTable.
     * This scopes subsequent lookups to the bank pane only, avoiding
     * fx:id collisions with the identically-named nodes in ClientListView.
     */
    private Node banksRoot(FxRobot robot) {
        return robot.lookup("#bankTable").query().getParent();
    }
}
