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
import ua.lpnu.deposits.model.Bank;
import ua.lpnu.deposits.service.BankService;
import ua.lpnu.deposits.util.AppContext;
import ua.lpnu.deposits.util.AppLogger;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the Banks tab — full CRUD with a sortable table and an
 * inline dialog for add/edit (no separate FXML needed given the simple model).
 * Contains no business logic; delegates to {@link BankService}.
 */
public class BankListController implements Initializable {

    private static final AppLogger logger = AppLogger.getLogger(BankListController.class);

    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private TextField searchField;

    @FXML private TableView<Bank>             bankTable;
    @FXML private TableColumn<Bank, Integer>  idColumn;
    @FXML private TableColumn<Bank, String>   nameColumn;
    @FXML private TableColumn<Bank, Double>   ratingColumn;
    @FXML private Label countLabel;

    private final BankService bankService = AppContext.getInstance().getBankService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        setupSelectionBinding();
        loadBanks();
    }

    private void setupColumns() {
        idColumn.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().getId()).asObject());

        nameColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getName()));

        ratingColumn.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().getRating()).asObject());
        ratingColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%.1f", v));
            }
        });

        bankTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void setupSelectionBinding() {
        bankTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> {
                    editButton.setDisable(sel == null);
                    deleteButton.setDisable(sel == null);
                });
    }

    private void loadBanks() {
        try {
            List<Bank> banks = bankService.getAllBanks();
            bankTable.setItems(FXCollections.observableArrayList(banks));
            countLabel.setText("Банків: " + banks.size());
        } catch (SQLException e) {
            logger.error("Failed to load banks", e);
            AlertUtil.showError("Помилка завантаження",
                    "Не вдалося завантажити список банків:\n" + e.getMessage());
        }
    }

    @FXML
    private void onAdd() {
        showBankDialog(null).ifPresent(bank -> {
            try {
                bankService.createBank(bank);
                loadBanks();
            } catch (IllegalArgumentException e) {
                AlertUtil.showError("Помилка валідації", e.getMessage());
            } catch (SQLException e) {
                logger.error("Failed to create bank", e);
                AlertUtil.showError("Помилка збереження",
                        "Не вдалося зберегти банк:\n" + e.getMessage());
            }
        });
    }

    @FXML
    private void onEdit() {
        Bank selected = bankTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        showBankDialog(selected).ifPresent(updated -> {
            try {
                bankService.updateBank(updated);
                loadBanks();
            } catch (IllegalArgumentException e) {
                AlertUtil.showError("Помилка валідації", e.getMessage());
            } catch (SQLException e) {
                logger.error("Failed to update bank id=" + selected.getId(), e);
                AlertUtil.showError("Помилка оновлення",
                        "Не вдалося оновити банк:\n" + e.getMessage());
            }
        });
    }

    @FXML
    private void onDelete() {
        Bank selected = bankTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (!AlertUtil.showConfirm("Підтвердження",
                "Видалити банк «" + selected.getName() + "»?")) return;
        try {
            bankService.deleteBank(selected.getId());
            loadBanks();
        } catch (SQLException e) {
            logger.error("Failed to delete bank id=" + selected.getId(), e);
            AlertUtil.showError("Помилка видалення",
                    "Не вдалося видалити банк:\n" + e.getMessage());
        }
    }

    @FXML
    private void onRefresh() {
        searchField.clear();
        loadBanks();
    }

    @FXML
    private void onSearch() {
        String query = searchField.getText().trim();
        try {
            List<Bank> banks = bankService.searchByName(query);
            bankTable.setItems(FXCollections.observableArrayList(banks));
            countLabel.setText("Знайдено: " + banks.size());
        } catch (SQLException e) {
            logger.error("Failed to search banks", e);
            AlertUtil.showError("Помилка пошуку",
                    "Не вдалося виконати пошук:\n" + e.getMessage());
        }
    }

    private Optional<Bank> showBankDialog(Bank bankToEdit) {
        boolean isEdit = bankToEdit != null;

        Dialog<Bank> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Редагувати банк" : "Додати банк");
        dialog.setHeaderText(isEdit ? "Оновіть дані банку" : "Введіть дані нового банку");

        ButtonType saveType = new ButtonType("Зберегти", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        var css = getClass().getResource("/css/style.css");
        if (css != null) dialog.getDialogPane().getStylesheets().add(css.toExternalForm());

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField nameField = new TextField(isEdit ? bankToEdit.getName() : "");
        nameField.setPromptText("Назва банку");
        nameField.setPrefWidth(240);

        Spinner<Double> ratingSpinner = new Spinner<>(0.0, 10.0,
                isEdit ? bankToEdit.getRating() : 5.0, 0.5);
        ratingSpinner.setEditable(true);
        ratingSpinner.setPrefWidth(120);

        grid.add(new Label("Назва:"),        0, 0);
        grid.add(nameField,                  1, 0);
        grid.add(new Label("Рейтинг (0–10):"), 0, 1);
        grid.add(ratingSpinner,              1, 1);
        dialog.getDialogPane().setContent(grid);

        dialog.setOnShown(e -> nameField.requestFocus());

        dialog.setResultConverter(btn -> {
            if (btn != saveType) return null;
            String name   = nameField.getText().trim();
            double rating = ratingSpinner.getValue();
            return isEdit
                    ? new Bank(bankToEdit.getId(), name, rating)
                    : new Bank(name, rating);
        });

        return dialog.showAndWait();
    }
}
