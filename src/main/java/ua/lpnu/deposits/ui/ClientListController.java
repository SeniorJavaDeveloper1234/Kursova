package ua.lpnu.deposits.ui;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import ua.lpnu.deposits.model.Client;
import ua.lpnu.deposits.model.ClientDepositDetail;
import ua.lpnu.deposits.model.DepositType;
import ua.lpnu.deposits.service.DepositService;
import ua.lpnu.deposits.util.AppContext;
import ua.lpnu.deposits.util.AppLogger;
import ua.lpnu.deposits.util.UserSession;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the Clients tab — full CRUD with an inline dialog for add/edit,
 * plus a deposit details panel that loads when a client row is selected.
 * Contains no business logic; delegates everything to {@link DepositService}.
 */
public class ClientListController implements Initializable {

    private static final AppLogger logger = AppLogger.getLogger(ClientListController.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    /* ── Client table ──────────────────────────────────────── */
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private TextField searchField;

    @FXML private TableView<Client>             clientTable;
    @FXML private TableColumn<Client, Integer>  idColumn;
    @FXML private TableColumn<Client, String>   lastNameColumn;
    @FXML private TableColumn<Client, String>   firstNameColumn;
    @FXML private TableColumn<Client, String>   emailColumn;
    @FXML private Label countLabel;

    /* ── Deposit details panel ─────────────────────────────── */
    @FXML private TableView<ClientDepositDetail>            depositTable;
    @FXML private TableColumn<ClientDepositDetail, Integer> cdIdColumn;
    @FXML private TableColumn<ClientDepositDetail, String>  cdNameColumn;
    @FXML private TableColumn<ClientDepositDetail, String>  cdBankColumn;
    @FXML private TableColumn<ClientDepositDetail, String>  cdTypeColumn;
    @FXML private TableColumn<ClientDepositDetail, String>  cdAmountColumn;
    @FXML private TableColumn<ClientDepositDetail, String>  cdOpenedColumn;
    @FXML private TableColumn<ClientDepositDetail, String>  cdStatusColumn;
    @FXML private TableColumn<ClientDepositDetail, String>  cdProfitColumn;
    @FXML private Button closeDepositButton;

    /** Creates a new {@code ClientListController} (instantiated by the JavaFX FXML loader). */
    public ClientListController() {}

    private final DepositService depositService = AppContext.getInstance().getDepositService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupClientColumns();
        setupDepositColumns();
        setupClientSelectionBinding();
        setupDepositSelectionBinding();
        loadClients();
        applyRoleRestrictions();
    }

    /* ── Setup ─────────────────────────────────────────────── */

