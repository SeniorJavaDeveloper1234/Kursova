package ua.lpnu.deposits.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Thin façade over Log4j2 that augments {@code ERROR}-level calls with an
 * automatic email notification via {@link EmailSender}.
 *
 * <p>Usage:
 * <pre>
 *     private static final AppLogger logger = AppLogger.getLogger(MyClass.class);
 * </pre>
 *
 * <p>DEBUG, INFO, and WARN calls are forwarded to Log4j2 unchanged.
 * ERROR calls are forwarded to Log4j2 <em>and</em> trigger {@link EmailSender}.
 */
public class AppLogger {

    private final Logger logger;

    private AppLogger(Class<?> clazz) {
        this.logger = LogManager.getLogger(clazz);
    }

    /**
     * Creates an {@code AppLogger} for the given class.
     *
     * @param clazz the class whose name will label log messages
     * @return a new {@code AppLogger} instance
     */
    public static AppLogger getLogger(Class<?> clazz) {
        return new AppLogger(clazz);
    }

    // -------------------------------------------------------------------------
    // DEBUG
    // -------------------------------------------------------------------------

    /**
     * Logs a DEBUG message with optional parameterised arguments (Log4j2 {@code {}} style).
     *
     * @param message the message pattern
     * @param args    arguments substituted into {@code {}} placeholders
     */
    public void debug(String message, Object... args) {
        logger.debug(message, args);
    }

    // -------------------------------------------------------------------------
    // INFO
    // -------------------------------------------------------------------------

    /**
     * Logs an INFO message with optional parameterised arguments.
     *
     * @param message the message pattern
     * @param args    arguments substituted into {@code {}} placeholders
     */
    public void info(String message, Object... args) {
        logger.info(message, args);
    }

    // -------------------------------------------------------------------------
    // WARN
    // -------------------------------------------------------------------------

    /**
     * Logs a WARN message with optional parameterised arguments.
     *
     * @param message the message pattern
     * @param args    arguments substituted into {@code {}} placeholders
     */
    public void warn(String message, Object... args) {
        logger.warn(message, args);
    }

    // -------------------------------------------------------------------------
    // ERROR  — also triggers email notification
    // -------------------------------------------------------------------------

    /**
     * Logs an ERROR message with the causing exception and sends an email notification.
     *
     * @param message the error description
     * @param cause   the throwable that caused the error
     */
    public void error(String message, Throwable cause) {
        logger.error(message, cause);
        EmailSender.getInstance().sendErrorEmail(message, cause);
    }

    /**
     * Logs an ERROR message (no exception) and sends an email notification.
     *
     * @param message the error description
     */
    public void error(String message) {
        logger.error(message);
        EmailSender.getInstance().sendErrorEmail(message, null);
    }
}
