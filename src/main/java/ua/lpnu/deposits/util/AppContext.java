package ua.lpnu.deposits.util;

import ua.lpnu.deposits.repository.UserRepository;
import ua.lpnu.deposits.repository.impl.JdbcBankRepository;
import ua.lpnu.deposits.repository.impl.JdbcClientDepositRepository;
import ua.lpnu.deposits.repository.impl.JdbcClientRepository;
import ua.lpnu.deposits.repository.impl.JdbcDepositRepository;
import ua.lpnu.deposits.repository.impl.JdbcUserRepository;
import ua.lpnu.deposits.service.BankService;
import ua.lpnu.deposits.service.DepositService;
import ua.lpnu.deposits.service.SearchFilterService;

/**
 * Application-level singleton that owns all service and repository instances.
 * Must be initialised once via {@link #initialize()} before any controller accesses it.
 */
public class AppContext {

    private static volatile AppContext instance;

    private final BankService         bankService;
    private final DepositService      depositService;
    private final SearchFilterService searchFilterService;
    private final UserRepository      userRepository;

    private AppContext() throws Exception {
        JdbcBankRepository          bankRepo   = new JdbcBankRepository();
        JdbcDepositRepository       depositRepo = new JdbcDepositRepository();
        JdbcClientRepository        clientRepo  = new JdbcClientRepository();
        JdbcClientDepositRepository cdRepo      = new JdbcClientDepositRepository();
        JdbcUserRepository          userRepo    = new JdbcUserRepository();

        this.bankService          = new BankService(bankRepo, depositRepo);
        this.depositService       = new DepositService(depositRepo, clientRepo, cdRepo);
        this.searchFilterService  = new SearchFilterService();
        this.userRepository       = userRepo;

        userRepo.ensureDefaultAdmin();
        DataSeeder.seedIfEmpty();
    }

    /**
     * Creates the singleton on the first call; subsequent calls are no-ops.
     *
     * @throws Exception if the database connection or schema initialisation fails
     */
    public static void initialize() throws Exception {
        if (instance == null) {
            synchronized (AppContext.class) {
                if (instance == null) {
                    instance = new AppContext();
                }
            }
        }
    }

    /**
     * Returns the singleton instance.
     *
     * @return the application context
     * @throws IllegalStateException if {@link #initialize()} has not been called
     */
    public static AppContext getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AppContext not initialised — call initialize() first");
        }
        return instance;
    }

    /**
     * Returns the bank service.
     *
     * @return the bank service
     */
    public BankService getBankService() { return bankService; }

    /**
     * Returns the deposit service.
     *
     * @return the deposit service
     */
    public DepositService getDepositService() { return depositService; }

    /**
     * Returns the search-filter service.
     *
     * @return the search-filter service
     */
    public SearchFilterService getSearchFilterService() { return searchFilterService; }

    /**
     * Returns the user repository.
     *
     * @return the user repository
     */
    public UserRepository getUserRepository() { return userRepository; }
}
