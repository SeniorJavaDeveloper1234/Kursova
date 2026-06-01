package ua.lpnu.deposits.ui;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ua.lpnu.deposits.model.Deposit;
import ua.lpnu.deposits.model.DepositType;
import ua.lpnu.deposits.service.BankService;
import ua.lpnu.deposits.service.DepositService;
import ua.lpnu.deposits.util.AppContext;
import ua.lpnu.deposits.util.AppLogger;
import ua.lpnu.deposits.util.UserSession;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for the Deposits tab — full CRUD with a sortable table.
 * Contains no business logic; delegates everything to {@link DepositService}.
 */
public class DepositListController implements Initializable {

    private static final AppLogger logger = AppLogger.getLogger(DepositListController.class);

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private TableView<Deposit>           depositTable;
    @FXML private TableColumn<Deposit, String>  nameColumn;
    @FXML private TableColumn<Deposit, String>  bankColumn;
    @FXML private TableColumn<Deposit, String>  typeColumn;
    @FXML private TableColumn<Deposit, String>  currencyColumn;
    @FXML private TableColumn<Deposit, Double>  minAmountColumn;
    @FXML private TableColumn<Deposit, Double>  interestRateColumn;
    @FXML private TableColumn<Deposit, Integer> termMonthsColumn;
    @FXML private TableColumn<Deposit, Boolean> canWithdrawColumn;
    @FXML private TableColumn<Deposit, Boolean> canReplenishColumn;
    @FXML private Label countLabel;

    private final DepositService depositService = AppContext.getInstance().getDepositService();
    private final BankService    bankService    = AppContext.getInstance().getBankService();
    private final Map<Integer, String> bankNames = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        setupSelectionBinding();
        loadBankNames();
        loadDeposits();
        applyRoleRestrictions();
    }

    private void setupColumns() {
        nameColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getName()));

        bankColumn.setCellValueFactory(c ->
                new SimpleStringProperty(
                        bankNames.getOrDefault(c.getValue().getBankId(), "—")));

        typeColumn.setCellValueFactory(c ->
                new SimpleStringProperty(typeLabel(c.getValue().getType())));

        currencyColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getCurrency()));

        minAmountColumn.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().getMinAmount()));
        minAmountColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%.2f", v));
            }
        });

        interestRateColumn.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().getInterestRate()));
        interestRateColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%.2f %%", v));
            }
        });

        termMonthsColumn.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().getTermMonths()));
        termMonthsColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(v == 0 ? "Не обмежений" : v + " міс.");
            }
        });

        canWithdrawColumn.setCellValueFactory(c ->
                new SimpleBooleanProperty(c.getValue().isCanWithdrawEarly()));
        canWithdrawColumn.setCellFactory(col -> booleanCell());

        canReplenishColumn.setCellValueFactory(c ->
                new SimpleBooleanProperty(c.getValue().isCanReplenish()));
        canReplenishColumn.setCellFactory(col -> booleanCell());

        depositTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private TableCell<Deposit, Boolean> booleanCell() {
        return new TableCell<>() {
            @Override protected void updateItem(Boolean v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v ? "Так" : "Ні");
                setStyle(v ? "-fx-text-fill: #2e7d32; -fx-font-weight: bold;"
                           : "-fx-text-fill: #c62828;");
            }
        };
    }

    private void setupSelectionBinding() {
        depositTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> {
                    editButton.setDisable(sel == null);
                    deleteButton.setDisable(sel == null);
                });
    }

    private void applyRoleRestrictions() {
        if (!UserSession.isAdmin()) {
            addButton.setVisible(false);
            addButton.setManaged(false);
            editButton.setVisible(false);
            editButton.setManaged(false);
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
        }
    }

    private void loadBankNames() {
        try {
            bankNames.clear();
            bankService.getAllBanks().forEach(b -> bankNames.put(b.getId(), b.getName()));
        } catch (SQLException e) {
            logger.error("Failed to load bank names for deposit list", e);
        }
    }

    /** Refreshes the table from the database. Also called by the form after save. */
    public void loadDeposits() {
        try {
            List<Deposit> deposits = depositService.getAllDeposits();
            depositTable.setItems(FXCollections.observableArrayList(deposits));
            countLabel.setText("Записів: " + deposits.size());
        } catch (SQLException e) {
            logger.error("Failed to load deposits", e);
            AlertUtil.showError("Помилка завантаження",
                    "Не вдалося завантажити депозити:\n" + e.getMessage());
        }
    }

    @FXML
    private void onAdd() {
        openForm(null);
    }

    @FXML
    private void onEdit() {
        Deposit selected = depositTable.getSelectionModel().getSelectedItem();
        if (selected != null) openForm(selected);
    }

    @FXML
    private void onDelete() {
        Deposit selected = depositTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (!AlertUtil.showConfirm("Підтвердження",
                "Видалити депозит «" + selected.getName() + "»?")) return;

        try {
            depositService.deleteDeposit(selected.getId());
            loadDeposits();
        } catch (SQLException e) {
            logger.error("Failed to delete deposit id=" + selected.getId(), e);
            AlertUtil.showError("Помилка видалення",
                    "Не вдалося видалити депозит:\n" + e.getMessage());
        }
    }

    @FXML
    private void onRefresh() {
        loadBankNames();
        loadDeposits();
    }

    private void openForm(Deposit deposit) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/DepositFormView.fxml"));
            Scene scene = new Scene(loader.load(), 520, 500);
            scene.getStylesheets().add(
                    getClass().getResource("/css/style.css").toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(deposit == null ? "Додати депозит" : "Редагувати депозит");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            DepositFormController formCtrl = loader.getController();
            formCtrl.setDepositToEdit(deposit);
            formCtrl.setOnSaveCallback(this::loadDeposits);

            stage.showAndWait();
        } catch (Exception e) {
            logger.error("Failed to open deposit form", e);
            AlertUtil.showError("Помилка", "Не вдалося відкрити форму:\n" + e.getMessage());
        }
    }

    private String typeLabel(DepositType type) {
        return switch (type) {
            case TERM    -> "Строковий";
            case SAVINGS -> "Ощадний";
            case DEMAND  -> "До запитання";
        };
    }
}
