package ua.lpnu.deposits.util;

/**
 * Lightweight in-memory session that stores the currently logged-in username.
 * There is at most one active session per JVM process.
 */
public final class UserSession {

    private static volatile String currentUser;

    private UserSession() {}

    /**
     * Records the authenticated user.
     *
     * @param username the verified login name
     */
    public static void login(String username) {
        currentUser = username;
    }

    /**
     * Returns the username of the currently logged-in user,
     * or {@code null} if no one is logged in.
     *
     * @return current username or {@code null}
     */
    public static String getCurrentUser() {
        return currentUser;
    }

    /**
     * Clears the session (used on logout).
     */
    public static void logout() {
        currentUser = null;
    }

    /**
     * Returns {@code true} if a user is currently logged in.
     *
     * @return {@code true} when authenticated
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
