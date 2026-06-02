package ua.lpnu.deposits.repository;

import ua.lpnu.deposits.model.Client;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data-access contract for {@link Client} entities.
 * All SQL must live exclusively in implementations of this interface.
 */
public interface ClientRepository {

    /**
     * Persists a client: inserts if {@code client.getId() == 0}, otherwise updates.
     * After a successful insert the generated id is written back to the entity.
     *
     * @param client the client to save
     * @return the saved client (with id populated for inserts)
     * @throws SQLException on any database error
     */
    Client save(Client client) throws SQLException;

    /**
     * Finds a client by its primary key.
     *
     * @param id the client id
     * @return an {@link Optional} containing the client, or empty if not found
     * @throws SQLException on any database error
     */
    Optional<Client> findById(int id) throws SQLException;

    /**
     * Returns all clients ordered by last name, then first name.
     *
     * @return list of all clients, never {@code null}
     * @throws SQLException on any database error
     */
    List<Client> findAll() throws SQLException;

    /**
     * Finds clients whose last name contains the given substring (case-insensitive).
     *
     * @param lastName the last-name substring to search
     * @return matching clients, never {@code null}
     * @throws SQLException on any database error
     */
    List<Client> findByLastName(String lastName) throws SQLException;

    /**
     * Searches clients by id (exact, if the query is numeric), first name, or last name substring.
     *
     * @param query the search string
     * @return matching clients, never {@code null}
     * @throws SQLException on any database error
     */
    List<Client> search(String query) throws SQLException;

    /**
     * Finds a client by exact email address.
     *
     * @param email the email to search for
     * @return an {@link Optional} containing the client, or empty if not found
     * @throws SQLException on any database error
     */
    Optional<Client> findByEmail(String email) throws SQLException;

    /**
     * Deletes a client by its primary key.
     *
     * @param id the client id to remove
     * @throws SQLException on any database error
     */
    void delete(int id) throws SQLException;
}
