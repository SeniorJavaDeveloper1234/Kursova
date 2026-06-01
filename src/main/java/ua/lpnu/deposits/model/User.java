package ua.lpnu.deposits.model;

/**
 * Represents an application user stored in the {@code users} table.
 */
public class User {

    private final int    id;
    private final String username;
    private final String passwordHash;
    private final String role;

    /**
     * Creates a user with all fields (used when reading from DB).
     *
     * @param id           primary key
     * @param username     login name
     * @param passwordHash SHA-256 hex digest
     * @param role         user role (e.g. "ADMIN", "USER")
     */
    public User(int id, String username, String passwordHash, String role) {
        this.id           = id;
        this.username     = username;
        this.passwordHash = passwordHash;
        this.role         = role;
    }

    /**
     * Returns the database primary key.
     *
     * @return the primary key
     */
    public int getId() { return id; }

    /**
     * Returns the login name.
     *
     * @return the username
     */
    public String getUsername() { return username; }

    /**
     * Returns the SHA-256 hex digest of the password.
     *
     * @return the password hash
     */
    public String getPasswordHash() { return passwordHash; }

    /**
     * Returns the user role.
     *
     * @return the role (e.g. "ADMIN", "USER")
     */
    public String getRole() { return role; }
}
