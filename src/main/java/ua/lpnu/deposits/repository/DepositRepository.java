package ua.lpnu.deposits.repository;

import ua.lpnu.deposits.model.Deposit;
import ua.lpnu.deposits.model.DepositType;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data-access contract for {@link Deposit} entities (all subtypes).
 * All SQL must live exclusively in implementations of this interface.
 */
public interface DepositRepository {

    /**
     * Persists a deposit: inserts if {@code deposit.getId() == 0}, otherwise updates.
     * After a successful insert the generated id is written back to the entity.
     *
     * @param deposit the deposit to save
     * @return the saved deposit (with id populated for inserts)
     * @throws SQLException on any database error
     */
    Deposit save(Deposit deposit) throws SQLException;

    /**
     * Finds a deposit by its primary key.
     *
     * @param id the deposit id
     * @return an {@link Optional} containing the deposit, or empty if not found
     * @throws SQLException on any database error
     */
    Optional<Deposit> findById(int id) throws SQLException;

    /**
     * Returns all deposits ordered by name.
     *
     * @return list of all deposits, never {@code null}
     * @throws SQLException on any database error
     */
    List<Deposit> findAll() throws SQLException;

    /**
     * Returns all deposits offered by the given bank.
     *
     * @param bankId the bank id
     * @return matching deposits, never {@code null}
     * @throws SQLException on any database error
     */
    List<Deposit> findByBankId(int bankId) throws SQLException;

    /**
     * Returns all deposits of the given type.
     *
     * @param type the deposit type to filter by
     * @return matching deposits, never {@code null}
     * @throws SQLException on any database error
     */
    List<Deposit> findByType(DepositType type) throws SQLException;

    /**
     * Returns all deposits in the given currency.
     *
     * @param currency the currency code (e.g. "UAH", "USD")
     * @return matching deposits, never {@code null}
     * @throws SQLException on any database error
     */
    List<Deposit> findByCurrency(String currency) throws SQLException;

    /**
     * Returns deposits whose interest rate is within the given inclusive range.
     *
     * @param minRate lower bound of the interest rate
     * @param maxRate upper bound of the interest rate
     * @return matching deposits, never {@code null}
     * @throws SQLException on any database error
     */
    List<Deposit> findByInterestRateBetween(double minRate, double maxRate) throws SQLException;

    /**
     * Deletes a deposit by its primary key.
     *
     * @param id the deposit id to remove
     * @throws SQLException on any database error
     */
    void delete(int id) throws SQLException;
}
