package ua.lpnu.deposits.ui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import ua.lpnu.deposits.model.Client;
import ua.lpnu.deposits.model.Deposit;
import ua.lpnu.deposits.util.AppContext;
import ua.lpnu.deposits.util.DatabaseConnection;
import ua.lpnu.deposits.util.UserSession;

import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TestFX GUI automation tests for the Deposit add/edit form (DepositFormView).
 *
 * <p>Covers the client-assignment feature added to {@link DepositFormController}:
 * ComboBox default state, enable/disable behaviour of the amount field,
 * validation error paths, and the full save-with-client flow.</p>
 *
 * <p>Tests 8 and 9 write to the database; each cleans up its own records after
 * asserting so the DB is left in the same state as before.  All node lookups
 * inside deposits-list are scoped to {@code #depositListRoot} to avoid fx:id
 * collisions with identically-named nodes in ClientListView.</p>
 */
@ExtendWith(ApplicationExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DepositFormViewTest {

    // ─── Bootstrap ───────────────────────────────────────────────────────────

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

    @BeforeEach
    void goToDepositsAndReset(FxRobot robot) {
        robot.clickOn("#navDeposits");
        robot.interact(() ->
                ((TableView<?>) depositsRoot(robot).lookup("#depositTable"))
                        .getSelectionModel().clearSelection());
    }

    /**
     * Closes any leftover modal windows so tests don't bleed into each other:
     * first any open Alert (identified by {@code .dialog-pane}),
     * then the deposit form (identified by {@code #nameField}).
     */
    @AfterEach
    void closeAnyOpenModals(FxRobot robot) {
        robot.lookup(".dialog-pane").tryQuery().ifPresent(n ->
                robot.interact(() -> n.getScene().getWindow().hide()));
        robot.lookup("#nameField").tryQuery().ifPresent(n ->
                robot.interact(() -> n.getScene().getWindow().hide()));
    }

    // ─── Test 1: client ComboBox default is "— Не призначати —" ─────────────

    @Test
    @Order(1)
    void onOpenAddForm_clientComboBoxDefaultIsNoAssign(FxRobot robot) {
        robot.clickOn("➕  Додати");

        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            ComboBox<Client> cb = (ComboBox<Client>) robot.lookup("#clientComboBox").query();
            assertNull(cb.getValue(),
                    "Default selection must be null ('— Не призначати —')");
        });
    }

    // ─── Test 2: amount field is disabled when no client is selected ──────────

    @Test
    @Order(2)
    void onOpenAddForm_depositAmountFieldIsDisabledByDefault(FxRobot robot) {
        robot.clickOn("➕  Додати");

        robot.interact(() -> {
            TextField field = (TextField) robot.lookup("#depositAmountField").query();
            assertTrue(field.isDisabled(),
                    "Amount field must be disabled when no client is selected");
        });
    }

    // ─── Test 3: ComboBox is populated with seeded clients ───────────────────

    @Test
    @Order(3)
    void onOpenAddForm_clientComboBoxPopulatedWithClients(FxRobot robot) {
        robot.clickOn("➕  Додати");

        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            ComboBox<Client> cb = (ComboBox<Client>) robot.lookup("#clientComboBox").query();
            assertTrue(cb.getItems().size() >= 2,
                    "ComboBox must have at least 2 items: null sentinel + at least one client");
            assertNull(cb.getItems().get(0),
                    "First item must be null ('— Не призначати —')");
            assertNotNull(cb.getItems().get(1),
                    "Second item must be a real Client object");
        });
    }

    // ─── Test 4: selecting a client enables the amount field ─────────────────

    @Test
    @Order(4)
    void onSelectClient_depositAmountFieldBecomesEnabled(FxRobot robot) {
        robot.clickOn("➕  Додати");

        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            ComboBox<Client> cb = (ComboBox<Client>) robot.lookup("#clientComboBox").query();
            cb.getSelectionModel().select(1); // first real client
        });

        robot.interact(() -> {
            TextField field = (TextField) robot.lookup("#depositAmountField").query();
            assertFalse(field.isDisabled(),
                    "Amount field must be enabled after selecting a client");
        });
    }

    // ─── Test 5: deselecting client disables the amount field again ───────────

    @Test
    @Order(5)
    void onDeselectClient_depositAmountFieldBecomesDisabledAgain(FxRobot robot) {
        robot.clickOn("➕  Додати");

        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            ComboBox<Client> cb = (ComboBox<Client>) robot.lookup("#clientComboBox").query();
            cb.getSelectionModel().select(1); // select real client
        });
        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            ComboBox<Client> cb = (ComboBox<Client>) robot.lookup("#clientComboBox").query();
            cb.getSelectionModel().select(0); // back to "— Не призначати —"
        });

        robot.interact(() -> {
            TextField field = (TextField) robot.lookup("#depositAmountField").query();
            assertTrue(field.isDisabled(),
                    "Amount field must be disabled again after re-selecting 'no client'");
        });
    }

    // ─── Test 6: saving with client but empty amount triggers validation ──────

    @Test
    @Order(6)
    void onSaveWithClientAndEmptyAmount_showsValidationError(FxRobot robot) {
        robot.clickOn("➕  Додати");
        robot.interact(() -> {
            fillRequiredFields(robot, "Тест Валідація 1", "1000", "10.0", "12");
            @SuppressWarnings("unchecked")
            ComboBox<Client> cb = (ComboBox<Client>) robot.lookup("#clientComboBox").query();
            cb.getSelectionModel().select(1);
            // amount field intentionally left empty
        });

        fireSaveButton(robot);

        assertTrue(robot.lookup(".dialog-pane").tryQuery().isPresent(),
                "Validation error dialog must appear when client is selected but amount is empty");
        robot.clickOn("OK");
    }

    // ─── Test 7: amount below minimum triggers validation ────────────────────

    @Test
    @Order(7)
    void onSaveWithClientAndAmountBelowMin_showsValidationError(FxRobot robot) {
        robot.clickOn("➕  Додати");
        robot.interact(() -> {
            fillRequiredFields(robot, "Тест Валідація 2", "5000", "10.0", "12");
            @SuppressWarnings("unchecked")
            ComboBox<Client> cb = (ComboBox<Client>) robot.lookup("#clientComboBox").query();
            cb.getSelectionModel().select(1);
        });
        robot.interact(() ->
                ((TextField) robot.lookup("#depositAmountField").query()).setText("100")); // below 5000

        fireSaveButton(robot);

        assertTrue(robot.lookup(".dialog-pane").tryQuery().isPresent(),
                "Validation error dialog must appear when amount is below the deposit minimum");
        robot.clickOn("OK");
    }

    // ─── Test 8: save without client adds deposit to the table ───────────────

    @Test
    @Order(8)
    void onSaveWithoutClient_depositAddedToTable(FxRobot robot) throws Exception {
        int[] rowsBefore = {0};
        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<Deposit> t =
                    (TableView<Deposit>) depositsRoot(robot).lookup("#depositTable");
            rowsBefore[0] = t.getItems().size();
        });

        robot.clickOn("➕  Додати");
        robot.interact(() ->
                fillRequiredFields(robot, "Тест Без Клієнта", "1000", "10.0", "12"));
        // client stays at "— Не призначати —"
        fireSaveButton(robot);
        robot.clickOn("OK");
        robot.interact(() -> {}); // wait for form to close + onSaveCallback to refresh table

        int[] newId = {0};
        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<Deposit> t =
                    (TableView<Deposit>) depositsRoot(robot).lookup("#depositTable");
            assertEquals(rowsBefore[0] + 1, t.getItems().size(),
                    "Table must grow by one after saving a deposit without a client");
            t.getItems().stream()
                    .filter(d -> "Тест Без Клієнта".equals(d.getName()))
                    .findFirst()
                    .ifPresent(d -> newId[0] = d.getId());
        });
        assertTrue(newId[0] > 0, "New deposit must have a non-zero id");

        // Cleanup
        robot.interact(() -> {
            try {
                AppContext.getInstance().getDepositService().deleteDeposit(newId[0]);
            } catch (Exception e) {
                throw new RuntimeException("Cleanup: deleteDeposit failed", e);
            }
        });
        refreshDepositsTable(robot);
    }

    // ─── Test 9: save with client creates deposit AND client_deposit ──────────

    @Test
    @Order(9)
    void onSaveWithClientAndValidAmount_clientLinkedToDeposit(FxRobot robot) throws Exception {
        int[] newDepositId = {0};

        robot.clickOn("➕  Додати");
        robot.interact(() -> {
            fillRequiredFields(robot, "Тест З Клієнтом", "1000", "12.5", "6");
            @SuppressWarnings("unchecked")
            ComboBox<Client> cb = (ComboBox<Client>) robot.lookup("#clientComboBox").query();
            cb.getSelectionModel().select(1); // first real client
        });
        robot.interact(() ->
                ((TextField) robot.lookup("#depositAmountField").query()).setText("2000"));
        fireSaveButton(robot);
        robot.clickOn("OK");
        robot.interact(() -> {}); // wait for form to close + onSaveCallback to refresh table

        // Verify deposit appears in the list
        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<Deposit> t =
                    (TableView<Deposit>) depositsRoot(robot).lookup("#depositTable");
            Deposit created = t.getItems().stream()
                    .filter(d -> "Тест З Клієнтом".equals(d.getName()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Saved deposit not found in table"));
            newDepositId[0] = created.getId();
        });

        // Verify an active client_deposit record was created for the new deposit
        robot.interact(() -> {
            try {
                assertTrue(
                        AppContext.getInstance().getDepositService()
                                .getActiveClientForDeposit(newDepositId[0])
                                .isPresent(),
                        "An active client_deposit record must exist after saving with a client");
            } catch (Exception e) {
                throw new RuntimeException("getActiveClientForDeposit failed", e);
            }
        });

        // Cleanup: remove client_deposit rows first (FK constraint), then the deposit
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM client_deposits WHERE deposit_id = ?")) {
            ps.setInt(1, newDepositId[0]);
            ps.executeUpdate();
        }
        robot.interact(() -> {
            try {
                AppContext.getInstance().getDepositService().deleteDeposit(newDepositId[0]);
            } catch (Exception e) {
                throw new RuntimeException("Cleanup: deleteDeposit failed", e);
            }
        });
        refreshDepositsTable(robot);
    }

    // ─── Test 10: edit form pre-fills deposit name ───────────────────────────

    @Test
    @Order(10)
    void onEditDeposit_formPrefilledWithDepositName(FxRobot robot) {
        String[] expected = {""};
        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<Deposit> t =
                    (TableView<Deposit>) depositsRoot(robot).lookup("#depositTable");
            t.getSelectionModel().select(0);
            expected[0] = t.getItems().get(0).getName();
        });

        // Click the edit button scoped to the deposits pane to avoid id collisions
        final Node[] editBtn = {null};
        robot.interact(() -> editBtn[0] = depositsRoot(robot).lookup("#editButton"));
        robot.clickOn(editBtn[0]);

        robot.interact(() -> {
            TextField nameField = (TextField) robot.lookup("#nameField").query();
            assertEquals(expected[0], nameField.getText(),
                    "Name field must be pre-filled with the selected deposit's name");
        });
    }

    // ─── Test 11: editing deposit with active client pre-selects that client ──

    @Test
    @Order(11)
    void onEditDepositWithActiveClient_clientPreselectedInComboBox(FxRobot robot) {
        // Deposit id=1 ("Строковий Плюс") has client 1 ACTIVE in seeded data
        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<Deposit> t =
                    (TableView<Deposit>) depositsRoot(robot).lookup("#depositTable");
            for (int i = 0; i < t.getItems().size(); i++) {
                if (t.getItems().get(i).getId() == 1) {
                    t.getSelectionModel().select(i);
                    break;
                }
            }
        });

        final Node[] editBtn = {null};
        robot.interact(() -> editBtn[0] = depositsRoot(robot).lookup("#editButton"));
        robot.clickOn(editBtn[0]);

        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            ComboBox<Client> cb = (ComboBox<Client>) robot.lookup("#clientComboBox").query();
            assertNotNull(cb.getValue(),
                    "Editing a deposit with an ACTIVE client must pre-select that client in the ComboBox");
        });
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Fires the save button inside the deposit form without blocking the test thread.
     *
     * <p>TestFX 4 + {@code APPLICATION_MODAL} + double-nested {@code showAndWait}
     * (form window + alert) causes {@code robot.clickOn()} to stall because
     * {@code waitForFxEvents()} waits for the nested alert loop to exit, which
     * never happens without a subsequent robot action.  The workaround:
     * post {@code btn.fire()} via {@link Platform#runLater} so the test thread
     * does NOT wait for it; then run an empty {@code robot.interact} which only
     * returns after the FX thread has reached its next idle point — i.e. after
     * the alert's nested event loop has started.</p>
     */
    private void fireSaveButton(FxRobot robot) {
        final Button[] btn = {null};
        robot.interact(() -> btn[0] = (Button) robot.lookup("#saveButton").query());
        Platform.runLater(() -> btn[0].fire());
        robot.interact(() -> {}); // returns once FX is idle inside the alert's nested loop
    }

    /**
     * Scopes all deposit-list node lookups to the root VBox of DepositListView,
     * preventing fx:id collisions with identically-named nodes in ClientListView.
     */
    private Node depositsRoot(FxRobot robot) {
        return robot.lookup("#depositListRoot").query();
    }

    /**
     * Fills the mandatory deposit form fields.
     * Must be called inside {@code robot.interact()} so the FX thread processes each setText.
     * The form opens in TERM mode by default, so termMonths is always set.
     */
    private void fillRequiredFields(FxRobot robot,
                                    String name, String minAmount,
                                    String rate, String termMonths) {
        ((TextField) robot.lookup("#nameField").query()).setText(name);
        ((TextField) robot.lookup("#minAmountField").query()).setText(minAmount);
        ((TextField) robot.lookup("#interestRateField").query()).setText(rate);
        TextField termField = (TextField) robot.lookup("#termMonthsField").query();
        if (termField.isVisible()) {
            termField.setText(termMonths);
        }
    }

    /** Fires the "↺ Оновити" button inside the deposits pane. */
    private void refreshDepositsTable(FxRobot robot) {
        robot.interact(() -> {
            for (Node n : depositsRoot(robot).lookupAll("Button")) {
                if (n instanceof Button b && b.getText().contains("Оновити")) {
                    b.fire();
                    break;
                }
            }
        });
    }
}
