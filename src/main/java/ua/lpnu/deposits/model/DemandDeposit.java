package ua.lpnu.deposits.model;

/**
 * Demand (on-call) deposit: funds are always available for withdrawal,
 * replenishment is allowed, and interest is calculated using simple interest
 * on the actual holding period. The rate is typically lower than term deposits.
 */
public class DemandDeposit extends Deposit {

    /**
     * Constructs a DemandDeposit loaded from the database.
     *
     * @param id           unique identifier
     * @param bankId       id of the offering bank
     * @param name         product name
     * @param currency     currency code
     * @param minAmount    minimum opening amount
     * @param interestRate annual interest rate in percent
     */
    public DemandDeposit(int id, int bankId, String name, String currency,
                         double minAmount, double interestRate) {
        super(id, bankId, name, DepositType.DEMAND, currency, minAmount, interestRate,
                0, true, true, 0.0);
    }

    /**
     * Constructs a new DemandDeposit before persisting (id = 0).
     *
     * @param bankId       id of the offering bank
     * @param name         product name
     * @param currency     currency code
     * @param minAmount    minimum opening amount
     * @param interestRate annual interest rate in percent
     */
    public DemandDeposit(int bankId, String name, String currency,
                         double minAmount, double interestRate) {
        super(bankId, name, DepositType.DEMAND, currency, minAmount, interestRate,
                0, true, true, 0.0);
    }

    /**
     * Calculates simple interest profit for the actual holding period.
     * Formula: principal * interestRate / 100 * months / 12.
     *
     * @param principal the deposited amount
     * @param months    actual holding period in months
     * @return interest profit
     */
    @Override
    public double calculateProfit(double principal, int months) {
        return principal * getInterestRate() / 100.0 * months / 12.0;
    }
}
