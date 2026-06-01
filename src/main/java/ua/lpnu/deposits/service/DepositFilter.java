package ua.lpnu.deposits.service;

import ua.lpnu.deposits.model.DepositType;

/**
 * Immutable filter criteria passed to {@link SearchFilterService#applyFilters}.
 * Any field left {@code null} is not applied.
 * Construct via {@link #builder()}.
 */
public class DepositFilter {

    private final DepositType type;
    private final String currency;
    private final Integer bankId;
    private final Boolean canWithdrawEarly;
    private final Boolean canReplenish;
    private final Double minInterestRate;
    private final Double maxInterestRate;
    private final Double minMinAmount;
    private final Double maxMinAmount;
    private final Integer minTermMonths;
    private final Integer maxTermMonths;

    private DepositFilter(Builder b) {
        this.type = b.type;
        this.currency = b.currency;
        this.bankId = b.bankId;
        this.canWithdrawEarly = b.canWithdrawEarly;
        this.canReplenish = b.canReplenish;
        this.minInterestRate = b.minInterestRate;
        this.maxInterestRate = b.maxInterestRate;
        this.minMinAmount = b.minMinAmount;
        this.maxMinAmount = b.maxMinAmount;
        this.minTermMonths = b.minTermMonths;
        this.maxTermMonths = b.maxTermMonths;
    }

    /**
     * Returns the deposit type filter.
     *
     * @return deposit type filter, or {@code null} if not set
     */
    public DepositType getType() { return type; }

    /**
     * Returns the currency filter.
     *
     * @return currency filter, or {@code null} if not set
     */
    public String getCurrency() { return currency; }

    /**
     * Returns the bank id filter.
     *
     * @return bank id filter, or {@code null} if not set
     */
    public Integer getBankId() { return bankId; }

    /**
     * Returns the early-withdrawal filter.
     *
     * @return early-withdrawal filter, or {@code null} if not set
     */
    public Boolean getCanWithdrawEarly() { return canWithdrawEarly; }

    /**
     * Returns the replenishment filter.
     *
     * @return replenishment filter, or {@code null} if not set
     */
    public Boolean getCanReplenish() { return canReplenish; }

    /**
     * Returns the lower bound of the interest rate range filter.
     *
     * @return lower bound in percent, or {@code null} if not set
     */
    public Double getMinInterestRate() { return minInterestRate; }

    /**
     * Returns the upper bound of the interest rate range filter.
     *
     * @return upper bound in percent, or {@code null} if not set
     */
    public Double getMaxInterestRate() { return maxInterestRate; }

    /**
     * Returns the lower bound of the minimum-amount range filter.
     *
     * @return lower bound, or {@code null} if not set
     */
    public Double getMinMinAmount() { return minMinAmount; }

    /**
     * Returns the upper bound of the minimum-amount range filter.
     *
     * @return upper bound, or {@code null} if not set
     */
    public Double getMaxMinAmount() { return maxMinAmount; }

    /**
     * Returns the lower bound of the term-months range filter.
     *
     * @return lower bound in months, or {@code null} if not set
     */
    public Integer getMinTermMonths() { return minTermMonths; }

    /**
     * Returns the upper bound of the term-months range filter.
     *
     * @return upper bound in months, or {@code null} if not set
     */
    public Integer getMaxTermMonths() { return maxTermMonths; }

    /**
     * Creates a new {@link Builder}.
     *
     * @return a fresh builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link DepositFilter}.
     */
    public static class Builder {

        private DepositType type;
        private String currency;
        private Integer bankId;
        private Boolean canWithdrawEarly;
        private Boolean canReplenish;
        private Double minInterestRate;
        private Double maxInterestRate;
        private Double minMinAmount;
        private Double maxMinAmount;
        private Integer minTermMonths;
        private Integer maxTermMonths;

        /** Creates a new empty {@code Builder}. */
        public Builder() {}

        /**
         * Sets the deposit type to filter by.
         *
         * @param type deposit type to filter by
         * @return this builder
         */
        public Builder type(DepositType type) { this.type = type; return this; }

        /**
         * Sets the currency code to filter by.
         *
         * @param currency currency code to filter by (e.g. "UAH")
         * @return this builder
         */
        public Builder currency(String currency) { this.currency = currency; return this; }

        /**
         * Sets the bank id to filter by.
         *
         * @param bankId bank id to filter by
         * @return this builder
         */
        public Builder bankId(int bankId) { this.bankId = bankId; return this; }

        /**
         * Filters by early-withdrawal permission.
         *
         * @param val if {@code true}, only deposits allowing early withdrawal are kept
         * @return this builder
         */
        public Builder canWithdrawEarly(boolean val) { this.canWithdrawEarly = val; return this; }

        /**
         * Filters by replenishment permission.
         *
         * @param val if {@code true}, only deposits allowing replenishment are kept
         * @return this builder
         */
        public Builder canReplenish(boolean val) { this.canReplenish = val; return this; }

        /**
         * Inclusive interest rate range filter.
         *
         * @param min lower bound (percent)
         * @param max upper bound (percent)
         * @return this builder
         */
        public Builder interestRateRange(double min, double max) {
            this.minInterestRate = min;
            this.maxInterestRate = max;
            return this;
        }

        /**
         * Inclusive minimum-amount range filter.
         *
         * @param min lower bound
         * @param max upper bound
         * @return this builder
         */
        public Builder minAmountRange(double min, double max) {
            this.minMinAmount = min;
            this.maxMinAmount = max;
            return this;
        }

        /**
         * Inclusive term range filter (in months).
         *
         * @param min lower bound
         * @param max upper bound
         * @return this builder
         */
        public Builder termMonthsRange(int min, int max) {
            this.minTermMonths = min;
            this.maxTermMonths = max;
            return this;
        }

        /**
         * Builds the immutable {@link DepositFilter}.
         *
         * @return the constructed, immutable {@link DepositFilter}
         */
        public DepositFilter build() { return new DepositFilter(this); }
    }
}
