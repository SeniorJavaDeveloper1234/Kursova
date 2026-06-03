package ua.lpnu.deposits.service;

import ua.lpnu.deposits.model.Client;
import ua.lpnu.deposits.model.ClientDeposit;
import ua.lpnu.deposits.model.ClientDepositDetail;
import ua.lpnu.deposits.model.Deposit;
import ua.lpnu.deposits.model.DepositType;
import ua.lpnu.deposits.repository.ClientDepositRepository;
import ua.lpnu.deposits.repository.ClientRepository;
import ua.lpnu.deposits.repository.DepositRepository;
import ua.lpnu.deposits.util.AppLogger;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for deposit products and client deposit operations.
 * Contains no SQL and no JavaFX imports; delegates all persistence to repositories.
 */
public class DepositService {

    private static final AppLogger logger = AppLogger.getLogger(DepositService.class);

    private final DepositRepository depositRepository;
    private final ClientRepository clientRepository;
    private final ClientDepositRepository clientDepositRepository;

    /**
     * Constructs a DepositService with its required repositories.
     *
     * @param depositRepository       deposit product data-access object
     * @param clientRepository        client data-access object
     * @param clientDepositRepository client-deposit record data-access object
     */
    public DepositService(DepositRepository depositRepository,
                          ClientRepository clientRepository,
                          ClientDepositRepository clientDepositRepository) {
        this.depositRepository = depositRepository;
        this.clientRepository = clientRepository;
        this.clientDepositRepository = clientDepositRepository;
    }

    /**
     * Validates and persists a new deposit product.
     *
     * @param deposit the deposit to create (id must be 0)
     * @return the saved deposit with its generated id
     * @throws IllegalArgumentException if validation fails
     * @throws SQLException             on any database error
     */
    public Deposit createDeposit(Deposit deposit) throws SQLException {
        logger.info("Creating deposit: name='{}', type={}, bankId={}",
                deposit.getName(), deposit.getType(), deposit.getBankId());
        try {
            validateDeposit(deposit);
            Deposit saved = depositRepository.save(deposit);
            logger.info("Deposit created: id={}, name='{}'", saved.getId(), saved.getName());
            return saved;
        } catch (SQLException e) {
            logger.error("Failed to create deposit '" + deposit.getName() + "'", e);
            throw e;
        }
    }

    /**
     * Validates and updates an existing deposit product.
     *
     * @param deposit the deposit to update (id must be non-zero)
     * @return the updated deposit
     * @throws IllegalArgumentException if validation fails or id is 0
     * @throws SQLException             on any database error
     */
    public Deposit updateDeposit(Deposit deposit) throws SQLException {
        if (deposit.getId() == 0) {
            throw new IllegalArgumentException("Cannot update a deposit without an id");
        }
        logger.info("Updating deposit: id={}, name='{}'", deposit.getId(), deposit.getName());
        try {
            validateDeposit(deposit);
            Deposit updated = depositRepository.save(deposit);
            logger.info("Deposit updated: id={}", updated.getId());
            return updated;
        } catch (SQLException e) {
            logger.error("Failed to update deposit id=" + deposit.getId(), e);
            throw e;
        }
    }

    /**
     * Deletes a deposit product by id.
     *
     * @param id the deposit id to remove
     * @throws SQLException on any database error
     */
    public void deleteDeposit(int id) throws SQLException {
        logger.info("Deleting deposit: id={}", id);
        try {
            for (ClientDeposit cd : clientDepositRepository.findByDepositId(id)) {
                clientDepositRepository.delete(cd.getId());
            }
            depositRepository.delete(id);
            logger.info("Deposit deleted: id={}", id);
        } catch (SQLException e) {
            logger.error("Failed to delete deposit id=" + id, e);
            throw e;
        }
    }

