package ua.lpnu.deposits.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Singleton that sends SMTP email notifications for critical application errors.
 * <p>
 * SMTP host/port are read from {@code email.properties} on the classpath.
 * Credentials (username, password, from, to) are read from the {@code .env} file
 * in the working directory, so they are never stored in the source tree.
 * <p>
 * If the configuration is absent or incomplete, email sending is silently disabled —
 * the application continues to run normally.
 */
public class EmailSender {

    private static final Logger LOGGER = LogManager.getLogger(EmailSender.class);
    private static final String CONFIG_RESOURCE = "/email.properties";
    private static final String DOT_ENV_FILE    = ".env";

    private static volatile EmailSender instance;

    private final String smtpHost;
    private final String smtpPort;
    private final String username;
    private final String password;
    private final String from;
    private final String to;
    private final boolean starttls;
    /** {@code true} only when all required properties are non-empty. */
    private final boolean enabled;

    private EmailSender() {
        Properties cfg = loadConfig();          // email.properties  — SMTP host/port
        Map<String, String> env = loadDotEnv(); // .env              — credentials

        // .env values take priority; email.properties is the fallback
        this.smtpHost = env.getOrDefault("MAIL_SMTP_HOST",
                        cfg.getProperty("mail.smtp.host", "")).strip();
        this.smtpPort = env.getOrDefault("MAIL_SMTP_PORT",
                        cfg.getProperty("mail.smtp.port", "587")).strip();
        this.username = env.getOrDefault("MAIL_USERNAME",
                        cfg.getProperty("mail.username", "")).strip();
        this.password = env.getOrDefault("MAIL_PASSWORD",
                        cfg.getProperty("mail.password", "")).strip();
        this.from     = env.getOrDefault("MAIL_FROM",
                        cfg.getProperty("mail.from", "")).strip();
        this.to       = env.getOrDefault("MAIL_TO",
                        cfg.getProperty("mail.to", "")).strip();
        this.starttls = Boolean.parseBoolean(
                        env.getOrDefault("MAIL_STARTTLS",
                        cfg.getProperty("mail.smtp.starttls.enable", "true")).strip());

        this.enabled = !smtpHost.isEmpty() && !username.isEmpty()
                && !password.isEmpty() && !to.isEmpty();

        if (enabled) {
            LOGGER.info("EmailSender: error notifications enabled (recipient: {})", to);
        } else {
            LOGGER.warn("EmailSender: disabled — configure email.properties to enable error notifications");
        }
    }

    /**
     * Returns the singleton instance, creating it on the first call.
     *
     * @return the singleton {@code EmailSender}
     */
    public static EmailSender getInstance() {
        if (instance == null) {
            synchronized (EmailSender.class) {
                if (instance == null) {
                    instance = new EmailSender();
                }
            }
        }
        return instance;
    }

    /**
     * Sends an error notification email.
     * If email is disabled or sending fails, a warning is logged and the method returns normally —
     * it never propagates an exception to the caller.
     *
     * @param message the human-readable error description
     * @param cause   the throwable that triggered the error, or {@code null}
     */
    public void sendErrorEmail(String message, Throwable cause) {
        if (!enabled) {
            return;
        }
        try {
            Session session = buildSession();
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from.isEmpty() ? username : from));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject("Bank Deposits App — CRITICAL ERROR");
            msg.setText(buildBody(message, cause));
            Transport.send(msg);
            LOGGER.debug("Error notification email sent to {}", to);
        } catch (MessagingException e) {
            LOGGER.warn("Failed to send error notification email: {}", e.getMessage());
        }
    }

    private Session buildSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", String.valueOf(starttls));
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    private String buildBody(String message, Throwable cause) {
        StringBuilder body = new StringBuilder();
        body.append("Application: Bank Deposits App\n");
        body.append("Severity:    ERROR\n\n");
        body.append("Message:\n").append(message).append("\n");
        if (cause != null) {
            body.append("\nStack trace:\n");
            StringWriter sw = new StringWriter();
            cause.printStackTrace(new PrintWriter(sw));
            body.append(sw);
        }
        return body.toString();
    }

    private Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream is = EmailSender.class.getResourceAsStream(CONFIG_RESOURCE)) {
            if (is != null) {
                props.load(is);
            } else {
                LOGGER.warn("EmailSender: {} not found on classpath", CONFIG_RESOURCE);
            }
        } catch (IOException e) {
            LOGGER.warn("EmailSender: failed to load {}: {}", CONFIG_RESOURCE, e.getMessage());
        }
        return props;
    }

    /**
     * Reads key=value pairs from the {@code .env} file in the working directory.
     * Lines starting with {@code #} and blank lines are ignored.
     * Returns an empty map if the file does not exist.
     *
     * @return map of environment variable names to their values
     */
    private Map<String, String> loadDotEnv() {
        Map<String, String> result = new HashMap<>();
        Path path = Paths.get(DOT_ENV_FILE);
        if (!Files.exists(path)) {
            LOGGER.warn("EmailSender: .env file not found — credentials must be in email.properties");
            return result;
        }
        try {
            Files.lines(path, StandardCharsets.UTF_8)
                    .map(String::strip)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .forEach(line -> {
                        int eq = line.indexOf('=');
                        if (eq > 0) {
                            String key = line.substring(0, eq).strip();
                            String val = line.substring(eq + 1).strip();
                            result.put(key, val);
                        }
                    });
            LOGGER.debug("EmailSender: loaded {} entries from .env", result.size());
        } catch (IOException e) {
            LOGGER.warn("EmailSender: failed to read .env: {}", e.getMessage());
        }
        return result;
    }
}
