package ua.lpnu.deposits.ui;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import ua.lpnu.deposits.model.Deposit;
import ua.lpnu.deposits.model.DepositType;
import ua.lpnu.deposits.service.BankService;
import ua.lpnu.deposits.service.DepositFilter;
import ua.lpnu.deposits.service.DepositService;
import ua.lpnu.deposits.service.SearchFilterService;
import ua.lpnu.deposits.service.SearchFilterService.SortField;
import ua.lpnu.deposits.util.AppContext;
import ua.lpnu.deposits.util.AppLogger;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for the Search & Filter tab.
 * Pulls a fresh deposit list on every search, then applies in-memory filters
 * and sorting via {@link SearchFilterService}.
 * Contains no business logic beyond composing the filter and presenting results.
 */
public class SearchFilterController implements Initializable {

    private static final AppLogger logger = AppLogger.getLogger(SearchFilterController.class);

    /* ---- filter controls ---- */
    @FXML private ComboBox<String>  typeFilterCombo;
    @FXML private TextField         currencyFilterField;
    @FXML private TextField         minRateField;
    @FXML private TextField         maxRateField;
    @FXML private TextField         minAmountFromField;
    @FXML private TextField         maxAmountToField;
    @FXML private TextField         minTermField;
    @FXML private TextField         maxTermField;
    @FXML private CheckBox          withdrawCheck;
    @FXML private CheckBox          replenishCheck;
    @FXML private ComboBox<String>  sortFieldCombo;
    @FXML private RadioButton       ascRadio;
    @FXML private RadioButton       descRadio;

    /* ---- results table ---- */
    @FXML private TableView<Deposit>            resultsTable;
    @FXML private TableColumn<Deposit, String>  rNameColumn;
    @FXML private TableColumn<Deposit, String>  rBankColumn;
    @FXML private TableColumn<Deposit, String>  rTypeColumn;
    @FXML private TableColumn<Deposit, String>  rCurrencyColumn;
    @FXML private TableColumn<Deposit, Double>  rMinAmtColumn;
    @FXML private TableColumn<Deposit, Double>  rRateColumn;
    @FXML private TableColumn<Deposit, Integer> rTermColumn;
    @FXML private TableColumn<Deposit, Boolean> rWithdrawColumn;
    @FXML private TableColumn<Deposit, Boolean> rReplenishColumn;
    @FXML private Label resultCountLabel;

    private final DepositService      depositService      = AppContext.getInstance().getDepositService();
    private final BankService         bankService         = AppContext.getInstance().getBankService();
    private final SearchFilterService searchFilterService = AppContext.getInstance().getSearchFilterService();

