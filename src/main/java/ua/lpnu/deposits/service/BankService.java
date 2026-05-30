package ua.lpnu.deposits.service;

import ua.lpnu.deposits.model.Bank;
import ua.lpnu.deposits.model.Deposit;
import ua.lpnu.deposits.repository.BankRepository;
import ua.lpnu.deposits.repository.DepositRepository;
import ua.lpnu.deposits.util.AppLogger;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for bank management.
 * Contains no SQL and no JavaFX imports; delegates all persistence to repositories.
 */
public class BankService {

    private static final AppLogger logger = AppLogger.getLogger(BankService.class);

    private final BankRepository bankRepository;
    private final DepositRepository depositRepository;

    /**
     * Constructs a BankService with its required repositories.
     *
     * @param bankRepository    the bank data-access object
     * @param depositRepository the deposit data-access object (for bank-level deposit queries)
     */
    public BankService(BankRepository bankRepository, DepositRepository depositRepository) {
        this.bankRepository = bankRepository;
        this.depositRepository = depositRepository;
    }

    /**
     * Validates and persists a new bank.
     *
     * @param bank the bank to create (id must be 0)
     * @return the saved bank with its generated id
     * @throws IllegalArgumentException if validation fails
     * @throws SQLException             on any database error
     */
    public Bank createBank(Bank bank) throws SQLException {
        logger.info("Creating bank: name='{}'", bank.getName());
        try {
            validateBank(bank);
            Bank saved = bankRepository.save(bank);
            logger.info("Bank created: id={}, name='{}'", saved.getId(), saved.getName());
            return saved;
        } catch (SQLException e) {
            logger.error("Failed to create bank '" + bank.getName() + "'", e);
            throw e;
        }
    }

    /**
     * Validates and updates an existing bank.
     *
     * @param bank the bank to update (id must be non-zero)
     * @return the updated bank
     * @throws IllegalArgumentException if validation fails or id is 0
     * @throws SQLException             on any database error
     */
    public Bank updateBank(Bank bank) throws SQLException {
        if (bank.getId() == 0) {
            throw new IllegalArgumentException("Cannot update a bank without an id");
        }
        logger.info("Updating bank: id={}, name='{}'", bank.getId(), bank.getName());
        try {
            validateBank(bank);
            Bank updated = bankRepository.save(bank);
            logger.info("Bank updated: id={}", updated.getId());
            return updated;
        } catch (SQLException e) {
            logger.error("Failed to update bank id=" + bank.getId(), e);
            throw e;
        }
    }

    /**
     * Deletes a bank by id.
     *
     * @param id the bank id to remove
     * @throws SQLException on any database error
     */
    public void deleteBank(int id) throws SQLException {
        logger.info("Deleting bank: id={}", id);
        try {
            bankRepository.delete(id);
            logger.info("Bank deleted: id={}", id);
        } catch (SQLException e) {
            logger.error("Failed to delete bank id=" + id, e);
            throw e;
        }
    }

    /**
     * Retrieves a bank by its id.
     *
     * @param id the bank id
     * @return an {@link Optional} containing the bank, or empty if not found
     * @throws SQLException on any database error
     */
    public Optional<Bank> getBankById(int id) throws SQLException {
        logger.debug("Fetching bank: id={}", id);
        try {
            Optional<Bank> result = bankRepository.findById(id);
            if (result.isEmpty()) {
                logger.warn("Bank not found: id={}", id);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Failed to fetch bank id=" + id, e);
            throw e;
        }
    }

    /**
     * Returns all banks ordered by name.
     *
     * @return list of all banks, never {@code null}
     * @throws SQLException on any database error
     */
    public List<Bank> getAllBanks() throws SQLException {
        logger.debug("Fetching all banks");
        try {
            List<Bank> banks = bankRepository.findAll();
            logger.debug("Fetched {} bank(s)", banks.size());
            return banks;
        } catch (SQLException e) {
            logger.error("Failed to fetch all banks", e);
            throw e;
        }
    }

    /**
     * Searches banks by name substring. Returns all banks if the query is blank.
     *
     * @param name the name substring to search (case-insensitive)
     * @return matching banks, never {@code null}
     * @throws SQLException on any database error
     */
    public List<Bank> searchByName(String name) throws SQLException {
        logger.debug("Searching banks by name='{}'", name);
        try {
            List<Bank> result;
            if (name == null || name.isBlank()) {
                result = bankRepository.findAll();
            } else {
                result = bankRepository.findByName(name);
            }
            logger.debug("Bank search returned {} result(s)", result.size());
            return result;
        } catch (SQLException e) {
            logger.error("Failed to search banks by name '" + name + "'", e);
            throw e;
        }
    }

    /**
     * Returns all deposit products offered by the given bank.
     *
     * @param bankId the bank id
     * @return list of deposits, never {@code null}
     * @throws SQLException on any database error
     */
    public List<Deposit> getDepositsForBank(int bankId) throws SQLException {
        logger.debug("Fetching deposits for bank id={}", bankId);
        try {
            List<Deposit> deposits = depositRepository.findByBankId(bankId);
            logger.debug("Found {} deposit(s) for bank id={}", deposits.size(), bankId);
            return deposits;
        } catch (SQLException e) {
            logger.error("Failed to fetch deposits for bank id=" + bankId, e);
            throw e;
        }
    }

    private void validateBank(Bank bank) {
        if (bank.getName() == null || bank.getName().isBlank()) {
            throw new IllegalArgumentException("Bank name cannot be empty");
        }
        if (bank.getRating() < 0.0 || bank.getRating() > 10.0) {
            throw new IllegalArgumentException("Bank rating must be between 0.0 and 10.0");
        }
    }
}
