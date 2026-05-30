package ua.lpnu.deposits.model;

/**
 * Abstract base class representing a bank deposit product.
 * Concrete subclasses implement specific deposit behaviour (term, savings, demand).
 */
public abstract class Deposit {

    private int id;
    private int bankId;
    private String name;
    private DepositType type;
    private String currency;
    private double minAmount;
    private double interestRate;
    private int termMonths;
    private boolean canWithdrawEarly;
    private boolean canReplenish;
    private double penaltyRate;

    /**
     * Full constructor used when loading a deposit from the database.
     *
     * @param id               unique identifier
     * @param bankId           id of the owning bank
     * @param name             product name
     * @param type             deposit classification
     * @param currency         currency code (e.g. "UAH")
     * @param minAmount        minimum opening amount
     * @param interestRate     annual interest rate in percent
     * @param termMonths       deposit term in months
     * @param canWithdrawEarly whether early withdrawal is permitted
     * @param canReplenish     whether top-ups are permitted
     * @param penaltyRate      penalty rate applied on early withdrawal (percent)
     */
    protected Deposit(int id, int bankId, String name, DepositType type, String currency,
                      double minAmount, double interestRate, int termMonths,
                      boolean canWithdrawEarly, boolean canReplenish, double penaltyRate) {
        this.id = id;
        this.bankId = bankId;
        this.name = name;
        this.type = type;
        this.currency = currency;
        this.minAmount = minAmount;
        this.interestRate = interestRate;
        this.termMonths = termMonths;
        this.canWithdrawEarly = canWithdrawEarly;
        this.canReplenish = canReplenish;
        this.penaltyRate = penaltyRate;
    }

    /**
     * Constructor without id, used before persisting a new deposit.
     *
     * @param bankId           id of the owning bank
     * @param name             product name
     * @param type             deposit classification
     * @param currency         currency code (e.g. "UAH")
     * @param minAmount        minimum opening amount
     * @param interestRate     annual interest rate in percent
     * @param termMonths       deposit term in months
     * @param canWithdrawEarly whether early withdrawal is permitted
     * @param canReplenish     whether top-ups are permitted
     * @param penaltyRate      penalty rate applied on early withdrawal (percent)
     */
    protected Deposit(int bankId, String name, DepositType type, String currency,
                      double minAmount, double interestRate, int termMonths,
                      boolean canWithdrawEarly, boolean canReplenish, double penaltyRate) {
        this(0, bankId, name, type, currency, minAmount, interestRate, termMonths,
                canWithdrawEarly, canReplenish, penaltyRate);
    }

    /**
     * Calculates the interest profit earned on this deposit.
     * Each concrete subclass overrides this with its own formula
     * (simple interest, compound interest, or penalty-adjusted interest).
     *
     * @param principal the deposited amount
     * @param months    the actual holding period in months
     * @return interest profit (not including the principal)
     */
    public abstract double calculateProfit(double principal, int months);

    /**
     * Calculates the total amount returned to the client at the end of the period,
     * including the original principal.
     *
     * @param principal the deposited amount
     * @param months    the actual holding period in months
     * @return principal plus interest profit
     */
    public double calculateTotalReturn(double principal, int months) {
        return principal + calculateProfit(principal, months);
    }

    /** @return the deposit's database id */
    public int getId() {
        return id;
    }

    /** @param id the database id to set */
    public void setId(int id) {
        this.id = id;
    }

    /** @return the id of the bank that offers this deposit */
    public int getBankId() {
        return bankId;
    }

    /** @param bankId the bank id to set */
    public void setBankId(int bankId) {
        this.bankId = bankId;
    }

    /** @return the deposit product name */
    public String getName() {
        return name;
    }

    /** @param name the product name to set */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the deposit type */
    public DepositType getType() {
        return type;
    }

    /** @param type the deposit type to set */
    public void setType(DepositType type) {
        this.type = type;
    }

    /** @return the currency code */
    public String getCurrency() {
        return currency;
    }

    /** @param currency the currency code to set */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /** @return the minimum opening amount */
    public double getMinAmount() {
        return minAmount;
    }

    /** @param minAmount the minimum amount to set */
    public void setMinAmount(double minAmount) {
        this.minAmount = minAmount;
    }

    /** @return the annual interest rate in percent */
    public double getInterestRate() {
        return interestRate;
    }

    /** @param interestRate the annual interest rate to set */
    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    /** @return the deposit term in months */
    public int getTermMonths() {
        return termMonths;
    }

    /** @param termMonths the term in months to set */
    public void setTermMonths(int termMonths) {
        this.termMonths = termMonths;
    }

    /** @return true if early withdrawal is permitted */
    public boolean isCanWithdrawEarly() {
        return canWithdrawEarly;
    }

    /** @param canWithdrawEarly whether early withdrawal is permitted */
    public void setCanWithdrawEarly(boolean canWithdrawEarly) {
        this.canWithdrawEarly = canWithdrawEarly;
    }

    /** @return true if top-up replenishment is permitted */
    public boolean isCanReplenish() {
        return canReplenish;
    }

    /** @param canReplenish whether replenishment is permitted */
    public void setCanReplenish(boolean canReplenish) {
        this.canReplenish = canReplenish;
    }

    /** @return the early-withdrawal penalty rate in percent */
    public double getPenaltyRate() {
        return penaltyRate;
    }

    /** @param penaltyRate the penalty rate to set */
    public void setPenaltyRate(double penaltyRate) {
        this.penaltyRate = penaltyRate;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + ", name='" + name + "', bank=" + bankId
                + ", rate=" + interestRate + "%, term=" + termMonths + "m, currency=" + currency + '}';
    }
}