    private final Map<Integer, String> bankNames = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadBankNames();
        setupTypeCombo();
        setupSortCombo();
        setupSortToggle();
        setupResultsTable();
    }

    // -------------------------------------------------------------------------
    // FXML handlers
    // -------------------------------------------------------------------------

    @FXML
    private void onApply() {
        try {
            List<Deposit> all = depositService.getAllDeposits();
            List<Deposit> filtered = searchFilterService.applyFilters(all, buildFilter());
            List<Deposit> sorted   = searchFilterService.sort(filtered, selectedSortField(),
                    ascRadio.isSelected());
            resultsTable.setItems(FXCollections.observableArrayList(sorted));
            resultCountLabel.setText("Знайдено: " + sorted.size() + " з " + all.size());
        } catch (IllegalArgumentException e) {
            AlertUtil.showError("Помилка фільтру", e.getMessage());
        } catch (SQLException e) {
            logger.error("Search failed", e);
            AlertUtil.showError("Помилка пошуку",
                    "Не вдалося завантажити дані:\n" + e.getMessage());
        }
    }

    @FXML
    private void onReset() {
        typeFilterCombo.getSelectionModel().selectFirst();
        currencyFilterField.clear();
        minRateField.clear();
        maxRateField.clear();
        minAmountFromField.clear();
        maxAmountToField.clear();
        minTermField.clear();
        maxTermField.clear();
        withdrawCheck.setSelected(false);
        replenishCheck.setSelected(false);
        sortFieldCombo.getSelectionModel().selectFirst();
        ascRadio.setSelected(true);
        resultsTable.getItems().clear();
        resultCountLabel.setText("");
    }

    // -------------------------------------------------------------------------
    // Initialisation helpers
    // -------------------------------------------------------------------------

    private void loadBankNames() {
        try {
            bankNames.clear();
            bankService.getAllBanks().forEach(b -> bankNames.put(b.getId(), b.getName()));
        } catch (SQLException e) {
            logger.error("Failed to load bank names for search view", e);
        }
    }

    private void setupTypeCombo() {
        typeFilterCombo.setItems(FXCollections.observableArrayList(
                "Всі типи", "Строковий", "Ощадний", "До запитання"));
        typeFilterCombo.getSelectionModel().selectFirst();
    }

    private void setupSortCombo() {
        sortFieldCombo.setItems(FXCollections.observableArrayList(
                "Назва", "Ставка %", "Мін. сума", "Термін"));
        sortFieldCombo.getSelectionModel().selectFirst();
    }

    private void setupSortToggle() {
        ToggleGroup group = new ToggleGroup();
        ascRadio.setToggleGroup(group);
        descRadio.setToggleGroup(group);
        ascRadio.setSelected(true);
    }

    private void setupResultsTable() {
        rNameColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getName()));
        rBankColumn.setCellValueFactory(c ->
                new SimpleStringProperty(
                        bankNames.getOrDefault(c.getValue().getBankId(), "—")));
        rTypeColumn.setCellValueFactory(c ->
                new SimpleStringProperty(typeLabel(c.getValue().getType())));
        rCurrencyColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getCurrency()));

        rMinAmtColumn.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().getMinAmount()));
        rMinAmtColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%.2f", v));
            }
        });

        rRateColumn.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().getInterestRate()));
        rRateColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%.2f %%", v));
            }
        });

        rTermColumn.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().getTermMonths()));
        rTermColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(v == 0 ? "—" : v + " міс.");
            }
        });

        rWithdrawColumn.setCellValueFactory(c ->
                new SimpleBooleanProperty(c.getValue().isCanWithdrawEarly()));
        rWithdrawColumn.setCellFactory(col -> booleanCell());

        rReplenishColumn.setCellValueFactory(c ->
                new SimpleBooleanProperty(c.getValue().isCanReplenish()));
        rReplenishColumn.setCellFactory(col -> booleanCell());

        resultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    // -------------------------------------------------------------------------
    // Filter / sort construction
    // -------------------------------------------------------------------------

    private DepositFilter buildFilter() {
        DepositFilter.Builder b = DepositFilter.builder();

        String typeSel = typeFilterCombo.getValue();
        if (typeSel != null && !"Всі типи".equals(typeSel)) {
            b.type(switch (typeSel) {
                case "Строковий"    -> DepositType.TERM;
                case "Ощадний"      -> DepositType.SAVINGS;
                case "До запитання" -> DepositType.DEMAND;
                default             -> throw new IllegalArgumentException("Unknown type: " + typeSel);
            });
        }

        String currency = currencyFilterField.getText().trim();
        if (!currency.isEmpty()) b.currency(currency);

        if (isDouble(minRateField) && isDouble(maxRateField))
            b.interestRateRange(toDouble(minRateField), toDouble(maxRateField));

        if (isDouble(minAmountFromField) && isDouble(maxAmountToField))
            b.minAmountRange(toDouble(minAmountFromField), toDouble(maxAmountToField));

        if (isInt(minTermField) && isInt(maxTermField))
            b.termMonthsRange(toInt(minTermField), toInt(maxTermField));

        if (withdrawCheck.isSelected())  b.canWithdrawEarly(true);
        if (replenishCheck.isSelected()) b.canReplenish(true);

        return b.build();
    }

    private SortField selectedSortField() {
        return switch (sortFieldCombo.getValue()) {
            case "Ставка %"  -> SortField.INTEREST_RATE;
            case "Мін. сума" -> SortField.MIN_AMOUNT;
            case "Термін"    -> SortField.TERM_MONTHS;
            default           -> SortField.NAME;
        };
    }

    // -------------------------------------------------------------------------
    // Small helpers
    // -------------------------------------------------------------------------

    private boolean isDouble(TextField f) {
        if (f.getText().isBlank()) return false;
        try { Double.parseDouble(f.getText()); return true; }
        catch (NumberFormatException e) { return false; }
    }

    private boolean isInt(TextField f) {
        if (f.getText().isBlank()) return false;
        try { Integer.parseInt(f.getText()); return true; }
        catch (NumberFormatException e) { return false; }
    }

    private double toDouble(TextField f) { return Double.parseDouble(f.getText()); }
    private int    toInt(TextField f)    { return Integer.parseInt(f.getText());    }

    private String typeLabel(DepositType type) {
        return switch (type) {
            case TERM    -> "Строковий";
            case SAVINGS -> "Ощадний";
            case DEMAND  -> "До запитання";
        };
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
}
