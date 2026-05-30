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

    /** @return database primary key */
    public int getId()           { return id; }

    /** @return login name */
    public String getUsername()  { return username; }

    /** @return SHA-256 hex digest of the password */
    public String getPasswordHash() { return passwordHash; }

    /** @return user role */
    public String getRole()      { return role; }
}
