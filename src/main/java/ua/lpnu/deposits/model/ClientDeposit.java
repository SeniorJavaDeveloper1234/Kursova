package ua.lpnu.deposits.model;

/**
 * Represents a client's active or historical deposit record,
 * linking a {@link Client} to a {@link Deposit} product with a specific amount.
 */
public class ClientDeposit {

    private int id;
    private int clientId;
    private int depositId;
    private double amount;
    private String openedAt;
    private String status;

    /**
     * Constructs a ClientDeposit with all fields (loaded from DB).
     *
     * @param id        unique identifier
     * @param clientId  the owning client's id
     * @param depositId the deposit product id
     * @param amount    the amount placed on deposit
     * @param openedAt  ISO-8601 datetime when the deposit was opened
     * @param status    current status (e.g. "ACTIVE", "CLOSED")
     */
    public ClientDeposit(int id, int clientId, int depositId,
                         double amount, String openedAt, String status) {
        this.id = id;
        this.clientId = clientId;
        this.depositId = depositId;
        this.amount = amount;
        this.openedAt = openedAt;
        this.status = status;
    }

    /**
     * Constructs a new ClientDeposit before persisting (id = 0, status = "ACTIVE").
     *
     * @param clientId  the owning client's id
     * @param depositId the deposit product id
     * @param amount    the amount to place on deposit
     */
    public ClientDeposit(int clientId, int depositId, double amount) {
        this(0, clientId, depositId, amount, null, "ACTIVE");
    }

    /**
     * Returns the record's database id.
     *
     * @return the database id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the record's database id.
     *
     * @param id the database id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the client's database id.
     *
     * @return the client id
     */
    public int getClientId() {
        return clientId;
    }

    /**
     * Sets the client's database id.
     *
     * @param clientId the client id to set
     */
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    /**
     * Returns the deposit product's database id.
     *
     * @return the deposit product id
     */
    public int getDepositId() {
        return depositId;
    }

    /**
     * Sets the deposit product's database id.
     *
     * @param depositId the deposit product id to set
     */
    public void setDepositId(int depositId) {
        this.depositId = depositId;
    }

    /**
     * Returns the amount placed on this deposit.
     *
     * @return the deposit amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Sets the amount placed on this deposit.
     *
     * @param amount the amount to set
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * Returns the ISO-8601 datetime when this deposit was opened.
     *
     * @return the opening datetime string
     */
    public String getOpenedAt() {
        return openedAt;
    }

    /**
     * Sets the ISO-8601 datetime when this deposit was opened.
     *
     * @param openedAt the opening datetime to set
     */
    public void setOpenedAt(String openedAt) {
        this.openedAt = openedAt;
    }

    /**
     * Returns the current status of this deposit record.
     *
     * @return the status string (e.g. "ACTIVE", "CLOSED")
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the current status of this deposit record.
     *
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ClientDeposit{id=" + id + ", clientId=" + clientId + ", depositId=" + depositId
                + ", amount=" + amount + ", status='" + status + "'}";
    }
}
