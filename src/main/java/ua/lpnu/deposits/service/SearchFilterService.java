package ua.lpnu.deposits.service;

import ua.lpnu.deposits.model.Deposit;
import ua.lpnu.deposits.model.DepositType;
import ua.lpnu.deposits.util.AppLogger;

import java.util.Comparator;
import java.util.List;

/**
 * Stateless service for in-memory sorting, filtering and range searches over
 * lists of {@link Deposit} objects.
 * <p>
 * Every method returns a new list — the input list is never mutated.
 * Contains no SQL and no JavaFX imports.
 */
public class SearchFilterService {

    private static final AppLogger logger = AppLogger.getLogger(SearchFilterService.class);

    /**
     * Fields that can be used as a sort key in {@link #sort}.
     */
    public enum SortField {
        NAME,
        INTEREST_RATE,
        MIN_AMOUNT,
        TERM_MONTHS
    }

    /**
     * Sorts a list of deposits by the given field.
     *
     * @param deposits  the source list (not mutated)
     * @param field     the field to sort by
     * @param ascending {@code true} for ascending, {@code false} for descending
     * @return a new sorted list
     */
    public List<Deposit> sort(List<Deposit> deposits, SortField field, boolean ascending) {
        logger.debug("Sorting {} deposit(s) by {} {}", deposits.size(), field,
                ascending ? "ASC" : "DESC");
        Comparator<Deposit> comparator = switch (field) {
            case NAME          -> Comparator.comparing(Deposit::getName,
                                        String.CASE_INSENSITIVE_ORDER);
            case INTEREST_RATE -> Comparator.comparingDouble(Deposit::getInterestRate);
            case MIN_AMOUNT    -> Comparator.comparingDouble(Deposit::getMinAmount);
            case TERM_MONTHS   -> Comparator.comparingInt(Deposit::getTermMonths);
        };
        if (!ascending) {
            comparator = comparator.reversed();
        }
        return deposits.stream().sorted(comparator).toList();
    }

    /**
     * Filters deposits by type.
     *
     * @param deposits the source list
     * @param type     the deposit type to keep
     * @return a new filtered list
     */
    public List<Deposit> filterByType(List<Deposit> deposits, DepositType type) {
        logger.debug("Filtering {} deposit(s) by type={}", deposits.size(), type);
        List<Deposit> result = deposits.stream()
                .filter(d -> d.getType() == type)
                .toList();
        logger.debug("filterByType({}) retained {} deposit(s)", type, result.size());
        return result;
    }

    /**
     * Filters deposits by currency code (case-insensitive).
     *
     * @param deposits the source list
     * @param currency the currency code to keep (e.g. "UAH")
     * @return a new filtered list
     */
    public List<Deposit> filterByCurrency(List<Deposit> deposits, String currency) {
        logger.debug("Filtering {} deposit(s) by currency='{}'", deposits.size(), currency);
        List<Deposit> result = deposits.stream()
                .filter(d -> currency.equalsIgnoreCase(d.getCurrency()))
                .toList();
        logger.debug("filterByCurrency('{}') retained {} deposit(s)", currency, result.size());
        return result;
    }

    /**
     * Filters deposits to those offered by a specific bank.
     *
     * @param deposits the source list
     * @param bankId   the bank id to keep
     * @return a new filtered list
     */
    public List<Deposit> filterByBankId(List<Deposit> deposits, int bankId) {
        logger.debug("Filtering {} deposit(s) by bankId={}", deposits.size(), bankId);
        List<Deposit> result = deposits.stream()
                .filter(d -> d.getBankId() == bankId)
                .toList();
        logger.debug("filterByBankId({}) retained {} deposit(s)", bankId, result.size());
        return result;
    }

    /**
     * Filters deposits to those that allow early withdrawal.
     *
     * @param deposits the source list
     * @return a new filtered list
     */
    public List<Deposit> filterByWithdrawable(List<Deposit> deposits) {
        logger.debug("Filtering {} deposit(s) by canWithdrawEarly=true", deposits.size());
        List<Deposit> result = deposits.stream()
                .filter(Deposit::isCanWithdrawEarly)
                .toList();
        logger.debug("filterByWithdrawable retained {} deposit(s)", result.size());
        return result;
    }

    /**
     * Filters deposits to those that allow replenishment.
     *
     * @param deposits the source list
     * @return a new filtered list
     */
    public List<Deposit> filterByReplenishable(List<Deposit> deposits) {
        logger.debug("Filtering {} deposit(s) by canReplenish=true", deposits.size());
        List<Deposit> result = deposits.stream()
                .filter(Deposit::isCanReplenish)
                .toList();
        logger.debug("filterByReplenishable retained {} deposit(s)", result.size());
        return result;
    }

