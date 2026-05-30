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
import java.util.Properties;

/**
 * Singleton that sends SMTP email notifications for critical application errors.
 * Configuration is loaded from {@code email.properties} on the classpath.
 * If the configuration is absent or incomplete, email sending is silently disabled —
 * the application continues to run normally.
 */
public class EmailSender {

    private static final Logger LOGGER = LogManager.getLogger(EmailSender.class);
    private static final String CONFIG_RESOURCE = "/email.properties";

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
        Properties cfg = loadConfig();
        this.smtpHost = cfg.getProperty("mail.smtp.host", "").strip();
        this.smtpPort = cfg.getProperty("mail.smtp.port", "587").strip();
        this.username = cfg.getProperty("mail.username", "").strip();
        this.password = cfg.getProperty("mail.password", "").strip();
        this.from     = cfg.getProperty("mail.from", "").strip();
        this.to       = cfg.getProperty("mail.to", "").strip();
        this.starttls = Boolean.parseBoolean(
                cfg.getProperty("mail.smtp.starttls.enable", "true").strip());

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

}
