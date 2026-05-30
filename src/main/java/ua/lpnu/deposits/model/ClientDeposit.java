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

    /** @return the record's database id */
    public int getId() {
        return id;
    }

    /** @param id the database id to set */
    public void setId(int id) {
        this.id = id;
    }

    /** @return the client's database id */
    public int getClientId() {
        return clientId;
    }

    /** @param clientId the client id to set */
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    /** @return the deposit product's database id */
    public int getDepositId() {
        return depositId;
    }

    /** @param depositId the deposit product id to set */
    public void setDepositId(int depositId) {
        this.depositId = depositId;
    }

    /** @return the amount placed on this deposit */
    public double getAmount() {
        return amount;
    }

    /** @param amount the amount to set */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /** @return the ISO-8601 datetime when this deposit was opened */
    public String getOpenedAt() {
        return openedAt;
    }

    /** @param openedAt the opening datetime to set */
    public void setOpenedAt(String openedAt) {
        this.openedAt = openedAt;
    }

    /** @return the current status of this deposit record */
    public String getStatus() {
        return status;
    }

    /** @param status the status to set */
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ClientDeposit{id=" + id + ", clientId=" + clientId + ", depositId=" + depositId
                + ", amount=" + amount + ", status='" + status + "'}";
    }
}
