package ua.lpnu.deposits.repository;

import ua.lpnu.deposits.model.Bank;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data-access contract for {@link Bank} entities.
 * All SQL must live exclusively in implementations of this interface.
 */
public interface BankRepository {

    /**
     * Persists a bank: inserts if {@code bank.getId() == 0}, otherwise updates.
     * After a successful insert the generated id is written back to the entity.
     *
     * @param bank the bank to save
     * @return the saved bank (with id populated for inserts)
     * @throws SQLException on any database error
     */
    Bank save(Bank bank) throws SQLException;

    /**
     * Finds a bank by its primary key.
     *
     * @param id the bank id
     * @return an {@link Optional} containing the bank, or empty if not found
     * @throws SQLException on any database error
     */
    Optional<Bank> findById(int id) throws SQLException;

    /**
     * Returns all banks ordered by name.
     *
     * @return list of all banks, never {@code null}
     * @throws SQLException on any database error
     */
    List<Bank> findAll() throws SQLException;

    /**
     * Finds banks whose name contains the given substring (case-insensitive).
     *
     * @param name the name substring to search
     * @return matching banks, never {@code null}
     * @throws SQLException on any database error
     */
    List<Bank> findByName(String name) throws SQLException;

    /**
     * Deletes a bank by its primary key.
     *
     * @param id the bank id to remove
     * @throws SQLException on any database error
     */
    void delete(int id) throws SQLException;
}
