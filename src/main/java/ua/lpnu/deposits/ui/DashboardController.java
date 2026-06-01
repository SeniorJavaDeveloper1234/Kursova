package ua.lpnu.deposits.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import ua.lpnu.deposits.model.Bank;
import ua.lpnu.deposits.model.Deposit;
import ua.lpnu.deposits.service.BankService;
import ua.lpnu.deposits.service.DepositService;
import ua.lpnu.deposits.util.AppContext;
import ua.lpnu.deposits.util.AppLogger;
import ua.lpnu.deposits.util.UserSession;

import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for the Dashboard screen.
 * Computes and displays aggregate statistics with no business logic —
 * all data retrieval is delegated to the service layer.
 */
public class DashboardController implements Initializable {

    private static final AppLogger logger = AppLogger.getLogger(DashboardController.class);

    @FXML private Label welcomeLabel;
    @FXML private Label depositCountLabel;
    @FXML private Label avgRateLabel;
    @FXML private Label bankCountLabel;
    @FXML private Label maxRateLabel;
    @FXML private Label bestNameLabel;
    @FXML private Label bestDetailsLabel;

    private final DepositService depositService = AppContext.getInstance().getDepositService();
    private final BankService    bankService    = AppContext.getInstance().getBankService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String user = UserSession.getCurrentUser();
        welcomeLabel.setText("Вітаємо, " + (user != null ? user : "користувач") + "!");
        loadStats();
    }

    /**
     * Re-loads all statistics from the database and refreshes the labels.
     */
    public void loadStats() {
        try {
            List<Deposit> deposits = depositService.getAllDeposits();
            List<Bank>    banks    = bankService.getAllBanks();

            int count = deposits.size();
            depositCountLabel.setText(String.valueOf(count));
            bankCountLabel.setText(String.valueOf(banks.size()));

            if (count == 0) {
                avgRateLabel.setText("—");
                maxRateLabel.setText("—");
                bestNameLabel.setText("Поки що немає депозитів");
                bestDetailsLabel.setText("Додайте перший депозит у розділі «Депозити»");
                return;
            }

            double avg = deposits.stream()
                    .mapToDouble(Deposit::getInterestRate)
                    .average()
                    .orElse(0);
            avgRateLabel.setText(String.format("%.2f%%", avg));

            Optional<Deposit> best = deposits.stream()
                    .max(Comparator.comparingDouble(Deposit::getInterestRate));

            best.ifPresent(d -> {
                double max = d.getInterestRate();
                maxRateLabel.setText(String.format("%.2f%%", max));
                bestNameLabel.setText(d.getName());

                Map<Integer, String> bankMap = banks.stream()
                        .collect(Collectors.toMap(Bank::getId, Bank::getName));
                String bankName = bankMap.getOrDefault(d.getBankId(), "—");
                String typeStr = switch (d.getType()) {
                    case TERM    -> "Строковий";
                    case SAVINGS -> "Ощадний";
                    case DEMAND  -> "До запитання";
                };
                String termStr = d.getTermMonths() == 0
                        ? "необмежений термін"
                        : d.getTermMonths() + " міс.";
                bestDetailsLabel.setText(
                        bankName + "  •  " + typeStr + "  •  " + termStr
                        + "  •  " + d.getCurrency()
                );
            });

        } catch (SQLException e) {
            logger.error("Failed to load dashboard stats", e);
            depositCountLabel.setText("!");
            avgRateLabel.setText("!");
        }
    }
}
