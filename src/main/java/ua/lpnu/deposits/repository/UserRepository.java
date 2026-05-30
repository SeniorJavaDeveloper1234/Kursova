package ua.lpnu.deposits.repository;

import ua.lpnu.deposits.model.User;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Data-access contract for the {@code users} table.
 */
public interface UserRepository {

    /**
     * Finds a user by their login name.
     *
     * @param username the login name to look up
     * @return an {@link Optional} containing the user, or empty if not found
     * @throws SQLException on DB error
     */
    Optional<User> findByUsername(String username) throws SQLException;

    /**
     * Verifies credentials. Hashes the plain-text password and compares
     * it against the stored SHA-256 digest.
     *
     * @param username  login name
     * @param plainText plain-text password (will be hashed internally)
     * @return {@code true} if the credentials match a user in the database
     * @throws SQLException on DB error
     */
    boolean authenticate(String username, String plainText) throws SQLException;

    /**
     * Persists a new user.  The {@code passwordHash} field of the supplied
     * {@link User} must already be the SHA-256 hex digest.
     *
     * @param user user to insert
     * @throws SQLException on DB error
     */
    void save(User user) throws SQLException;

    /**
     * Creates the default {@code admin/admin} account if the users table is empty.
     *
     * @throws SQLException on DB error
     */
    void ensureDefaultAdmin() throws SQLException;
}
