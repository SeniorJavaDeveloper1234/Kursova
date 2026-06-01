package ua.lpnu.deposits.util;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Manual smoke-test for SMTP connectivity.
 * Sends a real email — run only when you want to verify credentials:
 *
 *   mvn test -pl . -Dtest=EmailSenderLiveTest -Dgroups=live-email
 */
@Tag("live-email")
class EmailSenderLiveTest {

    @Test
    void sendTestEmail_shouldDeliverWithoutException() {
        EmailSender.getInstance().sendErrorEmail(
                "Test email from EmailSenderLiveTest — SMTP credentials OK", null);
    }
}
