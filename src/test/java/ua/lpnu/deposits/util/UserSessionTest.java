package ua.lpnu.deposits.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserSessionTest {

    @AfterEach
    void tearDown() {
        UserSession.logout();
    }

    @Test
    void noLogin_currentUserIsNull() {
        assertNull(UserSession.getCurrentUser());
    }

    @Test
    void noLogin_currentRoleIsNull() {
        assertNull(UserSession.getCurrentRole());
    }

    @Test
    void noLogin_isLoggedIn_returnsFalse() {
        assertFalse(UserSession.isLoggedIn());
    }

    @Test
    void noLogin_isAdmin_returnsFalse() {
        assertFalse(UserSession.isAdmin());
    }

    @Test
    void login_setsCurrentUser() {
        UserSession.login("admin", "ADMIN");
        assertEquals("admin", UserSession.getCurrentUser());
    }

    @Test
    void login_setsCurrentRole() {
        UserSession.login("admin", "ADMIN");
        assertEquals("ADMIN", UserSession.getCurrentRole());
    }

    @Test
    void login_adminRole_isAdminReturnsTrue() {
        UserSession.login("admin", "ADMIN");
        assertTrue(UserSession.isAdmin());
    }

    @Test
    void login_userRole_isAdminReturnsFalse() {
        UserSession.login("user", "USER");
        assertFalse(UserSession.isAdmin());
    }

    @Test
    void login_isLoggedIn_returnsTrue() {
        UserSession.login("admin", "ADMIN");
        assertTrue(UserSession.isLoggedIn());
    }

    @Test
    void logout_clearsCurrentUser() {
        UserSession.login("admin", "ADMIN");
        UserSession.logout();
        assertNull(UserSession.getCurrentUser());
    }

    @Test
    void logout_clearsCurrentRole() {
        UserSession.login("admin", "ADMIN");
        UserSession.logout();
        assertNull(UserSession.getCurrentRole());
    }

    @Test
    void logout_isAdmin_returnsFalse() {
        UserSession.login("admin", "ADMIN");
        UserSession.logout();
        assertFalse(UserSession.isAdmin());
    }

    @Test
    void logout_isLoggedIn_returnsFalse() {
        UserSession.login("admin", "ADMIN");
        UserSession.logout();
        assertFalse(UserSession.isLoggedIn());
    }

    @Test
    void login_overwritesPreviousUser() {
        UserSession.login("first", "ADMIN");
        UserSession.login("second", "USER");
        assertEquals("second", UserSession.getCurrentUser());
        assertEquals("USER", UserSession.getCurrentRole());
    }
}
