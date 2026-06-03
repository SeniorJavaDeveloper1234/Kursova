/**
 * Application module for the Bank Deposits management system.
 * Provides the JavaFX UI, service layer, JDBC repositories, and utility classes.
 */
module ua.lpnu.deposits {

    // ── JavaFX ──────────────────────────────────────────────────────────────
    requires javafx.controls;
    requires javafx.fxml;

    // ── Database ─────────────────────────────────────────────────────────────
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    // ── Logging ──────────────────────────────────────────────────────────────
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;

    // ── Mail ─────────────────────────────────────────────────────────────────
    requires org.eclipse.angus.mail;

    // ── Opens for JavaFX reflection ──────────────────────────────────────────
    // javafx.graphics needs access to Application subclass (Main)
    opens ua.lpnu.deposits to javafx.graphics;

    // javafx.fxml needs reflective access to controllers (@FXML fields/methods)
    opens ua.lpnu.deposits.ui to javafx.fxml;

    // javafx.base needs access to model beans when used in observable properties
    opens ua.lpnu.deposits.model to javafx.base;
}
