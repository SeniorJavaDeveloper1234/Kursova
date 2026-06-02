package ua.lpnu.deposits.model;

/**
 * Read-only view object combining a {@code client_deposits} row with its
 * joined {@code deposits} and {@code banks} data, plus a calculated expected profit.
 * Used exclusively for display in the Clients tab; never persisted.
 */
public class ClientDepositDetail {

    private final int id;
    private final String depositName;
    private final String bankName;
    private final DepositType depositType;
    private final double amount;
    private final String openedAt;
    private final String status;
    private final Deposit deposit;
    private double expectedProfit;

    /**
     * Constructs a {@code ClientDepositDetail} from the JOIN query result.
     *
     * @param id          {@code client_deposits.id}
     * @param depositName deposit product name
     * @param bankName    bank name
     * @param depositType deposit type enum
     * @param amount      amount placed on deposit
     * @param openedAt    ISO-8601 opening datetime string
     * @param status      record status ("ACTIVE" or "CLOSED")
     * @param deposit     the {@link Deposit} instance used to call {@code calculateProfit}
     */
    public ClientDepositDetail(int id, String depositName, String bankName,
                               DepositType depositType, double amount,
                               String openedAt, String status, Deposit deposit) {
        this.id = id;
        this.depositName = depositName;
        this.bankName = bankName;
        this.depositType = depositType;
        this.amount = amount;
        this.openedAt = openedAt;
        this.status = status;
        this.deposit = deposit;
    }

    /**
     * Returns the {@code client_deposits} record id.
     *
     * @return the record id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the deposit product name.
     *
     * @return the deposit name
     */
    public String getDepositName() {
        return depositName;
    }

    /**
     * Returns the bank name.
     *
     * @return the bank name
     */
    public String getBankName() {
        return bankName;
    }

    /**
     * Returns the deposit type.
     *
     * @return the deposit type
     */
    public DepositType getDepositType() {
        return depositType;
    }

    /**
     * Returns the amount placed on this deposit.
     *
     * @return the amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Returns the ISO-8601 string for when this deposit was opened.
     *
     * @return the opened-at datetime string
     */
    public String getOpenedAt() {
        return openedAt;
    }

    /**
     * Returns the current status ("ACTIVE" or "CLOSED").
     *
     * @return the status string
     */
    public String getStatus() {
        return status;
    }

    /**
     * Returns the {@link Deposit} product instance (needed for profit calculation).
     *
     * @return the deposit product
     */
    public Deposit getDeposit() {
        return deposit;
    }

    /**
     * Returns the pre-calculated expected profit.
     *
     * @return the expected profit
     */
    public double getExpectedProfit() {
        return expectedProfit;
    }

    /**
     * Sets the expected profit (called by the service after construction).
     *
     * @param expectedProfit the computed profit value
     */
    public void setExpectedProfit(double expectedProfit) {
        this.expectedProfit = expectedProfit;
    }

    @Override
    public String toString() {
        return "ClientDepositDetail{id=" + id + ", deposit='" + depositName
                + "', bank='" + bankName + "', amount=" + amount + ", status='" + status + "'}";
    }
}
