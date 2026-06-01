package ua.lpnu.deposits;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ua.lpnu.deposits.util.AppContext;
import ua.lpnu.deposits.util.AppLogger;

/**
 * JavaFX application entry point.
 * Initialises the database / service layer, then shows the login screen.
 */
public class Main extends Application {

    private static final AppLogger logger = AppLogger.getLogger(Main.class);

    /** Creates a new {@code Main} application instance (called by the JavaFX runtime). */
    public Main() {}

    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.info("Starting Bank Deposits application");

        try {
            AppContext.initialize();
        } catch (Exception e) {
            logger.error("Failed to initialise application context", e);
            throw new RuntimeException("Cannot start: " + e.getMessage(), e);
        }

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/LoginView.fxml"));
        Scene scene = new Scene(loader.load(), 520, 480);
        scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm());

        primaryStage.setTitle("Депозитний Помічник — Вхід");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        logger.info("Login screen displayed");
    }

    /**
     * Application entry point.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
