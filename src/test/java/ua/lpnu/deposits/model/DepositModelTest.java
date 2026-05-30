package ua.lpnu.deposits.model;

import org.junit.jupiter.api.Test;
import ua.lpnu.deposits.service.DepositFilter;

import static org.junit.jupiter.api.Assertions.*;

class DepositModelTest {

    // =========================================================
    // TermDeposit
    // =========================================================

    @Test
    void termDeposit_fullTerm_simpleInterest() {
        TermDeposit d = new TermDeposit(1, 1, "Test", "UAH", 1000.0, 12.0, 12, 2.0);
        // 1000 * 12% * 12/12 = 120
        assertEquals(120.0, d.calculateProfit(1000.0, 12), 0.001);
    }

    @Test
    void termDeposit_earlyWithdrawal_penaltySubtracted() {
        TermDeposit d = new TermDeposit(1, 1, "Test", "UAH", 1000.0, 12.0, 12, 2.0);
        // earned = 1000 * 12% * 6/12 = 60; penalty = 1000 * 2% = 20; income = 40
        assertEquals(40.0, d.calculateProfit(1000.0, 6), 0.001);
    }

    @Test
    void termDeposit_earlyWithdrawal_penaltyExceedsEarned_returnsZero() {
        TermDeposit d = new TermDeposit(1, 1, "Test", "UAH", 1000.0, 5.0, 12, 10.0);
        // earned = 1000 * 5% * 1/12 ≈ 4.17; penalty = 1000 * 10% = 100; max(4.17-100, 0) = 0
        assertEquals(0.0, d.calculateProfit(1000.0, 1), 0.001);
    }

    @Test
    void termDeposit_calculateTotalReturn_includesPrincipal() {
        TermDeposit d = new TermDeposit(1, 1, "Test", "UAH", 1000.0, 12.0, 12, 2.0);
        // 1000 + 120 = 1120
        assertEquals(1120.0, d.calculateTotalReturn(1000.0, 12), 0.001);
    }

    @Test
    void termDeposit_getters_allCorrect() {
        TermDeposit d = new TermDeposit(5, 2, "Alpha", "USD", 500.0, 8.0, 6, 1.5);
        assertEquals(5, d.getId());
        assertEquals(2, d.getBankId());
        assertEquals("Alpha", d.getName());
        assertEquals("USD", d.getCurrency());
        assertEquals(500.0, d.getMinAmount());
        assertEquals(8.0, d.getInterestRate());
        assertEquals(6, d.getTermMonths());
        assertEquals(1.5, d.getPenaltyRate());
        assertEquals(DepositType.TERM, d.getType());
        assertTrue(d.isCanWithdrawEarly());
        assertFalse(d.isCanReplenish());
    }

    @Test
    void termDeposit_noIdConstructor_idIsZero() {
        TermDeposit d = new TermDeposit(1, "Test", "UAH", 1000.0, 12.0, 12, 2.0);
        assertEquals(0, d.getId());
        assertEquals(1, d.getBankId());
    }

    @Test
    void termDeposit_toString_containsName() {
        TermDeposit d = new TermDeposit(1, 1, "MyDeposit", "UAH", 1000.0, 12.0, 12, 2.0);
        assertTrue(d.toString().contains("MyDeposit"));
    }

    // =========================================================
    // SavingsDeposit
    // =========================================================

    @Test
    void savingsDeposit_compoundInterest_greaterThanSimple() {
        SavingsDeposit d = new SavingsDeposit(1, 1, "Savings", "UAH", 1000.0, 12.0, 12);
        double result = d.calculateProfit(1000.0, 12);
        // compound (1000 * ((1.01)^12 - 1)) > simple (1000 * 12% = 120)
        assertTrue(result > 120.0);
        assertEquals(1000.0 * (Math.pow(1.01, 12) - 1), result, 0.001);
    }

    @Test
    void savingsDeposit_calculateTotalReturn() {
        SavingsDeposit d = new SavingsDeposit(1, 1, "Savings", "UAH", 1000.0, 12.0, 12);
        double interest = d.calculateProfit(1000.0, 12);
        assertEquals(1000.0 + interest, d.calculateTotalReturn(1000.0, 12), 0.001);
    }

