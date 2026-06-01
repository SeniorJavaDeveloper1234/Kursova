package ua.lpnu.deposits.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ua.lpnu.deposits.model.*;
import ua.lpnu.deposits.service.BankService;
import ua.lpnu.deposits.service.DepositService;
import ua.lpnu.deposits.util.AppContext;
import ua.lpnu.deposits.util.AppLogger;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the add/edit deposit popup window.
 * Contains no business logic; delegates to {@link DepositService}.
 */
public class DepositFormController implements Initializable {

    private static final AppLogger logger = AppLogger.getLogger(DepositFormController.class);

    @FXML private Label    formTitleLabel;
    @FXML private TextField nameField;
    @FXML private ComboBox<Bank>        bankComboBox;
    @FXML private ComboBox<String>      typeComboBox;
    @FXML private TextField currencyField;
    @FXML private TextField minAmountField;
    @FXML private TextField interestRateField;
    @FXML private Label     termLabel;
    @FXML private TextField termMonthsField;
    @FXML private Label     penaltyLabel;
    @FXML private TextField penaltyRateField;
    @FXML private Label     featuresLabel;

    private final DepositService depositService = AppContext.getInstance().getDepositService();
    private final BankService    bankService    = AppContext.getInstance().getBankService();

    private Deposit editingDeposit;
    private Runnable onSaveCallback;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadBanks();
        setupTypeComboBox();
        currencyField.setText("UAH");
    }

    /**
     * Pre-fills the form fields when editing an existing deposit.
     * Pass {@code null} to open an empty "Add" form.
     *
     * @param deposit the deposit to edit, or {@code null}
     */
    public void setDepositToEdit(Deposit deposit) {
        this.editingDeposit = deposit;
        if (deposit == null) {
            formTitleLabel.setText("Новий депозит");
            typeComboBox.getSelectionModel().selectFirst();
            return;
        }
        formTitleLabel.setText("Редагування: " + deposit.getName());
        nameField.setText(deposit.getName());
        currencyField.setText(deposit.getCurrency());
        minAmountField.setText(String.valueOf(deposit.getMinAmount()));
        interestRateField.setText(String.valueOf(deposit.getInterestRate()));
        termMonthsField.setText(deposit.getTermMonths() == 0 ? "" :
                String.valueOf(deposit.getTermMonths()));
        penaltyRateField.setText(String.valueOf(deposit.getPenaltyRate()));

        String typeLabel = switch (deposit.getType()) {
            case TERM    -> "Строковий";
            case SAVINGS -> "Ощадний";
            case DEMAND  -> "До запитання";
        };
        typeComboBox.getSelectionModel().select(typeLabel);

        bankComboBox.getItems().stream()
                .filter(b -> b.getId() == deposit.getBankId())
                .findFirst()
                .ifPresent(bankComboBox.getSelectionModel()::select);
    }

    /**
     * Registers a callback invoked after a successful save.
     *
     * @param callback runnable (typically refreshes the deposit list)
     */
    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    private void onSave() {
        if (!validate()) return;

        try {
            Deposit deposit = buildDeposit();
            if (editingDeposit == null) {
                depositService.createDeposit(deposit);
                AlertUtil.showInfo("Збережено", "Депозит успішно створено.");
            } else {
                depositService.updateDeposit(deposit);
                AlertUtil.showInfo("Збережено", "Депозит успішно оновлено.");
            }
            if (onSaveCallback != null) onSaveCallback.run();
            closeWindow();
        } catch (IllegalArgumentException e) {
            AlertUtil.showError("Помилка валідації", e.getMessage());
        } catch (SQLException e) {
            logger.error("Failed to save deposit", e);
            AlertUtil.showError("Помилка збереження",
                    "Не вдалося зберегти депозит:\n" + e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void loadBanks() {
        try {
            List<Bank> banks = bankService.getAllBanks();
            bankComboBox.setItems(FXCollections.observableArrayList(banks));
            bankComboBox.setConverter(new javafx.util.StringConverter<>() {
                @Override public String toString(Bank b) {
                    return b == null ? "" : b.getName();
                }
                @Override public Bank fromString(String s) { return null; }
            });
            if (!banks.isEmpty()) bankComboBox.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            logger.error("Failed to load banks for deposit form", e);
            AlertUtil.showError("Помилка", "Не вдалося завантажити список банків.");
        }
    }

    private void setupTypeComboBox() {
        typeComboBox.setItems(FXCollections.observableArrayList(
                "Строковий", "Ощадний", "До запитання"));
        typeComboBox.getSelectionModel().selectFirst();
        typeComboBox.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, selected) -> updateFieldsForType(selected));
        updateFieldsForType(typeComboBox.getValue());
    }

    private void updateFieldsForType(String typeLabel) {
        if (typeLabel == null) return;
        boolean isTerm    = "Строковий".equals(typeLabel);
        boolean isSavings = "Ощадний".equals(typeLabel);
        boolean isDemand  = "До запитання".equals(typeLabel);

        termLabel.setVisible(!isDemand);
        termMonthsField.setVisible(!isDemand);

        penaltyLabel.setVisible(isTerm);
        penaltyRateField.setVisible(isTerm);

        if (isTerm)    featuresLabel.setText("Дострокове зняття: Так (з штрафом)  |  Поповнення: Ні");
        if (isSavings) featuresLabel.setText("Дострокове зняття: Ні  |  Поповнення: Так");
        if (isDemand)  featuresLabel.setText("Дострокове зняття: Так  |  Поповнення: Так");
    }

    private boolean validate() {
        if (nameField.getText().isBlank()) {
            AlertUtil.showError("Помилка", "Введіть назву депозиту.");
            return false;
        }
        if (bankComboBox.getValue() == null) {
            AlertUtil.showError("Помилка", "Оберіть банк.");
            return false;
        }
        if (typeComboBox.getValue() == null) {
            AlertUtil.showError("Помилка", "Оберіть тип депозиту.");
            return false;
        }
        if (!isValidDouble(minAmountField.getText())) {
            AlertUtil.showError("Помилка", "Некоректне значення мінімальної суми.");
            return false;
        }
        if (!isValidDouble(interestRateField.getText()) ||
                Double.parseDouble(interestRateField.getText()) <= 0) {
            AlertUtil.showError("Помилка", "Ставка відсотків має бути позитивним числом.");
            return false;
        }
        boolean isDemand = "До запитання".equals(typeComboBox.getValue());
        if (!isDemand && !isValidInt(termMonthsField.getText())) {
            AlertUtil.showError("Помилка", "Некоректне значення терміну (місяці).");
            return false;
        }
        boolean isTerm = "Строковий".equals(typeComboBox.getValue());
        if (isTerm && !penaltyRateField.getText().isBlank()
                && !isValidDouble(penaltyRateField.getText())) {
            AlertUtil.showError("Помилка", "Некоректний відсоток штрафу.");
            return false;
        }
        return true;
    }

    private Deposit buildDeposit() {
        String typeSel    = typeComboBox.getValue();
        int bankId        = bankComboBox.getValue().getId();
        String name       = nameField.getText().trim();
        String currency   = currencyField.getText().isBlank() ? "UAH" : currencyField.getText().trim();
        double minAmount  = Double.parseDouble(minAmountField.getText());
        double rate       = Double.parseDouble(interestRateField.getText());
        int    term       = termMonthsField.isVisible() && !termMonthsField.getText().isBlank()
                            ? Integer.parseInt(termMonthsField.getText()) : 0;
        double penalty    = penaltyRateField.isVisible() && !penaltyRateField.getText().isBlank()
                            ? Double.parseDouble(penaltyRateField.getText()) : 0.0;
        int existingId    = editingDeposit != null ? editingDeposit.getId() : 0;

        return switch (typeSel) {
            case "Строковий"     -> new TermDeposit(existingId, bankId, name, currency,
                                        minAmount, rate, term, penalty);
            case "Ощадний"       -> new SavingsDeposit(existingId, bankId, name, currency,
                                        minAmount, rate, term);
            case "До запитання"  -> new DemandDeposit(existingId, bankId, name, currency,
                                        minAmount, rate);
            default -> throw new IllegalArgumentException("Невідомий тип: " + typeSel);
        };
    }

    private boolean isValidDouble(String text) {
        if (text == null || text.isBlank()) return false;
        try { Double.parseDouble(text); return true; }
        catch (NumberFormatException e) { return false; }
    }

    private boolean isValidInt(String text) {
        if (text == null || text.isBlank()) return false;
        try { Integer.parseInt(text); return true; }
        catch (NumberFormatException e) { return false; }
    }

    private void closeWindow() {
        ((Stage) nameField.getScene().getWindow()).close();
    }
}
