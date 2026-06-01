package ua.lpnu.deposits.model;

/**
 * Fixed-term deposit: money is locked for the agreed term,
 * replenishment is not allowed, early withdrawal incurs a penalty.
 * Interest is calculated using simple (flat) interest.
 */
public class TermDeposit extends Deposit {

    /**
     * Constructs a TermDeposit loaded from the database.
     *
     * @param id           unique identifier
     * @param bankId       id of the offering bank
     * @param name         product name
     * @param currency     currency code
     * @param minAmount    minimum opening amount
     * @param interestRate annual interest rate in percent
     * @param termMonths   deposit term in months
     * @param penaltyRate  penalty rate for early withdrawal in percent
     */
    public TermDeposit(int id, int bankId, String name, String currency,
                       double minAmount, double interestRate, int termMonths, double penaltyRate) {
        super(id, bankId, name, DepositType.TERM, currency, minAmount, interestRate,
                termMonths, true, false, penaltyRate);
    }

    /**
     * Constructs a new TermDeposit before persisting (id = 0).
     *
     * @param bankId       id of the offering bank
     * @param name         product name
     * @param currency     currency code
     * @param minAmount    minimum opening amount
     * @param interestRate annual interest rate in percent
     * @param termMonths   deposit term in months
     * @param penaltyRate  penalty rate for early withdrawal in percent
     */
    public TermDeposit(int bankId, String name, String currency,
                       double minAmount, double interestRate, int termMonths, double penaltyRate) {
        super(bankId, name, DepositType.TERM, currency, minAmount, interestRate,
                termMonths, true, false, penaltyRate);
    }

    /**
     * Calculates simple interest for the given holding period.
     * If {@code months} is less than the contracted term, the penalty rate
     * is applied instead of the regular rate.
     *
     * @param principal the deposited amount
     * @param months    actual holding period in months
     * @return interest profit (clamped to zero if penalty exceeds earned interest)
     */
    @Override
    public double calculateProfit(double principal, int months) {
        if (months < getTermMonths()) {
            double earned = principal * getInterestRate() / 100.0 * months / 12.0;
            double penalty = principal * getPenaltyRate() / 100.0;
            return Math.max(earned - penalty, 0.0);
        }
        return principal * getInterestRate() / 100.0 * getTermMonths() / 12.0;
    }
}