    @Test
    void savingsDeposit_getters_typeAndFlags() {
        SavingsDeposit d = new SavingsDeposit(3, 1, "Beta", "UAH", 1000.0, 10.0, 24);
        assertEquals(DepositType.SAVINGS, d.getType());
        assertFalse(d.isCanWithdrawEarly());
        assertTrue(d.isCanReplenish());
        assertEquals(0.0, d.getPenaltyRate());
    }

    @Test
    void savingsDeposit_noIdConstructor() {
        SavingsDeposit d = new SavingsDeposit(1, "Test", "UAH", 500.0, 8.0, 6);
        assertEquals(0, d.getId());
    }

    // =========================================================
    // DemandDeposit
    // =========================================================

    @Test
    void demandDeposit_simpleInterest() {
        DemandDeposit d = new DemandDeposit(1, 1, "Demand", "UAH", 100.0, 6.0);
        // 1000 * 6% * 12/12 = 60
        assertEquals(60.0, d.calculateProfit(1000.0, 12), 0.001);
    }

    @Test
    void demandDeposit_partialYear() {
        DemandDeposit d = new DemandDeposit(1, 1, "Demand", "UAH", 100.0, 6.0);
        // 1000 * 6% * 6/12 = 30
        assertEquals(30.0, d.calculateProfit(1000.0, 6), 0.001);
    }

    @Test
    void demandDeposit_getters_typeAndFlags() {
        DemandDeposit d = new DemandDeposit(4, 2, "Gamma", "UAH", 100.0, 3.0);
        assertEquals(DepositType.DEMAND, d.getType());
        assertTrue(d.isCanWithdrawEarly());
        assertTrue(d.isCanReplenish());
        assertEquals(0, d.getTermMonths());
        assertEquals(0.0, d.getPenaltyRate());
    }

    @Test
    void demandDeposit_noIdConstructor() {
        DemandDeposit d = new DemandDeposit(2, "On-call", "USD", 50.0, 2.0);
        assertEquals(0, d.getId());
        assertEquals(2, d.getBankId());
    }

    // =========================================================
    // Deposit setters (tested via TermDeposit)
    // =========================================================

    @Test
    void deposit_allSetters() {
        TermDeposit d = new TermDeposit(1, 1, "Test", "UAH", 1000.0, 12.0, 12, 2.0);

        d.setId(99);
        d.setBankId(5);
        d.setName("Updated");
        d.setType(DepositType.SAVINGS);
        d.setCurrency("USD");
        d.setMinAmount(500.0);
        d.setInterestRate(8.0);
        d.setTermMonths(6);
        d.setCanWithdrawEarly(false);
        d.setCanReplenish(true);
        d.setPenaltyRate(1.0);

        assertEquals(99,   d.getId());
        assertEquals(5,    d.getBankId());
        assertEquals("Updated",     d.getName());
        assertEquals(DepositType.SAVINGS, d.getType());
        assertEquals("USD",         d.getCurrency());
        assertEquals(500.0,         d.getMinAmount());
        assertEquals(8.0,           d.getInterestRate());
        assertEquals(6,             d.getTermMonths());
        assertFalse(d.isCanWithdrawEarly());
        assertTrue(d.isCanReplenish());
        assertEquals(1.0,           d.getPenaltyRate());
        assertTrue(d.toString().contains("Updated"));
    }

    // =========================================================
    // Bank
    // =========================================================

    @Test
    void bank_fullConstructor_getters() {
        Bank b = new Bank(1, "ПриватБанк", 8.5);
        assertEquals(1, b.getId());
        assertEquals("ПриватБанк", b.getName());
        assertEquals(8.5, b.getRating());
    }

    @Test
    void bank_noIdConstructor_idIsZero() {
        Bank b = new Bank("Ощадбанк", 7.0);
        assertEquals(0, b.getId());
    }

    @Test
    void bank_setters() {
        Bank b = new Bank(1, "A", 5.0);
        b.setId(2);
        b.setName("B");
        b.setRating(9.0);

        assertEquals(2,   b.getId());
        assertEquals("B", b.getName());
        assertEquals(9.0, b.getRating());
        assertTrue(b.toString().contains("B"));
    }

    // =========================================================
    // Client
    // =========================================================

