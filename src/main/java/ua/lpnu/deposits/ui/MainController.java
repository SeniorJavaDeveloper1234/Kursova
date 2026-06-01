package ua.lpnu.deposits.ui;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import ua.lpnu.deposits.util.AppLogger;
import ua.lpnu.deposits.util.UserSession;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the main application shell.
 * Manages sidebar navigation and top-bar state.
 * Contains no business logic; all data work is inside the nested controllers.
 */
public class MainController implements Initializable {

    private static final AppLogger logger = AppLogger.getLogger(MainController.class);

    @FXML private StackPane contentPane;

    @FXML private Button navDashboard;
    @FXML private Button navDeposits;
    @FXML private Button navBanks;
    @FXML private Button navClients;
    @FXML private Button navSearch;

    @FXML private Label sidebarUserLabel;
    @FXML private Label topbarTitle;
    @FXML private Label topbarUserChip;

    @FXML private DashboardController dashboardRootController;

    private Button[] navButtons;
    private static final String[] PAGE_TITLES =
            {"Головна панель", "Депозити", "Банки", "Клієнти", "Пошук та фільтр"};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        navButtons = new Button[]{navDashboard, navDeposits, navBanks, navClients, navSearch};

        String user = UserSession.getCurrentUser();
        String displayUser = user != null ? user : "—";
        sidebarUserLabel.setText(displayUser);
        topbarUserChip.setText("👤  " + displayUser);

        if (!UserSession.isAdmin()) {
            navBanks.setVisible(false);
            navBanks.setManaged(false);
            navClients.setVisible(false);
            navClients.setManaged(false);
        }

        showPane(0);
    }

    /** @noinspection unused */
    @FXML private void onNavDashboard() { showPane(0); }

    /** @noinspection unused */
    @FXML private void onNavDeposits()  { showPane(1); }

    /** @noinspection unused */
    @FXML private void onNavBanks()     { showPane(2); }

    /** @noinspection unused */
    @FXML private void onNavClients()   { showPane(3); }

    /** @noinspection unused */
    @FXML private void onNavSearch()    { showPane(4); }

    /** @noinspection unused */
    @FXML
    private void onLogout() {
        if (!AlertUtil.showConfirm("Вихід із системи",
                "Ви впевнені, що хочете вийти з облікового запису?")) return;

        logger.info("User '{}' logged out", UserSession.getCurrentUser());
        UserSession.logout();

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/LoginView.fxml"));
            Scene scene = new Scene(loader.load(), 520, 480);
            scene.getStylesheets().add(
                    getClass().getResource("/css/style.css").toExternalForm());

            Stage loginStage = new Stage();
            loginStage.setTitle("Депозитний Помічник — Вхід");
            loginStage.setScene(scene);
            loginStage.setResizable(false);
            loginStage.show();

                ((Stage) contentPane.getScene().getWindow()).close();

        } catch (Exception e) {
            logger.error("Failed to return to login screen", e);
            AlertUtil.showError("Помилка", "Не вдалося відкрити вікно входу:\n" + e.getMessage());
        }
    }

    /**
     * Makes only the pane at {@code index} visible; hides and un-manages the rest.
     * Also updates the active-nav-button highlight and the top-bar title.
     *
     * @param index 0=Dashboard, 1=Deposits, 2=Banks, 3=Search
     */
    private void showPane(int index) {
        ObservableList<Node> children = contentPane.getChildren();
        for (int i = 0; i < children.size(); i++) {
            boolean active = (i == index);
            children.get(i).setVisible(active);
            children.get(i).setManaged(active);
        }

        if (index < PAGE_TITLES.length) {
            topbarTitle.setText(PAGE_TITLES[index]);
        }

        for (int i = 0; i < navButtons.length; i++) {
            navButtons[i].getStyleClass().remove("nav-btn-active");
            if (i == index) navButtons[i].getStyleClass().add("nav-btn-active");
        }

        if (index == 0 && dashboardRootController != null) {
            dashboardRootController.loadStats();
        }
    }
}