    private void setupClientColumns() {
        idColumn.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().getId()).asObject());
        lastNameColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getLastName()));
        firstNameColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getFirstName()));
        emailColumn.setCellValueFactory(c -> {
            String email = c.getValue().getEmail();
            return new SimpleStringProperty(email != null ? email : "—");
        });
        clientTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void setupDepositColumns() {
        cdIdColumn.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().getId()).asObject());
        cdNameColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDepositName()));
        cdBankColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getBankName()));
        cdTypeColumn.setCellValueFactory(c ->
                new SimpleStringProperty(translateType(c.getValue().getDepositType())));
        cdAmountColumn.setCellValueFactory(c ->
                new SimpleStringProperty(formatAmount(c.getValue().getAmount())));
        cdOpenedColumn.setCellValueFactory(c ->
                new SimpleStringProperty(formatDate(c.getValue().getOpenedAt())));
        cdStatusColumn.setCellValueFactory(c ->
                new SimpleStringProperty(translateStatus(c.getValue().getStatus())));
        cdProfitColumn.setCellValueFactory(c ->
                new SimpleStringProperty(formatAmount(c.getValue().getExpectedProfit())));
        depositTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void setupClientSelectionBinding() {
        clientTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, selected) -> {
                    boolean hasSelection = selected != null;
                    editButton.setDisable(!hasSelection);
                    deleteButton.setDisable(!hasSelection);
                    if (hasSelection) {
                        loadDepositsForClient(selected);
                    } else {
                        depositTable.getItems().clear();
                        closeDepositButton.setDisable(true);
                    }
                });
    }

    private void setupDepositSelectionBinding() {
        depositTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) ->
                        closeDepositButton.setDisable(
                                sel == null || !"ACTIVE".equals(sel.getStatus())));
    }

    private void applyRoleRestrictions() {
        if (!UserSession.isAdmin()) {
            addButton.setVisible(false);
            addButton.setManaged(false);
            editButton.setVisible(false);
            editButton.setManaged(false);
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
            closeDepositButton.setVisible(false);
            closeDepositButton.setManaged(false);
        }
    }

    /* ── Data loading ──────────────────────────────────────── */

    private void loadClients() {
        try {
            List<Client> clients = depositService.getAllClients();
            clientTable.setItems(FXCollections.observableArrayList(clients));
            countLabel.setText("Клієнтів: " + clients.size());
        } catch (SQLException e) {
            logger.error("Failed to load clients", e);
            AlertUtil.showError("Помилка завантаження",
                    "Не вдалося завантажити список клієнтів:\n" + e.getMessage());
        }
    }

    private void loadDepositsForClient(Client client) {
        try {
            List<ClientDepositDetail> details =
                    depositService.getClientDepositDetails(client.getId());
            depositTable.setItems(FXCollections.observableArrayList(details));
        } catch (SQLException e) {
            logger.error("Failed to load deposits for clientId=" + client.getId(), e);
            AlertUtil.showError("Помилка",
                    "Не вдалося завантажити депозити клієнта:\n" + e.getMessage());
        }
    }

    /* ── CRUD handlers ─────────────────────────────────────── */

    @FXML
    private void onAdd() {
        showClientDialog(null).ifPresent(client -> {
            try {
                depositService.createClient(client);
                loadClients();
            } catch (IllegalArgumentException e) {
                AlertUtil.showError("Помилка валідації", e.getMessage());
            } catch (SQLException e) {
                logger.error("Failed to create client", e);
                AlertUtil.showError("Помилка збереження",
                        "Не вдалося зберегти клієнта:\n" + e.getMessage());
            }
        });
    }

    @FXML
    private void onEdit() {
        Client selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        showClientDialog(selected).ifPresent(updated -> {
            try {
                depositService.updateClient(updated);
                loadClients();
            } catch (IllegalArgumentException e) {
                AlertUtil.showError("Помилка валідації", e.getMessage());
            } catch (SQLException e) {
                logger.error("Failed to update client id=" + selected.getId(), e);
                AlertUtil.showError("Помилка оновлення",
                        "Не вдалося оновити клієнта:\n" + e.getMessage());
            }
        });
    }

    @FXML
    private void onDelete() {
        Client selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (!AlertUtil.showConfirm("Підтвердження",
                "Видалити клієнта «" + selected.getFullName() + "»?")) return;
        try {
            depositService.deleteClient(selected.getId());
            depositTable.getItems().clear();
            loadClients();
        } catch (SQLException e) {
            logger.error("Failed to delete client id=" + selected.getId(), e);
            AlertUtil.showError("Помилка видалення",
                    "Не вдалося видалити клієнта:\n" + e.getMessage());
        }
    }

    @FXML
    private void onRefresh() {
        searchField.clear();
        depositTable.getItems().clear();
        loadClients();
    }

    @FXML
    private void onSearch() {
        String query = searchField.getText().trim();
        try {
            List<Client> clients = depositService.searchClients(query);
            clientTable.setItems(FXCollections.observableArrayList(clients));
            countLabel.setText("Знайдено: " + clients.size());
        } catch (SQLException e) {
            logger.error("Failed to search clients", e);
            AlertUtil.showError("Помилка пошуку",
                    "Не вдалося виконати пошук:\n" + e.getMessage());
        }
    }

    @FXML
    private void onCloseDeposit() {
        ClientDepositDetail selected = depositTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (!AlertUtil.showConfirm("Підтвердження",
                "Закрити депозит «" + selected.getDepositName() + "» ("
                        + selected.getBankName() + ")?")) return;
        try {
            depositService.closeDeposit(selected.getId());
            Client client = clientTable.getSelectionModel().getSelectedItem();
            if (client != null) loadDepositsForClient(client);
        } catch (SQLException e) {
            logger.error("Failed to close deposit id=" + selected.getId(), e);
            AlertUtil.showError("Помилка",
                    "Не вдалося закрити депозит:\n" + e.getMessage());
        }
    }

    /* ── Helpers ───────────────────────────────────────────── */

    private String translateType(DepositType type) {
        if (type == null) return "—";
        return switch (type) {
            case TERM    -> "Строковий";
            case SAVINGS -> "Ощадний";
            case DEMAND  -> "До запитання";
        };
    }

    private String translateStatus(String status) {
        if (status == null) return "—";
        return switch (status) {
            case "ACTIVE" -> "Активний";
            case "CLOSED" -> "Закритий";
            default       -> status;
        };
    }

    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isBlank()) return "—";
        try {
            return LocalDate.parse(isoDate.substring(0, 10)).format(DATE_FMT);
        } catch (Exception e) {
            return isoDate;
        }
    }

    private String formatAmount(double amount) {
        return String.format("%,.2f", amount);
    }

    private Optional<Client> showClientDialog(Client clientToEdit) {
        boolean isEdit = clientToEdit != null;

        Dialog<Client> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Редагувати клієнта" : "Додати клієнта");
        dialog.setHeaderText(isEdit ? "Оновіть дані клієнта" : "Введіть дані нового клієнта");

        ButtonType saveType = new ButtonType("Зберегти", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        var css = getClass().getResource("/css/style.css");
        if (css != null) dialog.getDialogPane().getStylesheets().add(css.toExternalForm());

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField firstNameField = new TextField(isEdit ? clientToEdit.getFirstName() : "");
        firstNameField.setPromptText("Ім'я");
        firstNameField.setPrefWidth(240);

        TextField lastNameField = new TextField(isEdit ? clientToEdit.getLastName() : "");
        lastNameField.setPromptText("Прізвище");
        lastNameField.setPrefWidth(240);

        TextField emailField = new TextField(
                isEdit && clientToEdit.getEmail() != null ? clientToEdit.getEmail() : "");
        emailField.setPromptText("example@email.ua");
        emailField.setPrefWidth(240);

        grid.add(new Label("Ім'я:"),      0, 0);
        grid.add(firstNameField,           1, 0);
        grid.add(new Label("Прізвище:"),  0, 1);
        grid.add(lastNameField,            1, 1);
        grid.add(new Label("Email:"),      0, 2);
        grid.add(emailField,               1, 2);
        dialog.getDialogPane().setContent(grid);

        dialog.setOnShown(e -> firstNameField.requestFocus());

        dialog.setResultConverter(btn -> {
            if (btn != saveType) return null;
            String firstName = firstNameField.getText().trim();
            String lastName  = lastNameField.getText().trim();
            String email     = emailField.getText().trim();
            String emailVal  = email.isEmpty() ? null : email;
            return isEdit
                    ? new Client(clientToEdit.getId(), firstName, lastName, emailVal)
                    : new Client(firstName, lastName, emailVal);
        });

        return dialog.showAndWait();
    }
}
