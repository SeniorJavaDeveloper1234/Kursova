package ua.lpnu.deposits.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppLoggerTest {

    private final AppLogger logger = AppLogger.getLogger(AppLoggerTest.class);

    @Test
    void getLogger_returnsNonNull() {
        assertNotNull(AppLogger.getLogger(AppLoggerTest.class));
    }

    @Test
    void debug_doesNotThrow() {
        logger.debug("debug message: {}", "arg");
    }

    @Test
    void info_doesNotThrow() {
        logger.info("info message: {}", "arg");
    }

    @Test
    void warn_doesNotThrow() {
        logger.warn("warn message: {}", "arg");
    }

    @Test
    void error_withThrowable_doesNotThrow() {
        logger.error("error with cause", new RuntimeException("test cause"));
    }

    @Test
    void error_withoutThrowable_doesNotThrow() {
        logger.error("error without cause");
    }
}