    /**
     * Keeps only deposits whose annual interest rate is within {@code [min, max]}.
     *
     * @param deposits the source list
     * @param min      lower bound (percent), inclusive
     * @param max      upper bound (percent), inclusive
     * @return a new filtered list
     * @throws IllegalArgumentException if min &gt; max
     */
    public List<Deposit> filterByInterestRateRange(List<Deposit> deposits,
                                                   double min, double max) {
        validateRange(min, max, "interest rate");
        logger.debug("Filtering {} deposit(s) by interest rate in [{}, {}]",
                deposits.size(), min, max);
        List<Deposit> result = deposits.stream()
                .filter(d -> d.getInterestRate() >= min && d.getInterestRate() <= max)
                .toList();
        logger.debug("filterByInterestRateRange retained {} deposit(s)", result.size());
        return result;
    }

    /**
     * Keeps only deposits whose minimum opening amount is within {@code [min, max]}.
     *
     * @param deposits the source list
     * @param min      lower bound, inclusive
     * @param max      upper bound, inclusive
     * @return a new filtered list
     * @throws IllegalArgumentException if min &gt; max
     */
    public List<Deposit> filterByMinAmountRange(List<Deposit> deposits,
                                                double min, double max) {
        validateRange(min, max, "minimum amount");
        logger.debug("Filtering {} deposit(s) by minAmount in [{}, {}]",
                deposits.size(), min, max);
        List<Deposit> result = deposits.stream()
                .filter(d -> d.getMinAmount() >= min && d.getMinAmount() <= max)
                .toList();
        logger.debug("filterByMinAmountRange retained {} deposit(s)", result.size());
        return result;
    }

    /**
     * Keeps only deposits whose term is within {@code [min, max]} months.
     * Demand deposits (term = 0) are excluded unless {@code min == 0}.
     *
     * @param deposits the source list
     * @param min      lower bound in months, inclusive
     * @param max      upper bound in months, inclusive
     * @return a new filtered list
     * @throws IllegalArgumentException if min &gt; max or either bound is negative
     */
    public List<Deposit> filterByTermMonthsRange(List<Deposit> deposits, int min, int max) {
        if (min < 0 || max < 0) {
            throw new IllegalArgumentException("Term bounds must be non-negative");
        }
        validateRange(min, max, "term months");
        logger.debug("Filtering {} deposit(s) by termMonths in [{}, {}]",
                deposits.size(), min, max);
        List<Deposit> result = deposits.stream()
                .filter(d -> d.getTermMonths() >= min && d.getTermMonths() <= max)
                .toList();
        logger.debug("filterByTermMonthsRange retained {} deposit(s)", result.size());
        return result;
    }

    /**
     * Applies all non-null criteria in {@code filter} sequentially.
     * Criteria are ANDed: each one narrows the previous result.
     *
     * @param deposits the source list
     * @param filter   the criteria to apply
     * @return a new filtered list matching all active criteria
     */
    public List<Deposit> applyFilters(List<Deposit> deposits, DepositFilter filter) {
        logger.debug("Applying combined filter to {} deposit(s)", deposits.size());
        List<Deposit> result = deposits;

        if (filter.getType() != null) {
            result = filterByType(result, filter.getType());
        }
        if (filter.getCurrency() != null && !filter.getCurrency().isBlank()) {
            result = filterByCurrency(result, filter.getCurrency());
        }
        if (filter.getBankId() != null) {
            result = filterByBankId(result, filter.getBankId());
        }
        if (Boolean.TRUE.equals(filter.getCanWithdrawEarly())) {
            result = filterByWithdrawable(result);
        }
        if (Boolean.TRUE.equals(filter.getCanReplenish())) {
            result = filterByReplenishable(result);
        }
        if (filter.getMinInterestRate() != null && filter.getMaxInterestRate() != null) {
            result = filterByInterestRateRange(result,
                    filter.getMinInterestRate(), filter.getMaxInterestRate());
        }
        if (filter.getMinMinAmount() != null && filter.getMaxMinAmount() != null) {
            result = filterByMinAmountRange(result,
                    filter.getMinMinAmount(), filter.getMaxMinAmount());
        }
        if (filter.getMinTermMonths() != null && filter.getMaxTermMonths() != null) {
            result = filterByTermMonthsRange(result,
                    filter.getMinTermMonths(), filter.getMaxTermMonths());
        }

        logger.debug("Combined filter result: {} deposit(s)", result.size());
        return result;
    }

    private void validateRange(double min, double max, String label) {
        if (min > max) {
            throw new IllegalArgumentException(
                    "Invalid " + label + " range: min=" + min + " > max=" + max);
        }
    }
}
