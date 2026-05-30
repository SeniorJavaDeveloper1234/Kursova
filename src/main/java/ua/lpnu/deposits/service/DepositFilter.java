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

    /** @return deposit type filter, or {@code null} if not set */
    public DepositType getType() { return type; }

    /** @return currency filter, or {@code null} if not set */
    public String getCurrency() { return currency; }

    /** @return bank id filter, or {@code null} if not set */
    public Integer getBankId() { return bankId; }

    /** @return early-withdrawal filter, or {@code null} if not set */
    public Boolean getCanWithdrawEarly() { return canWithdrawEarly; }

    /** @return replenishment filter, or {@code null} if not set */
    public Boolean getCanReplenish() { return canReplenish; }

    /** @return lower bound of interest rate range, or {@code null} if not set */
    public Double getMinInterestRate() { return minInterestRate; }

    /** @return upper bound of interest rate range, or {@code null} if not set */
    public Double getMaxInterestRate() { return maxInterestRate; }

    /** @return lower bound of min-amount range, or {@code null} if not set */
    public Double getMinMinAmount() { return minMinAmount; }

    /** @return upper bound of min-amount range, or {@code null} if not set */
    public Double getMaxMinAmount() { return maxMinAmount; }

    /** @return lower bound of term-months range, or {@code null} if not set */
    public Integer getMinTermMonths() { return minTermMonths; }

    /** @return upper bound of term-months range, or {@code null} if not set */
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

        /** @param type deposit type to filter by */
        public Builder type(DepositType type) { this.type = type; return this; }

        /** @param currency currency code to filter by (e.g. "UAH") */
        public Builder currency(String currency) { this.currency = currency; return this; }

        /** @param bankId bank id to filter by */
        public Builder bankId(int bankId) { this.bankId = bankId; return this; }

        /** @param val if {@code true}, only deposits allowing early withdrawal are kept */
        public Builder canWithdrawEarly(boolean val) { this.canWithdrawEarly = val; return this; }

        /** @param val if {@code true}, only deposits allowing replenishment are kept */
        public Builder canReplenish(boolean val) { this.canReplenish = val; return this; }

        /**
         * Inclusive interest rate range filter.
         *
         * @param min lower bound (percent)
         * @param max upper bound (percent)
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
         */
        public Builder termMonthsRange(int min, int max) {
            this.minTermMonths = min;
            this.maxTermMonths = max;
            return this;
        }

        /** @return the constructed, immutable {@link DepositFilter} */
        public DepositFilter build() { return new DepositFilter(this); }
    }
}
