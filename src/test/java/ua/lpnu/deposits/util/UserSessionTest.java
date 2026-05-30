package ua.lpnu.deposits.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserSessionTest {

    @AfterEach
    void tearDown() {
        // Always clean up so tests don't bleed into each other
        UserSession.logout();
    }

    @Test
    void noLogin_currentUserIsNull() {
        assertNull(UserSession.getCurrentUser());
    }

    @Test
    void noLogin_isLoggedIn_returnsFalse() {
        assertFalse(UserSession.isLoggedIn());
    }

    @Test
    void login_setsCurrentUser() {
        UserSession.login("admin");
        assertEquals("admin", UserSession.getCurrentUser());
    }

    @Test
    void login_isLoggedIn_returnsTrue() {
        UserSession.login("admin");
        assertTrue(UserSession.isLoggedIn());
    }

    @Test
    void logout_clearsCurrentUser() {
        UserSession.login("admin");
        UserSession.logout();
        assertNull(UserSession.getCurrentUser());
    }

    @Test
    void logout_isLoggedIn_returnsFalse() {
        UserSession.login("admin");
        UserSession.logout();
        assertFalse(UserSession.isLoggedIn());
    }

    @Test
    void login_overwritesPreviousUser() {
        UserSession.login("first");
        UserSession.login("second");
        assertEquals("second", UserSession.getCurrentUser());
    }
}
