package ua.lpnu.deposits.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.lpnu.deposits.model.*;
import ua.lpnu.deposits.service.SearchFilterService.SortField;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchFilterServiceTest {

    private SearchFilterService service;

    // rates: termA=5, termB=15, savings=10, demand=3
    // amounts: termA=500, termB=2000, savings=1000, demand=100
    // terms:   termA=6,  termB=12,   savings=24,   demand=0
    // banks:   termA=1,  termB=2,    savings=1,    demand=2
    // currencies: termA=UAH, termB=USD, savings=UAH, demand=UAH
    private TermDeposit    termA;
    private TermDeposit    termB;
    private SavingsDeposit savings;
    private DemandDeposit  demand;

    @BeforeEach
    void setUp() {
        service = new SearchFilterService();
        termA   = new TermDeposit(1, 1, "А Депозит", "UAH", 500.0,  5.0,  6, 1.0);
        termB   = new TermDeposit(2, 2, "Б Депозит", "USD", 2000.0, 15.0, 12, 3.0);
        savings = new SavingsDeposit(3, 1, "В Ощадний", "UAH", 1000.0, 10.0, 24);
        demand  = new DemandDeposit(4,  2, "Г Вклад",   "UAH", 100.0,  3.0);
    }

    private List<Deposit> all() {
        return List.of(termA, termB, savings, demand);
    }

    // =========================================================
    // sort()
    // =========================================================

    @Test
    void sort_byName_ascending_firstIsA() {
        List<Deposit> sorted = service.sort(all(), SortField.NAME, true);
        assertEquals("А Депозит", sorted.get(0).getName());
        assertEquals("Г Вклад",   sorted.get(3).getName());
    }

    @Test
    void sort_byName_descending_firstIsG() {
        List<Deposit> sorted = service.sort(all(), SortField.NAME, false);
        assertEquals("Г Вклад",   sorted.get(0).getName());
        assertEquals("А Депозит", sorted.get(3).getName());
    }

    @Test
    void sort_byInterestRate_ascending() {
        List<Deposit> sorted = service.sort(all(), SortField.INTEREST_RATE, true);
        assertEquals(3.0,  sorted.get(0).getInterestRate());
        assertEquals(15.0, sorted.get(3).getInterestRate());
    }

    @Test
    void sort_byInterestRate_descending() {
        List<Deposit> sorted = service.sort(all(), SortField.INTEREST_RATE, false);
        assertEquals(15.0, sorted.get(0).getInterestRate());
        assertEquals(3.0,  sorted.get(3).getInterestRate());
    }

    @Test
    void sort_byMinAmount_ascending() {
        List<Deposit> sorted = service.sort(all(), SortField.MIN_AMOUNT, true);
        assertEquals(100.0,  sorted.get(0).getMinAmount());
        assertEquals(2000.0, sorted.get(3).getMinAmount());
    }

    @Test
    void sort_byMinAmount_descending() {
        List<Deposit> sorted = service.sort(all(), SortField.MIN_AMOUNT, false);
        assertEquals(2000.0, sorted.get(0).getMinAmount());
        assertEquals(100.0,  sorted.get(3).getMinAmount());
    }

    @Test
    void sort_byTermMonths_ascending_demandFirst() {
        List<Deposit> sorted = service.sort(all(), SortField.TERM_MONTHS, true);
        assertEquals(0,  sorted.get(0).getTermMonths());
        assertEquals(24, sorted.get(3).getTermMonths());
    }

    @Test
    void sort_byTermMonths_descending() {
        List<Deposit> sorted = service.sort(all(), SortField.TERM_MONTHS, false);
        assertEquals(24, sorted.get(0).getTermMonths());
        assertEquals(0,  sorted.get(3).getTermMonths());
    }

    @Test
    void sort_preservesSize() {
        assertEquals(4, service.sort(all(), SortField.NAME, true).size());
    }

    @Test
    void sort_emptyList_returnsEmpty() {
        assertTrue(service.sort(List.of(), SortField.NAME, true).isEmpty());
    }

    @Test
    void sort_singleElement_returnsSingle() {
        List<Deposit> result = service.sort(List.of(termA), SortField.INTEREST_RATE, true);
        assertEquals(1, result.size());
        assertSame(termA, result.get(0));
    }

    // =========================================================
    // filterByType()
    // =========================================================

    @Test
    void filterByType_TERM_returnsBothTermDeposits() {
        List<Deposit> result = service.filterByType(all(), DepositType.TERM);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(d -> d.getType() == DepositType.TERM));
    }

    @Test
    void filterByType_SAVINGS_returnsOne() {
        List<Deposit> result = service.filterByType(all(), DepositType.SAVINGS);
        assertEquals(1, result.size());
        assertSame(savings, result.get(0));
    }

    @Test
    void filterByType_DEMAND_returnsOne() {
        List<Deposit> result = service.filterByType(all(), DepositType.DEMAND);
        assertEquals(1, result.size());
        assertSame(demand, result.get(0));
    }

    @Test
    void filterByType_noMatch_returnsEmpty() {
        List<Deposit> onlyDemand = List.of(demand);
        assertTrue(service.filterByType(onlyDemand, DepositType.TERM).isEmpty());
    }

    // =========================================================
    // filterByCurrency()
    // =========================================================

    @Test
    void filterByCurrency_UAH_returnsThree() {
        List<Deposit> result = service.filterByCurrency(all(), "UAH");
        assertEquals(3, result.size());
    }

    @Test
    void filterByCurrency_USD_returnsOne() {
        List<Deposit> result = service.filterByCurrency(all(), "USD");
        assertEquals(1, result.size());
        assertSame(termB, result.get(0));
    }

    @Test
    void filterByCurrency_caseInsensitive_lowercase() {
        List<Deposit> result = service.filterByCurrency(all(), "uah");
        assertEquals(3, result.size());
    }

    @Test
    void filterByCurrency_noMatch_returnsEmpty() {
        assertTrue(service.filterByCurrency(all(), "EUR").isEmpty());
    }

    // =========================================================
    // filterByBankId()
    // =========================================================

    @Test
    void filterByBankId_bank1_returnsTermAAndSavings() {
        List<Deposit> result = service.filterByBankId(all(), 1);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(d -> d.getBankId() == 1));
    }

    @Test
    void filterByBankId_bank2_returnsTermBAndDemand() {
        List<Deposit> result = service.filterByBankId(all(), 2);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(d -> d.getBankId() == 2));
    }

    @Test
    void filterByBankId_noMatch_returnsEmpty() {
        assertTrue(service.filterByBankId(all(), 99).isEmpty());
    }

    // =========================================================
    // filterByWithdrawable()
    // TERM=canWithdraw, SAVINGS=cannot, DEMAND=canWithdraw
    // =========================================================

    @Test
    void filterByWithdrawable_returnsTermDepositsAndDemand() {
        List<Deposit> result = service.filterByWithdrawable(all());
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(Deposit::isCanWithdrawEarly));
    }

    @Test
    void filterByWithdrawable_emptyList_returnsEmpty() {
        assertTrue(service.filterByWithdrawable(List.of()).isEmpty());
    }

    // =========================================================
    // filterByReplenishable()
    // TERM=cannot, SAVINGS=canReplenish, DEMAND=canReplenish
    // =========================================================

    @Test
    void filterByReplenishable_returnsSavingsAndDemand() {
        List<Deposit> result = service.filterByReplenishable(all());
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(Deposit::isCanReplenish));
    }

    @Test
    void filterByReplenishable_emptyList_returnsEmpty() {
        assertTrue(service.filterByReplenishable(List.of()).isEmpty());
    }

    // =========================================================
    // filterByInterestRateRange()
    // rates: demand=3, termA=5, savings=10, termB=15
    // =========================================================

    @Test
    void filterByInterestRateRange_5to10_keepsTermAAndSavings() {
        List<Deposit> result = service.filterByInterestRateRange(all(), 5.0, 10.0);
        assertEquals(2, result.size());
    }

    @Test
    void filterByInterestRateRange_exactMatch_keepsSingle() {
        List<Deposit> result = service.filterByInterestRateRange(all(), 5.0, 5.0);
        assertEquals(1, result.size());
        assertEquals(5.0, result.get(0).getInterestRate());
    }

    @Test
    void filterByInterestRateRange_wideRange_keepsAll() {
        List<Deposit> result = service.filterByInterestRateRange(all(), 0.0, 100.0);
        assertEquals(4, result.size());
    }

    @Test
    void filterByInterestRateRange_noMatch_returnsEmpty() {
        assertTrue(service.filterByInterestRateRange(all(), 20.0, 30.0).isEmpty());
    }

    @Test
    void filterByInterestRateRange_minGreaterThanMax_throwsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> service.filterByInterestRateRange(all(), 10.0, 5.0));
    }

    // =========================================================
    // filterByMinAmountRange()
    // amounts: demand=100, termA=500, savings=1000, termB=2000
    // =========================================================

    @Test
    void filterByMinAmountRange_500to1000_keepsTermAAndSavings() {
        List<Deposit> result = service.filterByMinAmountRange(all(), 500.0, 1000.0);
        assertEquals(2, result.size());
    }

    @Test
    void filterByMinAmountRange_wideRange_keepsAll() {
        List<Deposit> result = service.filterByMinAmountRange(all(), 0.0, 9999.0);
        assertEquals(4, result.size());
    }

    @Test
    void filterByMinAmountRange_noMatch_returnsEmpty() {
        assertTrue(service.filterByMinAmountRange(all(), 5000.0, 10000.0).isEmpty());
    }

    @Test
    void filterByMinAmountRange_minGreaterThanMax_throwsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> service.filterByMinAmountRange(all(), 1000.0, 100.0));
    }

    // =========================================================
    // filterByTermMonthsRange()
    // terms: demand=0, termA=6, termB=12, savings=24
    // =========================================================

    @Test
    void filterByTermMonthsRange_6to12_keepsTermAAndTermB() {
        List<Deposit> result = service.filterByTermMonthsRange(all(), 6, 12);
        assertEquals(2, result.size());
    }

    @Test
    void filterByTermMonthsRange_0to6_keepsDemandAndTermA() {
        List<Deposit> result = service.filterByTermMonthsRange(all(), 0, 6);
        assertEquals(2, result.size());
    }

    @Test
    void filterByTermMonthsRange_0to0_keepsDemandOnly() {
        List<Deposit> result = service.filterByTermMonthsRange(all(), 0, 0);
        assertEquals(1, result.size());
        assertSame(demand, result.get(0));
    }

    @Test
    void filterByTermMonthsRange_wideRange_keepsAll() {
        List<Deposit> result = service.filterByTermMonthsRange(all(), 0, 999);
        assertEquals(4, result.size());
    }

    @Test
    void filterByTermMonthsRange_minGreaterThanMax_throwsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> service.filterByTermMonthsRange(all(), 12, 6));
    }

    @Test
    void filterByTermMonthsRange_negativeMin_throwsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> service.filterByTermMonthsRange(all(), -1, 12));
    }

    @Test
    void filterByTermMonthsRange_negativeMax_throwsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> service.filterByTermMonthsRange(all(), 0, -1));
    }

    // =========================================================
    // applyFilters()
    // =========================================================

    @Test
    void applyFilters_emptyFilter_returnsAll() {
        DepositFilter filter = DepositFilter.builder().build();
        assertEquals(4, service.applyFilters(all(), filter).size());
    }

    @Test
    void applyFilters_typeFilter_TERM() {
        DepositFilter filter = DepositFilter.builder().type(DepositType.TERM).build();
        assertEquals(2, service.applyFilters(all(), filter).size());
    }

    @Test
    void applyFilters_currencyFilter_USD() {
        DepositFilter filter = DepositFilter.builder().currency("USD").build();
        List<Deposit> result = service.applyFilters(all(), filter);
        assertEquals(1, result.size());
        assertSame(termB, result.get(0));
    }

    @Test
    void applyFilters_bankIdFilter() {
        DepositFilter filter = DepositFilter.builder().bankId(1).build();
        assertEquals(2, service.applyFilters(all(), filter).size());
    }

    @Test
    void applyFilters_canWithdrawFilter() {
        DepositFilter filter = DepositFilter.builder().canWithdrawEarly(true).build();
        assertEquals(3, service.applyFilters(all(), filter).size());
    }

    @Test
    void applyFilters_canReplenishFilter() {
        DepositFilter filter = DepositFilter.builder().canReplenish(true).build();
        assertEquals(2, service.applyFilters(all(), filter).size());
    }

    @Test
    void applyFilters_interestRateRange() {
        DepositFilter filter = DepositFilter.builder().interestRateRange(5.0, 10.0).build();
        assertEquals(2, service.applyFilters(all(), filter).size());
    }

    @Test
    void applyFilters_minAmountRange() {
        DepositFilter filter = DepositFilter.builder().minAmountRange(500.0, 1000.0).build();
        assertEquals(2, service.applyFilters(all(), filter).size());
    }

    @Test
    void applyFilters_termRange() {
        DepositFilter filter = DepositFilter.builder().termMonthsRange(6, 24).build();
        // keeps termA(6), termB(12), savings(24); excludes demand(0)
        assertEquals(3, service.applyFilters(all(), filter).size());
    }

    @Test
    void applyFilters_combinedTypeAndCurrency_returnsOnlyTermA() {
        DepositFilter filter = DepositFilter.builder()
                .type(DepositType.TERM)
                .currency("UAH")
                .build();
        List<Deposit> result = service.applyFilters(all(), filter);
        assertEquals(1, result.size());
        assertSame(termA, result.get(0));
    }

    @Test
    void applyFilters_combined_allCriteria_noMatch() {
        DepositFilter filter = DepositFilter.builder()
                .type(DepositType.SAVINGS)
                .currency("USD")
                .build();
        assertTrue(service.applyFilters(all(), filter).isEmpty());
    }

    @Test
    void applyFilters_blankCurrencyIgnored() {
        DepositFilter filter = DepositFilter.builder().currency("  ").build();
        // blank currency should be ignored per applyFilters logic
        assertEquals(4, service.applyFilters(all(), filter).size());
    }
}
