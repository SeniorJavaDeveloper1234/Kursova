package ua.lpnu.deposits.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import ua.lpnu.deposits.model.Client;
import ua.lpnu.deposits.model.ClientDeposit;
import ua.lpnu.deposits.model.ClientDepositDetail;
import ua.lpnu.deposits.util.AppContext;
import ua.lpnu.deposits.util.DatabaseConnection;
import ua.lpnu.deposits.util.UserSession;

import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TestFX GUI automation tests for the Clients tab (ClientListView).
 * Runs headless via Monocle — same infrastructure as {@link BankListViewTest}.
 *
 * <p>All node lookups are scoped to {@code #clientListRoot} (the outer VBox of
 * ClientListView.fxml) to avoid fx:id collisions with identically-named nodes
 * in BankListView and other views that co-exist in the StackPane.</p>
 *
 * <p>Test 5 creates a transient {@code client_deposit} record for its own use and
 * deletes it on completion, so the DB is left in the same state as before.</p>
 */
@ExtendWith(ApplicationExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientListViewTest {

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

    /** Navigate to the Clients tab and clear all UI state before every test. */
    @BeforeEach
    void goToClientsAndReset(FxRobot robot) {
        robot.clickOn("#navClients");
        robot.interact(() -> {
            Node root = clientsRoot(robot);
            ((TextField) root.lookup("#searchField")).clear();
            ((TableView<?>) root.lookup("#clientTable")).getSelectionModel().clearSelection();
            ((TableView<?>) root.lookup("#depositTable")).getSelectionModel().clearSelection();
        });
    }

    // ─── Test 1: upper table is populated ────────────────────────────────────

    @Test
    @Order(1)
    void clientListShowsTableTest(FxRobot robot) {
        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<Client> table =
                    (TableView<Client>) clientsRoot(robot).lookup("#clientTable");
            assertFalse(table.getItems().isEmpty(),
                    "Client table must have rows after DataSeeder runs");
        });
    }

    // ─── Test 2: selecting a client populates the deposit panel ──────────────

    @Test
    @Order(2)
    void selectClientShowsDepositsTest(FxRobot robot) {
        // Select the first seeded client (id 1–5 all have deposits).
        // We search by id because SQLite sorts ASCII "t" before Ukrainian letters,
        // so the manually-added test client "tt" (id=6, no deposits) may appear at index 0.
        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<Client> ct =
                    (TableView<Client>) clientsRoot(robot).lookup("#clientTable");
            ct.getSelectionModel().select(indexOfSeededClient(robot));
        });

        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<ClientDepositDetail> dt =
                    (TableView<ClientDepositDetail>) clientsRoot(robot).lookup("#depositTable");
            assertFalse(dt.getItems().isEmpty(),
                    "Deposit table must be populated after selecting a client that has deposits");
        });
    }

    // ─── Test 3: clearing client selection empties the deposit table ──────────

    @Test
    @Order(3)
    void deselectClientClearsDepositsTest(FxRobot robot) {
        // Select a seeded client (has deposits) to populate the deposit table
        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<Client> ct =
                    (TableView<Client>) clientsRoot(robot).lookup("#clientTable");
            ct.getSelectionModel().select(indexOfSeededClient(robot));
        });
        // Verify deposits were loaded before clearing
        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<ClientDepositDetail> dt =
                    (TableView<ClientDepositDetail>) clientsRoot(robot).lookup("#depositTable");
            assertFalse(dt.getItems().isEmpty(),
                    "Pre-condition: deposit table must be non-empty before deselecting");
        });

        // Clear the client selection
        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<Client> ct =
                    (TableView<Client>) clientsRoot(robot).lookup("#clientTable");
            ct.getSelectionModel().clearSelection();
        });

        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<ClientDepositDetail> dt =
                    (TableView<ClientDepositDetail>) clientsRoot(robot).lookup("#depositTable");
            assertTrue(dt.getItems().isEmpty(),
                    "Deposit table must be empty after client selection is cleared");
        });
    }

    // ─── Test 4: close-deposit button is disabled when nothing is selected ────

    @Test
    @Order(4)
    void closeDepositButtonDisabledByDefaultTest(FxRobot robot) {
        robot.interact(() -> {
            Button btn = (Button) clientsRoot(robot).lookup("#closeDepositButton");
            assertTrue(btn.isDisabled(),
                    "Close deposit button must be disabled when no deposit row is selected");
        });
    }

    // ─── Test 5: closing an ACTIVE deposit changes its status to CLOSED ───────

    /**
     * Creates a temporary ACTIVE deposit for client 1 via the service, closes it
     * through the UI, verifies the status became {@code CLOSED}, then deletes the
     * temporary record to keep the DB clean for subsequent runs.
     */
    @Test
    @Order(5)
    void closeDepositChangesStatusTest(FxRobot robot) throws Exception {
        // ── Arrange: insert a fresh ACTIVE deposit (client=1, deposit=3 DEMAND min=500) ──
        int[] tempId = {0};
        robot.interact(() -> {
            try {
                ClientDeposit cd = AppContext.getInstance()
                        .getDepositService()
                        .openDeposit(1, 3, 2000.0);
                tempId[0] = cd.getId();
            } catch (Exception e) {
                throw new RuntimeException("openDeposit failed", e);
            }
        });

        // ── Select client with id=1 in the upper table ──
        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<Client> ct =
                    (TableView<Client>) clientsRoot(robot).lookup("#clientTable");
            for (int i = 0; i < ct.getItems().size(); i++) {
                if (ct.getItems().get(i).getId() == 1) {
                    ct.getSelectionModel().select(i);
                    break;
                }
            }
        });

        // ── Select the newly created deposit row in the lower table ──
        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<ClientDepositDetail> dt =
                    (TableView<ClientDepositDetail>) clientsRoot(robot).lookup("#depositTable");
            for (int i = 0; i < dt.getItems().size(); i++) {
                if (dt.getItems().get(i).getId() == tempId[0]) {
                    dt.getSelectionModel().select(i);
                    break;
                }
            }
        });

        // ── Click close button — this opens a confirmation dialog ──
        robot.clickOn("#closeDepositButton");
        // ── Confirm the dialog ──
        robot.clickOn("OK");

        // ── Assert: the row now has status CLOSED ──
        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<ClientDepositDetail> dt =
                    (TableView<ClientDepositDetail>) clientsRoot(robot).lookup("#depositTable");
            ClientDepositDetail closed = dt.getItems().stream()
                    .filter(d -> d.getId() == tempId[0])
                    .findFirst()
                    .orElseThrow(() -> new AssertionError(
                            "Closed deposit (id=" + tempId[0] + ") not found in deposit table"));
            assertEquals("CLOSED", closed.getStatus(),
                    "Deposit status must be CLOSED after clicking '🔒 Закрити депозит'");
        });

        // ── Cleanup: remove the temp record so DB state is unchanged ──
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM client_deposits WHERE id = ?")) {
            ps.setInt(1, tempId[0]);
            ps.executeUpdate();
        }
    }

    // ─── Test 6: search field filters the client list ────────────────────────

    @Test
    @Order(6)
    void clientSearchFilterTest(FxRobot robot) {
        // Set text and fire the "Знайти" button, both scoped to clientsRoot
        robot.interact(() -> {
            ((TextField) clientsRoot(robot).lookup("#searchField")).setText("Коваленко");
        });
        robot.interact(() -> {
            for (Node n : clientsRoot(robot).lookupAll("Button")) {
                if (n instanceof Button b && b.getText().contains("Знайти")) {
                    b.fire();
                    break;
                }
            }
        });

        robot.interact(() -> {
            @SuppressWarnings("unchecked")
            TableView<Client> table =
                    (TableView<Client>) clientsRoot(robot).lookup("#clientTable");
            assertFalse(table.getItems().isEmpty(),
                    "Search for 'Коваленко' must return at least one result");
            table.getItems().forEach(c ->
                    assertTrue(c.getLastName().contains("Коваленко"),
                            "Every search result must match 'Коваленко', got: " + c.getLastName()));
        });
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    /**
     * Returns the root VBox of ClientListView by looking up its CSS id.
     * Scopes all subsequent lookups to the clients pane only, avoiding
     * fx:id collisions with identically-named nodes in other views.
     */
    private Node clientsRoot(FxRobot robot) {
        return robot.lookup("#clientListRoot").query();
    }

    /**
     * Returns the table index of the first DataSeeder client (id 1–5).
     * These clients all have seeded deposits and are the reliable targets for
     * tests that need a populated deposit panel.
     *
     * <p>A plain {@code select(0)} is unreliable because SQLite compares text
     * bytes: the ASCII test-client "tt" (id=6, no deposits) sorts before
     * Ukrainian-named clients and may appear at index 0.</p>
     */
    @SuppressWarnings("unchecked")
    private int indexOfSeededClient(FxRobot robot) {
        TableView<Client> ct =
                (TableView<Client>) clientsRoot(robot).lookup("#clientTable");
        for (int i = 0; i < ct.getItems().size(); i++) {
            if (ct.getItems().get(i).getId() <= 5) {
                return i;
            }
        }
        throw new AssertionError("No seeded client (id 1-5) found in the client table");
    }
}
