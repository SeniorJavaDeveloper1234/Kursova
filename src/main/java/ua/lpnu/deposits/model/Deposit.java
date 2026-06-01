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

    /**
     * Returns the deposit's database id.
     *
     * @return the database id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the deposit's database id.
     *
     * @param id the database id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the id of the bank that offers this deposit.
     *
     * @return the bank id
     */
    public int getBankId() {
        return bankId;
    }

    /**
     * Sets the id of the bank that offers this deposit.
     *
     * @param bankId the bank id to set
     */
    public void setBankId(int bankId) {
        this.bankId = bankId;
    }

    /**
     * Returns the deposit product name.
     *
     * @return the product name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the deposit product name.
     *
     * @param name the product name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the deposit type.
     *
     * @return the deposit type
     */
    public DepositType getType() {
        return type;
    }

    /**
     * Sets the deposit type.
     *
     * @param type the deposit type to set
     */
    public void setType(DepositType type) {
        this.type = type;
    }

    /**
     * Returns the currency code.
     *
     * @return the currency code (e.g. "UAH")
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Sets the currency code.
     *
     * @param currency the currency code to set
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Returns the minimum opening amount.
     *
     * @return the minimum amount
     */
    public double getMinAmount() {
        return minAmount;
    }

    /**
     * Sets the minimum opening amount.
     *
     * @param minAmount the minimum amount to set
     */
    public void setMinAmount(double minAmount) {
        this.minAmount = minAmount;
    }

    /**
     * Returns the annual interest rate in percent.
     *
     * @return the annual interest rate
     */
    public double getInterestRate() {
        return interestRate;
    }

    /**
     * Sets the annual interest rate in percent.
     *
     * @param interestRate the annual interest rate to set
     */
    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    /**
     * Returns the deposit term in months.
     *
     * @return the term in months
     */
    public int getTermMonths() {
        return termMonths;
    }

    /**
     * Sets the deposit term in months.
     *
     * @param termMonths the term in months to set
     */
    public void setTermMonths(int termMonths) {
        this.termMonths = termMonths;
    }

    /**
     * Returns whether early withdrawal is permitted.
     *
     * @return {@code true} if early withdrawal is permitted
     */
    public boolean isCanWithdrawEarly() {
        return canWithdrawEarly;
    }

    /**
     * Sets whether early withdrawal is permitted.
     *
     * @param canWithdrawEarly {@code true} to permit early withdrawal
     */
    public void setCanWithdrawEarly(boolean canWithdrawEarly) {
        this.canWithdrawEarly = canWithdrawEarly;
    }

    /**
     * Returns whether top-up replenishment is permitted.
     *
     * @return {@code true} if replenishment is permitted
     */
    public boolean isCanReplenish() {
        return canReplenish;
    }

    /**
     * Sets whether top-up replenishment is permitted.
     *
     * @param canReplenish {@code true} to permit replenishment
     */
    public void setCanReplenish(boolean canReplenish) {
        this.canReplenish = canReplenish;
    }

    /**
     * Returns the early-withdrawal penalty rate in percent.
     *
     * @return the penalty rate
     */
    public double getPenaltyRate() {
        return penaltyRate;
    }

    /**
     * Sets the early-withdrawal penalty rate in percent.
     *
     * @param penaltyRate the penalty rate to set
     */
    public void setPenaltyRate(double penaltyRate) {
        this.penaltyRate = penaltyRate;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + ", name='" + name + "', bank=" + bankId
                + ", rate=" + interestRate + "%, term=" + termMonths + "m, currency=" + currency + '}';
    }
}
