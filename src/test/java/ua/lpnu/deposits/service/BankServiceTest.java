package ua.lpnu.deposits.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lpnu.deposits.model.Bank;
import ua.lpnu.deposits.model.Deposit;
import ua.lpnu.deposits.model.TermDeposit;
import ua.lpnu.deposits.repository.BankRepository;
import ua.lpnu.deposits.repository.DepositRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {

    @Mock private BankRepository bankRepo;
    @Mock private DepositRepository depositRepo;
    @InjectMocks private BankService service;

    private Bank validBank;

    @BeforeEach
    void setUp() {
        validBank = new Bank("ПриватБанк", 8.5);
    }

    // --- createBank ---

    @Test
    void createBank_success() throws SQLException {
        Bank saved = new Bank(1, "ПриватБанк", 8.5);
        when(bankRepo.save(validBank)).thenReturn(saved);

        Bank result = service.createBank(validBank);

        assertSame(saved, result);
        verify(bankRepo).save(validBank);
    }

    @Test
    void createBank_blankName_throwsIAE() {
        Bank bank = new Bank("", 5.0);
        assertThrows(IllegalArgumentException.class, () -> service.createBank(bank));
        verifyNoInteractions(bankRepo);
    }

    @Test
    void createBank_nullName_throwsIAE() {
        Bank bank = new Bank(0, null, 5.0);
        assertThrows(IllegalArgumentException.class, () -> service.createBank(bank));
        verifyNoInteractions(bankRepo);
    }

    @Test
    void createBank_ratingBelowZero_throwsIAE() {
        Bank bank = new Bank("Test", -0.1);
        assertThrows(IllegalArgumentException.class, () -> service.createBank(bank));
    }

    @Test
    void createBank_ratingAboveTen_throwsIAE() {
        Bank bank = new Bank("Test", 10.1);
        assertThrows(IllegalArgumentException.class, () -> service.createBank(bank));
    }

    @Test
    void createBank_ratingExactlyZero_succeeds() throws SQLException {
        Bank bank = new Bank("Zero", 0.0);
        when(bankRepo.save(bank)).thenReturn(new Bank(1, "Zero", 0.0));
        assertNotNull(service.createBank(bank));
    }

    @Test
    void createBank_ratingExactlyTen_succeeds() throws SQLException {
        Bank bank = new Bank("Perfect", 10.0);
        when(bankRepo.save(bank)).thenReturn(new Bank(2, "Perfect", 10.0));
        assertNotNull(service.createBank(bank));
    }

    @Test
    void createBank_sqlException_rethrows() throws SQLException {
        when(bankRepo.save(any())).thenThrow(new SQLException("db error"));
        assertThrows(SQLException.class, () -> service.createBank(validBank));
    }

    // --- updateBank ---

    @Test
    void updateBank_success() throws SQLException {
        Bank bank = new Bank(1, "ПриватБанк", 8.5);
        when(bankRepo.save(bank)).thenReturn(bank);

        Bank result = service.updateBank(bank);

        assertSame(bank, result);
        verify(bankRepo).save(bank);
    }

    @Test
    void updateBank_idZero_throwsIAE() {
        Bank bank = new Bank("ПриватБанк", 8.5);  // id defaults to 0
        assertThrows(IllegalArgumentException.class, () -> service.updateBank(bank));
        verifyNoInteractions(bankRepo);
    }

    @Test
    void updateBank_blankName_throwsIAE() {
        Bank bank = new Bank(1, "   ", 5.0);
        assertThrows(IllegalArgumentException.class, () -> service.updateBank(bank));
    }

    @Test
    void updateBank_invalidRating_throwsIAE() {
        Bank bank = new Bank(1, "Test", -1.0);
        assertThrows(IllegalArgumentException.class, () -> service.updateBank(bank));
    }

    @Test
    void updateBank_sqlException_rethrows() throws SQLException {
        Bank bank = new Bank(1, "ПриватБанк", 8.5);
        when(bankRepo.save(any())).thenThrow(new SQLException("db error"));
        assertThrows(SQLException.class, () -> service.updateBank(bank));
    }

    // --- deleteBank ---

    @Test
    void deleteBank_success() throws SQLException {
        service.deleteBank(1);
        verify(bankRepo).delete(1);
    }

    @Test
    void deleteBank_sqlException_rethrows() throws SQLException {
        doThrow(new SQLException("db error")).when(bankRepo).delete(99);
        assertThrows(SQLException.class, () -> service.deleteBank(99));
    }

    // --- getBankById ---

    @Test
    void getBankById_found() throws SQLException {
        Bank bank = new Bank(1, "ПриватБанк", 8.5);
        when(bankRepo.findById(1)).thenReturn(Optional.of(bank));

        Optional<Bank> result = service.getBankById(1);

        assertTrue(result.isPresent());
        assertSame(bank, result.get());
    }

    @Test
    void getBankById_notFound_returnsEmpty() throws SQLException {
        when(bankRepo.findById(99)).thenReturn(Optional.empty());
        assertTrue(service.getBankById(99).isEmpty());
    }

    @Test
    void getBankById_sqlException_rethrows() throws SQLException {
        when(bankRepo.findById(1)).thenThrow(new SQLException("db error"));
        assertThrows(SQLException.class, () -> service.getBankById(1));
    }

    // --- getAllBanks ---

    @Test
    void getAllBanks_success() throws SQLException {
        List<Bank> banks = List.of(new Bank(1, "A", 5.0), new Bank(2, "B", 7.0));
        when(bankRepo.findAll()).thenReturn(banks);

        List<Bank> result = service.getAllBanks();

        assertEquals(2, result.size());
    }

    @Test
    void getAllBanks_empty_returnsEmptyList() throws SQLException {
        when(bankRepo.findAll()).thenReturn(List.of());
        assertTrue(service.getAllBanks().isEmpty());
    }

    @Test
    void getAllBanks_sqlException_rethrows() throws SQLException {
        when(bankRepo.findAll()).thenThrow(new SQLException("db error"));
        assertThrows(SQLException.class, () -> service.getAllBanks());
    }

    // --- searchByName ---

    @Test
    void searchByName_withQuery_callsFindByName() throws SQLException {
        List<Bank> banks = List.of(new Bank(1, "ПриватБанк", 8.5));
        when(bankRepo.findByName("Приват")).thenReturn(banks);

        List<Bank> result = service.searchByName("Приват");

        assertEquals(1, result.size());
        verify(bankRepo).findByName("Приват");
    }

    @Test
    void searchByName_blankQuery_callsFindAll() throws SQLException {
        List<Bank> all = List.of(new Bank(1, "A", 5.0));
        when(bankRepo.findAll()).thenReturn(all);

        List<Bank> result = service.searchByName("   ");

        assertEquals(1, result.size());
        verify(bankRepo).findAll();
        verify(bankRepo, never()).findByName(any());
    }

    @Test
    void searchByName_nullQuery_callsFindAll() throws SQLException {
        List<Bank> all = List.of(new Bank(1, "A", 5.0));
        when(bankRepo.findAll()).thenReturn(all);

        service.searchByName(null);

        verify(bankRepo).findAll();
        verify(bankRepo, never()).findByName(any());
    }

    @Test
    void searchByName_emptyString_callsFindAll() throws SQLException {
        when(bankRepo.findAll()).thenReturn(List.of());
        service.searchByName("");
        verify(bankRepo).findAll();
    }

    @Test
    void searchByName_sqlException_rethrows() throws SQLException {
        when(bankRepo.findByName(any())).thenThrow(new SQLException("db error"));
        assertThrows(SQLException.class, () -> service.searchByName("Test"));
    }

    // --- getDepositsForBank ---

    @Test
    void getDepositsForBank_success() throws SQLException {
        List<Deposit> deposits = List.of(
                new TermDeposit(1, 1, "Депозит А", "UAH", 1000.0, 12.0, 12, 2.0));
        when(depositRepo.findByBankId(1)).thenReturn(deposits);

        List<Deposit> result = service.getDepositsForBank(1);

        assertEquals(1, result.size());
        verify(depositRepo).findByBankId(1);
    }

    @Test
    void getDepositsForBank_empty_returnsEmptyList() throws SQLException {
        when(depositRepo.findByBankId(99)).thenReturn(List.of());
        assertTrue(service.getDepositsForBank(99).isEmpty());
    }

    @Test
    void getDepositsForBank_sqlException_rethrows() throws SQLException {
        when(depositRepo.findByBankId(1)).thenThrow(new SQLException("db error"));
        assertThrows(SQLException.class, () -> service.getDepositsForBank(1));
    }
}