    /**
     * Retrieves a deposit product by its id.
     *
     * @param id the deposit id
     * @return an {@link Optional} containing the deposit, or empty if not found
     * @throws SQLException on any database error
     */
    public Optional<Deposit> getDepositById(int id) throws SQLException {
        logger.debug("Fetching deposit: id={}", id);
        try {
            Optional<Deposit> result = depositRepository.findById(id);
            if (result.isEmpty()) {
                logger.warn("Deposit not found: id={}", id);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Failed to fetch deposit id=" + id, e);
            throw e;
        }
    }

    /**
     * Returns all deposit products ordered by name.
     *
     * @return list of all deposits, never {@code null}
     * @throws SQLException on any database error
     */
    public List<Deposit> getAllDeposits() throws SQLException {
        logger.debug("Fetching all deposits");
        try {
            List<Deposit> deposits = depositRepository.findAll();
            logger.debug("Fetched {} deposit(s)", deposits.size());
            return deposits;
        } catch (SQLException e) {
            logger.error("Failed to fetch all deposits", e);
            throw e;
        }
    }

    /**
     * Returns all deposit products of the given type.
     *
     * @param type the deposit type
     * @return matching deposits, never {@code null}
     * @throws SQLException on any database error
     */
    public List<Deposit> getDepositsByType(DepositType type) throws SQLException {
        logger.debug("Fetching deposits by type={}", type);
        try {
            List<Deposit> deposits = depositRepository.findByType(type);
            logger.debug("Found {} deposit(s) of type {}", deposits.size(), type);
            return deposits;
        } catch (SQLException e) {
            logger.error("Failed to fetch deposits by type " + type, e);
            throw e;
        }
    }

    /**
     * Returns all deposit products in the given currency.
     *
     * @param currency the currency code (e.g. "UAH")
     * @return matching deposits, never {@code null}
     * @throws SQLException on any database error
     */
    public List<Deposit> getDepositsByCurrency(String currency) throws SQLException {
        logger.debug("Fetching deposits by currency='{}'", currency);
        try {
            List<Deposit> deposits = depositRepository.findByCurrency(currency);
            logger.debug("Found {} deposit(s) in currency '{}'", deposits.size(), currency);
            return deposits;
        } catch (SQLException e) {
            logger.error("Failed to fetch deposits by currency '" + currency + "'", e);
            throw e;
        }
    }

    /**
     * Returns all deposit products offered by the given bank.
     *
     * @param bankId the bank id
     * @return matching deposits, never {@code null}
     * @throws SQLException on any database error
     */
    public List<Deposit> getDepositsByBank(int bankId) throws SQLException {
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

    /**
     * Returns deposits whose interest rate falls within the given inclusive range.
     *
     * @param minRate lower bound (percent)
     * @param maxRate upper bound (percent)
     * @return matching deposits, never {@code null}
     * @throws IllegalArgumentException if minRate &gt; maxRate
     * @throws SQLException             on any database error
     */
    public List<Deposit> getDepositsByInterestRateRange(double minRate, double maxRate)
            throws SQLException {
        if (minRate > maxRate) {
            throw new IllegalArgumentException(
                    "minRate (" + minRate + ") must not exceed maxRate (" + maxRate + ")");
        }
        logger.debug("Fetching deposits with interest rate in [{}, {}]", minRate, maxRate);
        try {
            List<Deposit> deposits = depositRepository.findByInterestRateBetween(minRate, maxRate);
            logger.debug("Found {} deposit(s) in rate range [{}, {}]",
                    deposits.size(), minRate, maxRate);
            return deposits;
        } catch (SQLException e) {
            logger.error("Failed to fetch deposits by interest rate range", e);
            throw e;
        }
    }

    /**
     * Validates and persists a new client.
     *
     * @param client the client to create (id must be 0)
     * @return the saved client with its generated id
     * @throws IllegalArgumentException if validation fails
     * @throws SQLException             on any database error
     */
    public Client createClient(Client client) throws SQLException {
        logger.info("Creating client: name='{} {}'", client.getFirstName(), client.getLastName());
        try {
            validateClient(client);
            Client saved = clientRepository.save(client);
            logger.info("Client created: id={}, name='{}'", saved.getId(), saved.getFullName());
            return saved;
        } catch (SQLException e) {
            logger.error("Failed to create client '" + client.getFullName() + "'", e);
            throw e;
        }
    }

    /**
     * Validates and updates an existing client.
     *
     * @param client the client to update (id must be non-zero)
     * @return the updated client
     * @throws IllegalArgumentException if validation fails or id is 0
     * @throws SQLException             on any database error
     */
    public Client updateClient(Client client) throws SQLException {
        if (client.getId() == 0) {
            throw new IllegalArgumentException("Cannot update a client without an id");
        }
        logger.info("Updating client: id={}, name='{}'", client.getId(), client.getFullName());
        try {
            validateClient(client);
            Client updated = clientRepository.save(client);
            logger.info("Client updated: id={}", updated.getId());
            return updated;
        } catch (SQLException e) {
            logger.error("Failed to update client id=" + client.getId(), e);
            throw e;
        }
    }

    /**
     * Deletes a client by id.
     *
     * @param id the client id to remove
     * @throws SQLException on any database error
     */
    public void deleteClient(int id) throws SQLException {
        logger.info("Deleting client: id={}", id);
        try {
            clientRepository.delete(id);
            logger.info("Client deleted: id={}", id);
        } catch (SQLException e) {
            logger.error("Failed to delete client id=" + id, e);
            throw e;
        }
    }

    /**
     * Retrieves a client by id.
     *
     * @param id the client id
     * @return an {@link Optional} containing the client, or empty if not found
     * @throws SQLException on any database error
     */
    public Optional<Client> getClientById(int id) throws SQLException {
        logger.debug("Fetching client: id={}", id);
        try {
            Optional<Client> result = clientRepository.findById(id);
            if (result.isEmpty()) {
                logger.warn("Client not found: id={}", id);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Failed to fetch client id=" + id, e);
            throw e;
        }
    }

    /**
     * Returns all clients ordered by last name, then first name.
     *
     * @return list of all clients, never {@code null}
     * @throws SQLException on any database error
     */
    public List<Client> getAllClients() throws SQLException {
        logger.debug("Fetching all clients");
        try {
            List<Client> clients = clientRepository.findAll();
            logger.debug("Fetched {} client(s)", clients.size());
            return clients;
        } catch (SQLException e) {
            logger.error("Failed to fetch all clients", e);
            throw e;
        }
    }

    /**
     * Searches clients by last name substring. Returns all clients if the query is blank.
     *
     * @param lastName the last-name substring (case-insensitive)
     * @return matching clients, never {@code null}
     * @throws SQLException on any database error
     */
    public List<Client> searchClientsByLastName(String lastName) throws SQLException {
        logger.debug("Searching clients by lastName='{}'", lastName);
        try {
            List<Client> result;
            if (lastName == null || lastName.isBlank()) {
                result = clientRepository.findAll();
            } else {
                result = clientRepository.findByLastName(lastName);
            }
            logger.debug("Client search returned {} result(s)", result.size());
            return result;
        } catch (SQLException e) {
            logger.error("Failed to search clients by last name '" + lastName + "'", e);
            throw e;
        }
    }

    /**
     * Searches clients by id (if numeric), first name, or last name substring.
     * Returns all clients if the query is blank.
     *
     * @param query the search string
     * @return matching clients, never {@code null}
     * @throws SQLException on any database error
     */
    public List<Client> searchClients(String query) throws SQLException {
        logger.debug("Searching clients by query='{}'", query);
        try {
            List<Client> result;
            if (query == null || query.isBlank()) {
                result = clientRepository.findAll();
            } else {
                result = clientRepository.search(query);
            }
            logger.debug("Client search returned {} result(s)", result.size());
            return result;
        } catch (SQLException e) {
            logger.error("Failed to search clients by query '" + query + "'", e);
            throw e;
        }
    }

    /**
     * Opens a deposit for a client, enforcing the minimum amount constraint.
     *
     * @param clientId  the client id
     * @param depositId the deposit product id
     * @param amount    the amount to place; must be &ge; the deposit's minimum amount
     * @return the persisted {@link ClientDeposit} record with the DB-assigned id and timestamp
     * @throws IllegalArgumentException if the amount is below the minimum or the deposit is not found
     * @throws SQLException             on any database error
     */
    public ClientDeposit openDeposit(int clientId, int depositId, double amount)
            throws SQLException {
        logger.info("Opening deposit: clientId={}, depositId={}, amount={}",
                clientId, depositId, amount);
        try {
            Deposit deposit = depositRepository.findById(depositId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Deposit product not found: id=" + depositId));

            if (amount < deposit.getMinAmount()) {
                throw new IllegalArgumentException(
                        "Amount " + amount + " is below the minimum " + deposit.getMinAmount()
                        + " required by deposit '" + deposit.getName() + "'");
            }

            ClientDeposit record = clientDepositRepository.save(
                    new ClientDeposit(clientId, depositId, amount));
            logger.info("Deposit opened: clientDepositId={}, clientId={}, depositId={}, amount={}",
                    record.getId(), clientId, depositId, amount);
            return record;
        } catch (SQLException e) {
            logger.error("Failed to open deposit: clientId=" + clientId
                    + ", depositId=" + depositId, e);
            throw e;
        }
    }

    /**
     * Closes an active client deposit by marking its status as CLOSED.
     *
     * @param clientDepositId the client-deposit record id
     * @throws IllegalArgumentException if the record is not found
     * @throws SQLException             on any database error
     */
    public void closeDeposit(int clientDepositId) throws SQLException {
        logger.info("Closing client deposit: id={}", clientDepositId);
        try {
            ClientDeposit record = clientDepositRepository.findById(clientDepositId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Client deposit record not found: id=" + clientDepositId));
            record.setStatus("CLOSED");
            clientDepositRepository.save(record);
            logger.info("Client deposit closed: id={}", clientDepositId);
        } catch (SQLException e) {
            logger.error("Failed to close client deposit id=" + clientDepositId, e);
            throw e;
        }
    }

    /**
     * Calculates the expected interest income for a client's deposit over a given period.
     *
     * @param clientDepositId the client-deposit record id
     * @param months          the holding period in months
     * @return the interest income (not including the principal)
     * @throws IllegalArgumentException if either record is not found or months is not positive
     * @throws SQLException             on any database error
     */
    public double calculateExpectedReturn(int clientDepositId, int months) throws SQLException {
        if (months <= 0) {
            throw new IllegalArgumentException("Months must be positive, got: " + months);
        }
        logger.debug("Calculating expected return: clientDepositId={}, months={}",
                clientDepositId, months);
        try {
            ClientDeposit record = clientDepositRepository.findById(clientDepositId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Client deposit record not found: id=" + clientDepositId));
            Deposit deposit = depositRepository.findById(record.getDepositId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Deposit product not found: id=" + record.getDepositId()));
            double income = deposit.calculateProfit(record.getAmount(), months);
            logger.debug("Expected return for clientDepositId={}: {} over {} month(s)",
                    clientDepositId, income, months);
            return income;
        } catch (SQLException e) {
            logger.error("Failed to calculate return for clientDepositId=" + clientDepositId, e);
            throw e;
        }
    }

    /**
     * Returns enriched client-deposit rows for the given client with deposit and bank info,
     * and calculates expected profit for each record (months from opened_at to today).
     *
     * @param clientId the client id
     * @return list of detail rows ordered by opened_at descending, never {@code null}
     * @throws SQLException on any database error
     */
    public List<ClientDepositDetail> getClientDepositDetails(int clientId) throws SQLException {
        logger.debug("Fetching detailed client deposits: clientId={}", clientId);
        try {
            List<ClientDepositDetail> details =
                    clientDepositRepository.findDetailedByClientId(clientId);
            LocalDate today = LocalDate.now();
            for (ClientDepositDetail detail : details) {
                Deposit deposit = detail.getDeposit();
                int months = switch (deposit.getType()) {
                    case TERM    -> Math.max(1, deposit.getTermMonths());
                    case DEMAND  -> 1;
                    case SAVINGS -> {
                        int elapsed = 1;
                        String openedAt = detail.getOpenedAt();
                        if (openedAt != null && openedAt.length() >= 10) {
                            try {
                                LocalDate opened = LocalDate.parse(openedAt.substring(0, 10));
                                elapsed = Math.max(1, (int) ChronoUnit.MONTHS.between(opened, today));
                            } catch (Exception ignored) { }
                        }
                        yield elapsed;
                    }
                };
                detail.setExpectedProfit(deposit.calculateProfit(detail.getAmount(), months));
            }
            logger.debug("Fetched {} deposit detail(s) for clientId={}", details.size(), clientId);
            return details;
        } catch (SQLException e) {
            logger.error("Failed to fetch deposit details for clientId=" + clientId, e);
            throw e;
        }
    }

    /**
     * Returns all client-deposit records for the given client.
     *
     * @param clientId the client id
     * @return list of records ordered by opened_at descending, never {@code null}
     * @throws SQLException on any database error
     */
    public List<ClientDeposit> getClientDeposits(int clientId) throws SQLException {
        logger.debug("Fetching client deposits: clientId={}", clientId);
        try {
            List<ClientDeposit> records = clientDepositRepository.findByClientId(clientId);
            logger.debug("Found {} deposit record(s) for clientId={}", records.size(), clientId);
            return records;
        } catch (SQLException e) {
            logger.error("Failed to fetch client deposits for clientId=" + clientId, e);
            throw e;
        }
    }

    /**
     * Returns the first client that holds an ACTIVE deposit for the given deposit product, if any.
     *
     * @param depositId the deposit product id
     * @return an {@link Optional} containing the client, or empty if none is actively linked
     * @throws SQLException on any database error
     */
    public Optional<Client> getActiveClientForDeposit(int depositId) throws SQLException {
        logger.debug("Fetching active client for depositId={}", depositId);
        try {
            Optional<ClientDeposit> active = clientDepositRepository.findByDepositId(depositId)
                    .stream()
                    .filter(cd -> "ACTIVE".equals(cd.getStatus()))
                    .findFirst();
            if (active.isEmpty()) return Optional.empty();
            return clientRepository.findById(active.get().getClientId());
        } catch (SQLException e) {
            logger.error("Failed to fetch active client for depositId=" + depositId, e);
            throw e;
        }
    }

    /**
     * Returns the active {@link ClientDeposit} record for the given deposit product, if any.
     * Use this when both the linked client id and the invested amount are needed.
     *
     * @param depositId the deposit product id
     * @return an {@link Optional} containing the record, or empty if none is actively linked
     * @throws SQLException on any database error
     */
    public Optional<ClientDeposit> getActiveClientDepositRecordForDeposit(int depositId)
            throws SQLException {
        logger.debug("Fetching active client deposit record for depositId={}", depositId);
        try {
            return clientDepositRepository.findByDepositId(depositId)
                    .stream()
                    .filter(cd -> "ACTIVE".equals(cd.getStatus()))
                    .findFirst();
        } catch (SQLException e) {
            logger.error("Failed to fetch active client deposit record for depositId=" + depositId, e);
            throw e;
        }
    }

    /**
     * Returns only the ACTIVE client-deposit records for the given client.
     *
     * @param clientId the client id
     * @return list of active records, never {@code null}
     * @throws SQLException on any database error
     */
    public List<ClientDeposit> getActiveClientDeposits(int clientId) throws SQLException {
        logger.debug("Fetching active client deposits: clientId={}", clientId);
        try {
            List<ClientDeposit> records = clientDepositRepository.findByClientId(clientId)
                    .stream()
                    .filter(cd -> "ACTIVE".equals(cd.getStatus()))
                    .toList();
            logger.debug("Found {} ACTIVE deposit record(s) for clientId={}", records.size(), clientId);
            return records;
        } catch (SQLException e) {
            logger.error("Failed to fetch active client deposits for clientId=" + clientId, e);
            throw e;
        }
    }

    private void validateDeposit(Deposit deposit) {
        if (deposit.getName() == null || deposit.getName().isBlank()) {
            throw new IllegalArgumentException("Deposit name cannot be empty");
        }
        if (deposit.getInterestRate() <= 0) {
            throw new IllegalArgumentException("Interest rate must be positive");
        }
        if (deposit.getMinAmount() < 0) {
            throw new IllegalArgumentException("Minimum amount cannot be negative");
        }
        if (deposit.getBankId() <= 0) {
            throw new IllegalArgumentException("Deposit must be linked to a valid bank id");
        }
    }

    private void validateClient(Client client) {
        if (client.getFirstName() == null || client.getFirstName().isBlank()) {
            throw new IllegalArgumentException("Client first name cannot be empty");
        }
        if (client.getLastName() == null || client.getLastName().isBlank()) {
            throw new IllegalArgumentException("Client last name cannot be empty");
        }
    }
}
