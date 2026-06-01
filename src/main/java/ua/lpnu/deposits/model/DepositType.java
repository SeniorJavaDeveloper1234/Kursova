package ua.lpnu.deposits.model;

/**
 * Enumeration of supported deposit types matching the database CHECK constraint.
 */
public enum DepositType {
    /** Fixed-term deposit — money is locked for an agreed period. */
    TERM,
    /** Savings deposit — replenishment allowed, compound interest. */
    SAVINGS,
    /** Demand (on-call) deposit — funds always available for withdrawal. */
    DEMAND
}