    @Test
    void client_fullConstructor_getters() {
        Client c = new Client(1, "Іван", "Петренко", "ivan@test.com");
        assertEquals(1,                c.getId());
        assertEquals("Іван",           c.getFirstName());
        assertEquals("Петренко",       c.getLastName());
        assertEquals("ivan@test.com",  c.getEmail());
        assertEquals("Іван Петренко",  c.getFullName());
    }

    @Test
    void client_noIdConstructor_idIsZero() {
        Client c = new Client("Олег", "Сидоренко", null);
        assertEquals(0, c.getId());
        assertEquals("Олег Сидоренко", c.getFullName());
    }

    @Test
    void client_setters() {
        Client c = new Client(1, "А", "Б", null);
        c.setId(5);
        c.setFirstName("Марія");
        c.setLastName("Коваль");
        c.setEmail("m@test.com");

        assertEquals(5,            c.getId());
        assertEquals("Марія Коваль", c.getFullName());
        assertEquals("m@test.com", c.getEmail());
        assertTrue(c.toString().contains("Коваль"));
    }

    // =========================================================
    // ClientDeposit
    // =========================================================

    @Test
    void clientDeposit_fullConstructor_getters() {
        ClientDeposit cd = new ClientDeposit(1, 2, 3, 5000.0, "2024-01-01", "ACTIVE");
        assertEquals(1,            cd.getId());
        assertEquals(2,            cd.getClientId());
        assertEquals(3,            cd.getDepositId());
        assertEquals(5000.0,       cd.getAmount());
        assertEquals("2024-01-01", cd.getOpenedAt());
        assertEquals("ACTIVE",     cd.getStatus());
    }

    @Test
    void clientDeposit_shortConstructor_defaultsActiveAndNullDate() {
        ClientDeposit cd = new ClientDeposit(1, 2, 3000.0);
        assertEquals(0,        cd.getId());
        assertEquals("ACTIVE", cd.getStatus());
        assertNull(cd.getOpenedAt());
    }

    @Test
    void clientDeposit_setters() {
        ClientDeposit cd = new ClientDeposit(1, 2, 3, 5000.0, "2024-01-01", "ACTIVE");
        cd.setId(10);
        cd.setClientId(20);
        cd.setDepositId(30);
        cd.setAmount(9999.0);
        cd.setOpenedAt("2025-06-01");
        cd.setStatus("CLOSED");

        assertEquals(10,          cd.getId());
        assertEquals(20,          cd.getClientId());
        assertEquals(30,          cd.getDepositId());
        assertEquals(9999.0,      cd.getAmount());
        assertEquals("2025-06-01",cd.getOpenedAt());
        assertEquals("CLOSED",    cd.getStatus());
        assertTrue(cd.toString().contains("CLOSED"));
    }

    // =========================================================
    // DepositFilter builder
    // =========================================================

    @Test
    void depositFilter_builder_allFields() {
        DepositFilter f = DepositFilter.builder()
                .type(DepositType.TERM)
                .currency("USD")
                .bankId(5)
                .canWithdrawEarly(true)
                .canReplenish(false)
                .interestRateRange(5.0, 15.0)
                .minAmountRange(500.0, 2000.0)
                .termMonthsRange(6, 24)
                .build();

        assertEquals(DepositType.TERM, f.getType());
        assertEquals("USD",            f.getCurrency());
        assertEquals(5,                f.getBankId());
        assertTrue(f.getCanWithdrawEarly());
        assertFalse(f.getCanReplenish());
        assertEquals(5.0,              f.getMinInterestRate());
        assertEquals(15.0,             f.getMaxInterestRate());
        assertEquals(500.0,            f.getMinMinAmount());
        assertEquals(2000.0,           f.getMaxMinAmount());
        assertEquals(6,                f.getMinTermMonths());
        assertEquals(24,               f.getMaxTermMonths());
    }

    @Test
    void depositFilter_builder_empty_allNull() {
        DepositFilter f = DepositFilter.builder().build();
        assertNull(f.getType());
        assertNull(f.getCurrency());
        assertNull(f.getBankId());
        assertNull(f.getCanWithdrawEarly());
        assertNull(f.getCanReplenish());
        assertNull(f.getMinInterestRate());
        assertNull(f.getMaxInterestRate());
        assertNull(f.getMinMinAmount());
        assertNull(f.getMaxMinAmount());
        assertNull(f.getMinTermMonths());
        assertNull(f.getMaxTermMonths());
    }
}
