package ua.lpnu.deposits.util;

/**
 * Lightweight in-memory session that stores the currently logged-in username and role.
 * There is at most one active session per JVM process.
 */
public final class UserSession {

    private static volatile String currentUser;
    private static volatile String currentRole;

    private UserSession() {}

    /**
     * Records the authenticated user and their role.
     *
     * @param username the verified login name
     * @param role     the user's role (e.g. {@code "ADMIN"}, {@code "USER"})
     */
    public static void login(String username, String role) {
        currentUser = username;
        currentRole = role;
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
     * Returns the role of the currently logged-in user,
     * or {@code null} if no one is logged in.
     *
     * @return current role or {@code null}
     */
    public static String getCurrentRole() {
        return currentRole;
    }

    /**
     * Returns {@code true} if the current user has the {@code ADMIN} role.
     *
     * @return {@code true} when the logged-in user is an administrator
     */
    public static boolean isAdmin() {
        return "ADMIN".equals(currentRole);
    }

    /**
     * Clears the session (used on logout).
     */
    public static void logout() {
        currentUser = null;
        currentRole = null;
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
