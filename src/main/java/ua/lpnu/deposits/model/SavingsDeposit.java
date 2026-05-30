package ua.lpnu.deposits.model;

/**
 * Savings deposit: replenishment is allowed and interest is compounded monthly.
 * Early withdrawal is not permitted.
 */
public class SavingsDeposit extends Deposit {

    /**
     * Constructs a SavingsDeposit loaded from the database.
     *
     * @param id           unique identifier
     * @param bankId       id of the offering bank
     * @param name         product name
     * @param currency     currency code
     * @param minAmount    minimum opening amount
     * @param interestRate annual interest rate in percent
     * @param termMonths   deposit term in months
     */
    public SavingsDeposit(int id, int bankId, String name, String currency,
                          double minAmount, double interestRate, int termMonths) {
        super(id, bankId, name, DepositType.SAVINGS, currency, minAmount, interestRate,
                termMonths, false, true, 0.0);
    }

    /**
     * Constructs a new SavingsDeposit before persisting (id = 0).
     *
     * @param bankId       id of the offering bank
     * @param name         product name
     * @param currency     currency code
     * @param minAmount    minimum opening amount
     * @param interestRate annual interest rate in percent
     * @param termMonths   deposit term in months
     */
    public SavingsDeposit(int bankId, String name, String currency,
                          double minAmount, double interestRate, int termMonths) {
        super(bankId, name, DepositType.SAVINGS, currency, minAmount, interestRate,
                termMonths, false, true, 0.0);
    }

    /**
     * Calculates compound interest profit, compounded monthly over {@code months}.
     * Formula: principal * (1 + r/12)^months - principal, where r = interestRate / 100.
     *
     * @param principal the deposited amount
     * @param months    holding period in months
     * @return compound interest profit
     */
    @Override
    public double calculateProfit(double principal, int months) {
        double monthlyRate = getInterestRate() / 100.0 / 12.0;
        return principal * (Math.pow(1 + monthlyRate, months) - 1);
    }
}
