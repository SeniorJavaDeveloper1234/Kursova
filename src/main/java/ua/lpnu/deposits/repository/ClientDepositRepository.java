package ua.lpnu.deposits.repository;

import ua.lpnu.deposits.model.ClientDeposit;
import ua.lpnu.deposits.model.ClientDepositDetail;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data-access contract for {@link ClientDeposit} records.
 * All SQL must live exclusively in implementations of this interface.
 */
public interface ClientDepositRepository {

    /**
     * Persists a record: inserts if {@code cd.getId() == 0}, otherwise updates.
     * After a successful insert the generated id is written back to the entity.
     *
     * @param clientDeposit the record to save
     * @return the saved record (with id populated for inserts)
     * @throws SQLException on any database error
     */
    ClientDeposit save(ClientDeposit clientDeposit) throws SQLException;

    /**
     * Finds a record by its primary key.
     *
     * @param id the record id
     * @return an {@link Optional} containing the record, or empty if not found
     * @throws SQLException on any database error
     */
    Optional<ClientDeposit> findById(int id) throws SQLException;

    /**
     * Returns all client-deposit records ordered by opened_at descending.
     *
     * @return list of all records, never {@code null}
     * @throws SQLException on any database error
     */
    List<ClientDeposit> findAll() throws SQLException;

    /**
     * Returns all deposit records for the given client.
     *
     * @param clientId the client id
     * @return matching records, never {@code null}
     * @throws SQLException on any database error
     */
    List<ClientDeposit> findByClientId(int clientId) throws SQLException;

    /**
     * Returns enriched client-deposit rows for the given client, joining
     * {@code deposits} and {@code banks} in a single query.
     *
     * @param clientId the client id
     * @return list of detail rows ordered by opened_at descending, never {@code null}
     * @throws SQLException on any database error
     */
    List<ClientDepositDetail> findDetailedByClientId(int clientId) throws SQLException;

    /**
     * Returns all client records that use a specific deposit product.
     *
     * @param depositId the deposit product id
     * @return matching records, never {@code null}
     * @throws SQLException on any database error
     */
    List<ClientDeposit> findByDepositId(int depositId) throws SQLException;

    /**
     * Returns all records with the given status.
     *
     * @param status the status value (e.g. "ACTIVE", "CLOSED")
     * @return matching records, never {@code null}
     * @throws SQLException on any database error
     */
    List<ClientDeposit> findByStatus(String status) throws SQLException;

    /**
     * Deletes a record by its primary key.
     *
     * @param id the record id to remove
     * @throws SQLException on any database error
     */
    void delete(int id) throws SQLException;
}
