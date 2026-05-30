package ua.lpnu.deposits.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lpnu.deposits.model.*;
import ua.lpnu.deposits.repository.ClientDepositRepository;
import ua.lpnu.deposits.repository.ClientRepository;
import ua.lpnu.deposits.repository.DepositRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositServiceTest {

    @Mock private DepositRepository depositRepo;
    @Mock private ClientRepository clientRepo;
    @Mock private ClientDepositRepository cdRepo;
    @InjectMocks private DepositService service;

    // ---- helpers ----

    private TermDeposit termDeposit(int id) {
        return new TermDeposit(id, 1, "Стандартний", "UAH", 1000.0, 12.0, 12, 2.0);
    }

    private Client client(int id) {
        return new Client(id, "Іван", "Петренко", "ivan@example.com");
    }

    private ClientDeposit activeRecord(int id, int depositId, double amount) {
        return new ClientDeposit(id, 1, depositId, amount, "2024-01-01", "ACTIVE");
    }

    // =========================================================
    // createDeposit
    // =========================================================

    @Test
    void createDeposit_success() throws SQLException {
        Deposit d = new TermDeposit(1, "Стандартний", "UAH", 1000.0, 12.0, 12, 2.0);
        Deposit saved = termDeposit(1);
        when(depositRepo.save(d)).thenReturn(saved);

        Deposit result = service.createDeposit(d);

        assertSame(saved, result);
        verify(depositRepo).save(d);
    }

    @Test
    void createDeposit_blankName_throwsIAE() {
        Deposit d = new TermDeposit(1, "", "UAH", 1000.0, 12.0, 12, 2.0);
        assertThrows(IllegalArgumentException.class, () -> service.createDeposit(d));
        verifyNoInteractions(depositRepo);
    }

    @Test
    void createDeposit_nullName_throwsIAE() {
        Deposit d = new TermDeposit(1, null, "UAH", 1000.0, 12.0, 12, 2.0);
        assertThrows(IllegalArgumentException.class, () -> service.createDeposit(d));
    }

    @Test
    void createDeposit_zeroInterestRate_throwsIAE() {
        Deposit d = new TermDeposit(1, "Test", "UAH", 1000.0, 0.0, 12, 2.0);
        assertThrows(IllegalArgumentException.class, () -> service.createDeposit(d));
    }

    @Test
    void createDeposit_negativeInterestRate_throwsIAE() {
        Deposit d = new TermDeposit(1, "Test", "UAH", 1000.0, -5.0, 12, 2.0);
        assertThrows(IllegalArgumentException.class, () -> service.createDeposit(d));
    }

    @Test
    void createDeposit_negativeMinAmount_throwsIAE() {
        Deposit d = new TermDeposit(1, "Test", "UAH", -1.0, 12.0, 12, 2.0);
        assertThrows(IllegalArgumentException.class, () -> service.createDeposit(d));
    }

    @Test
    void createDeposit_zeroBankId_throwsIAE() {
        Deposit d = new TermDeposit(0, "Test", "UAH", 1000.0, 12.0, 12, 2.0);
        assertThrows(IllegalArgumentException.class, () -> service.createDeposit(d));
    }

    @Test
    void createDeposit_sqlException_rethrows() throws SQLException {
        Deposit d = new TermDeposit(1, "Test", "UAH", 1000.0, 12.0, 12, 2.0);
        when(depositRepo.save(any())).thenThrow(new SQLException("db error"));
        assertThrows(SQLException.class, () -> service.createDeposit(d));
    }

    // =========================================================
    // updateDeposit
    // =========================================================

    @Test
    void updateDeposit_success() throws SQLException {
        Deposit d = termDeposit(5);
        when(depositRepo.save(d)).thenReturn(d);

        Deposit result = service.updateDeposit(d);

        assertSame(d, result);
        verify(depositRepo).save(d);
    }

    @Test
    void updateDeposit_idZero_throwsIAE() {
        Deposit d = new TermDeposit(1, "Test", "UAH", 1000.0, 12.0, 12, 2.0); // id=0
        assertThrows(IllegalArgumentException.class, () -> service.updateDeposit(d));
        verifyNoInteractions(depositRepo);
    }

    @Test
    void updateDeposit_blankName_throwsIAE() {
        Deposit d = termDeposit(1);
        d.setName("  ");
        assertThrows(IllegalArgumentException.class, () -> service.updateDeposit(d));
    }

    @Test
    void updateDeposit_sqlException_rethrows() throws SQLException {
        Deposit d = termDeposit(1);
        when(depositRepo.save(any())).thenThrow(new SQLException("db error"));
        assertThrows(SQLException.class, () -> service.updateDeposit(d));
    }

    // =========================================================
    // deleteDeposit
    // =========================================================

    @Test
    void deleteDeposit_success() throws SQLException {
        service.deleteDeposit(1);
        verify(depositRepo).delete(1);
    }

    @Test
    void deleteDeposit_sqlException_rethrows() throws SQLException {
        doThrow(new SQLException("db error")).when(depositRepo).delete(1);
        assertThrows(SQLException.class, () -> service.deleteDeposit(1));
    }

    // =========================================================
    // getDepositById
    // =========================================================

    @Test
    void getDepositById_found() throws SQLException {
        Deposit d = termDeposit(1);
        when(depositRepo.findById(1)).thenReturn(Optional.of(d));

        Optional<Deposit> result = service.getDepositById(1);

        assertTrue(result.isPresent());
        assertSame(d, result.get());
    }

    @Test
    void getDepositById_notFound_returnsEmpty() throws SQLException {
        when(depositRepo.findById(99)).thenReturn(Optional.empty());
        assertTrue(service.getDepositById(99).isEmpty());
    }

    @Test
    void getDepositById_sqlException_rethrows() throws SQLException {
        when(depositRepo.findById(1)).thenThrow(new SQLException());
        assertThrows(SQLException.class, () -> service.getDepositById(1));
    }

    // =========================================================
    // getAllDeposits
    // =========================================================

    @Test
    void getAllDeposits_success() throws SQLException {
        when(depositRepo.findAll()).thenReturn(List.of(termDeposit(1), termDeposit(2)));
        assertEquals(2, service.getAllDeposits().size());
    }

    @Test
    void getAllDeposits_empty() throws SQLException {
        when(depositRepo.findAll()).thenReturn(List.of());
        assertTrue(service.getAllDeposits().isEmpty());
    }

    @Test
    void getAllDeposits_sqlException_rethrows() throws SQLException {
        when(depositRepo.findAll()).thenThrow(new SQLException());
        assertThrows(SQLException.class, () -> service.getAllDeposits());
    }

    // =========================================================
    // getDepositsByType
    // =========================================================

    @Test
    void getDepositsByType_TERM_success() throws SQLException {
        when(depositRepo.findByType(DepositType.TERM)).thenReturn(List.of(termDeposit(1)));
        assertEquals(1, service.getDepositsByType(DepositType.TERM).size());
    }

    @Test
    void getDepositsByType_sqlException_rethrows() throws SQLException {
        when(depositRepo.findByType(any())).thenThrow(new SQLException());
        assertThrows(SQLException.class, () -> service.getDepositsByType(DepositType.SAVINGS));
    }

    // =========================================================
    // getDepositsByCurrency
    // =========================================================

    @Test
    void getDepositsByCurrency_success() throws SQLException {
        when(depositRepo.findByCurrency("UAH")).thenReturn(List.of(termDeposit(1)));
        assertEquals(1, service.getDepositsByCurrency("UAH").size());
    }

    @Test
    void getDepositsByCurrency_sqlException_rethrows() throws SQLException {
        when(depositRepo.findByCurrency(any())).thenThrow(new SQLException());
        assertThrows(SQLException.class, () -> service.getDepositsByCurrency("UAH"));
    }

    // =========================================================
    // getDepositsByBank
    // =========================================================

    @Test
    void getDepositsByBank_success() throws SQLException {
        when(depositRepo.findByBankId(1)).thenReturn(List.of(termDeposit(1)));
        assertEquals(1, service.getDepositsByBank(1).size());
    }

    @Test
    void getDepositsByBank_sqlException_rethrows() throws SQLException {
        when(depositRepo.findByBankId(anyInt())).thenThrow(new SQLException());
        assertThrows(SQLException.class, () -> service.getDepositsByBank(1));
    }

    // =========================================================
    // getDepositsByInterestRateRange
    // =========================================================

    @Test
    void getDepositsByInterestRateRange_success() throws SQLException {
        when(depositRepo.findByInterestRateBetween(5.0, 15.0)).thenReturn(List.of(termDeposit(1)));
        assertEquals(1, service.getDepositsByInterestRateRange(5.0, 15.0).size());
    }

    @Test
    void getDepositsByInterestRateRange_equalBounds_success() throws SQLException {
        when(depositRepo.findByInterestRateBetween(12.0, 12.0)).thenReturn(List.of(termDeposit(1)));
        assertEquals(1, service.getDepositsByInterestRateRange(12.0, 12.0).size());
    }

    @Test
    void getDepositsByInterestRateRange_minGreaterThanMax_throwsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getDepositsByInterestRateRange(15.0, 5.0));
        verifyNoInteractions(depositRepo);
    }

    @Test
    void getDepositsByInterestRateRange_sqlException_rethrows() throws SQLException {
        when(depositRepo.findByInterestRateBetween(anyDouble(), anyDouble()))
                .thenThrow(new SQLException());
        assertThrows(SQLException.class,
                () -> service.getDepositsByInterestRateRange(5.0, 15.0));
    }

    // =========================================================
    // createClient
    // =========================================================

    @Test
    void createClient_success() throws SQLException {
        Client c = new Client("Іван", "Петренко", "ivan@example.com");
        Client saved = client(1);
        when(clientRepo.save(c)).thenReturn(saved);

        Client result = service.createClient(c);

        assertSame(saved, result);
        verify(clientRepo).save(c);
    }

    @Test
    void createClient_blankFirstName_throwsIAE() {
        Client c = new Client("", "Петренко", null);
        assertThrows(IllegalArgumentException.class, () -> service.createClient(c));
        verifyNoInteractions(clientRepo);
    }

    @Test
    void createClient_nullFirstName_throwsIAE() {
        Client c = new Client(0, null, "Петренко", null);
        assertThrows(IllegalArgumentException.class, () -> service.createClient(c));
    }

    @Test
    void createClient_blankLastName_throwsIAE() {
        Client c = new Client("Іван", "   ", null);
        assertThrows(IllegalArgumentException.class, () -> service.createClient(c));
    }

    @Test
    void createClient_nullLastName_throwsIAE() {
        Client c = new Client(0, "Іван", null, null);
        assertThrows(IllegalArgumentException.class, () -> service.createClient(c));
    }

    @Test
    void createClient_sqlException_rethrows() throws SQLException {
        Client c = new Client("Іван", "Петренко", null);
        when(clientRepo.save(any())).thenThrow(new SQLException());
        assertThrows(SQLException.class, () -> service.createClient(c));
    }

    // =========================================================
    // updateClient
    // =========================================================

    @Test
    void updateClient_success() throws SQLException {
        Client c = client(1);
        when(clientRepo.save(c)).thenReturn(c);

        Client result = service.updateClient(c);

        assertSame(c, result);
        verify(clientRepo).save(c);
    }

    @Test
    void updateClient_idZero_throwsIAE() {
        Client c = new Client("Іван", "Петренко", null); // id=0
        assertThrows(IllegalArgumentException.class, () -> service.updateClient(c));
        verifyNoInteractions(clientRepo);
    }

    @Test
    void updateClient_blankFirstName_throwsIAE() {
        Client c = client(1);
        c.setFirstName("");
        assertThrows(IllegalArgumentException.class, () -> service.updateClient(c));
    }

    @Test
    void updateClient_sqlException_rethrows() throws SQLException {
        Client c = client(1);
        when(clientRepo.save(any())).thenThrow(new SQLException());
        assertThrows(SQLException.class, () -> service.updateClient(c));
    }

    // =========================================================
    // deleteClient
    // =========================================================

    @Test
    void deleteClient_success() throws SQLException {
        service.deleteClient(1);
        verify(clientRepo).delete(1);
    }

    @Test
    void deleteClient_sqlException_rethrows() throws SQLException {
        doThrow(new SQLException()).when(clientRepo).delete(1);
        assertThrows(SQLException.class, () -> service.deleteClient(1));
    }

    // =========================================================
    // getClientById
    // =========================================================

    @Test
    void getClientById_found() throws SQLException {
        when(clientRepo.findById(1)).thenReturn(Optional.of(client(1)));
        assertTrue(service.getClientById(1).isPresent());
    }

    @Test
    void getClientById_notFound_returnsEmpty() throws SQLException {
        when(clientRepo.findById(99)).thenReturn(Optional.empty());
        assertTrue(service.getClientById(99).isEmpty());
    }

    @Test
    void getClientById_sqlException_rethrows() throws SQLException {
        when(clientRepo.findById(1)).thenThrow(new SQLException());
        assertThrows(SQLException.class, () -> service.getClientById(1));
    }

    // =========================================================
    // getAllClients
    // =========================================================

    @Test
    void getAllClients_success() throws SQLException {
        when(clientRepo.findAll()).thenReturn(List.of(client(1), client(2)));
        assertEquals(2, service.getAllClients().size());
    }

    @Test
    void getAllClients_sqlException_rethrows() throws SQLException {
        when(clientRepo.findAll()).thenThrow(new SQLException());
        assertThrows(SQLException.class, () -> service.getAllClients());
    }

    // =========================================================
    // searchClientsByLastName
    // =========================================================

    @Test
    void searchClientsByLastName_withName_callsFindByLastName() throws SQLException {
        when(clientRepo.findByLastName("Петренко")).thenReturn(List.of(client(1)));
        assertEquals(1, service.searchClientsByLastName("Петренко").size());
        verify(clientRepo).findByLastName("Петренко");
    }

    @Test
    void searchClientsByLastName_blank_callsFindAll() throws SQLException {
        when(clientRepo.findAll()).thenReturn(List.of(client(1)));
        service.searchClientsByLastName("  ");
        verify(clientRepo).findAll();
        verify(clientRepo, never()).findByLastName(any());
    }

    @Test
    void searchClientsByLastName_null_callsFindAll() throws SQLException {
        when(clientRepo.findAll()).thenReturn(List.of(client(1)));
        service.searchClientsByLastName(null);
        verify(clientRepo).findAll();
    }

    @Test
    void searchClientsByLastName_sqlException_rethrows() throws SQLException {
        when(clientRepo.findByLastName(any())).thenThrow(new SQLException());
        assertThrows(SQLException.class, () -> service.searchClientsByLastName("Test"));
    }

    // =========================================================
    // openDeposit
    // =========================================================

    @Test
    void openDeposit_success() throws SQLException {
        Deposit deposit = termDeposit(1); // minAmount=1000
        ClientDeposit expected = activeRecord(10, 1, 5000.0);
        when(depositRepo.findById(1)).thenReturn(Optional.of(deposit));
        when(cdRepo.save(any(ClientDeposit.class))).thenReturn(expected);

        ClientDeposit result = service.openDeposit(1, 1, 5000.0);

        assertSame(expected, result);
        verify(cdRepo).save(any(ClientDeposit.class));
    }

    @Test
    void openDeposit_exactMinimum_succeeds() throws SQLException {
        Deposit deposit = termDeposit(1); // minAmount=1000
        ClientDeposit expected = activeRecord(10, 1, 1000.0);
        when(depositRepo.findById(1)).thenReturn(Optional.of(deposit));
        when(cdRepo.save(any())).thenReturn(expected);

        assertNotNull(service.openDeposit(1, 1, 1000.0));
    }

    @Test
    void openDeposit_depositNotFound_throwsIAE() throws SQLException {
        when(depositRepo.findById(99)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.openDeposit(1, 99, 5000.0));
        verifyNoInteractions(cdRepo);
    }

    @Test
    void openDeposit_amountBelowMinimum_throwsIAE() throws SQLException {
        when(depositRepo.findById(1)).thenReturn(Optional.of(termDeposit(1)));
        assertThrows(IllegalArgumentException.class, () -> service.openDeposit(1, 1, 500.0));
        verifyNoInteractions(cdRepo);
    }

    @Test
    void openDeposit_sqlException_rethrows() throws SQLException {
        when(depositRepo.findById(1)).thenThrow(new SQLException());
        assertThrows(SQLException.class, () -> service.openDeposit(1, 1, 5000.0));
    }

    // =========================================================
    // closeDeposit
    // =========================================================

    @Test
    void closeDeposit_success() throws SQLException {
        ClientDeposit record = activeRecord(1, 1, 5000.0);
        when(cdRepo.findById(1)).thenReturn(Optional.of(record));

        service.closeDeposit(1);

        assertEquals("CLOSED", record.getStatus());
        verify(cdRepo).save(record);
    }

    @Test
    void closeDeposit_notFound_throwsIAE() throws SQLException {
        when(cdRepo.findById(99)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.closeDeposit(99));
    }

    @Test
    void closeDeposit_sqlException_rethrows() throws SQLException {
        when(cdRepo.findById(1)).thenThrow(new SQLException());
        assertThrows(SQLException.class, () -> service.closeDeposit(1));
    }

    // =========================================================
    // calculateExpectedReturn
    // =========================================================

    @Test
    void calculateExpectedReturn_termDeposit_fullTerm() throws SQLException {
        ClientDeposit record = activeRecord(1, 1, 10000.0);
        TermDeposit deposit = termDeposit(1); // rate=12%, term=12m
        when(cdRepo.findById(1)).thenReturn(Optional.of(record));
        when(depositRepo.findById(1)).thenReturn(Optional.of(deposit));

        // 10000 * 12% * 12/12 = 1200
        double income = service.calculateExpectedReturn(1, 12);

        assertEquals(1200.0, income, 0.001);
    }

    @Test
    void calculateExpectedReturn_termDeposit_earlyWithdrawal() throws SQLException {
        ClientDeposit record = activeRecord(1, 1, 10000.0);
        TermDeposit deposit = termDeposit(1); // rate=12%, term=12m, penalty=2%
        when(cdRepo.findById(1)).thenReturn(Optional.of(record));
        when(depositRepo.findById(1)).thenReturn(Optional.of(deposit));

        // earned = 10000 * 12% * 6/12 = 600; penalty = 10000 * 2% = 200; income = 400
        double income = service.calculateExpectedReturn(1, 6);

        assertEquals(400.0, income, 0.001);
    }

    @Test
    void calculateExpectedReturn_zeroMonths_throwsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> service.calculateExpectedReturn(1, 0));
        verifyNoInteractions(cdRepo, depositRepo);
    }

    @Test
    void calculateExpectedReturn_negativeMonths_throwsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> service.calculateExpectedReturn(1, -5));
    }

    @Test
    void calculateExpectedReturn_clientDepositNotFound_throwsIAE() throws SQLException {
        when(cdRepo.findById(99)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.calculateExpectedReturn(99, 12));
    }

    @Test
    void calculateExpectedReturn_depositNotFound_throwsIAE() throws SQLException {
        ClientDeposit record = activeRecord(1, 99, 5000.0);
        when(cdRepo.findById(1)).thenReturn(Optional.of(record));
        when(depositRepo.findById(99)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.calculateExpectedReturn(1, 12));
    }

    @Test
    void calculateExpectedReturn_sqlException_rethrows() throws SQLException {
        when(cdRepo.findById(1)).thenThrow(new SQLException());
        assertThrows(SQLException.class, () -> service.calculateExpectedReturn(1, 12));
    }

    // =========================================================
    // getClientDeposits
    // =========================================================

    @Test
    void getClientDeposits_returnsAll() throws SQLException {
        when(cdRepo.findByClientId(1)).thenReturn(
                List.of(activeRecord(1, 1, 5000.0), activeRecord(2, 2, 3000.0)));
        assertEquals(2, service.getClientDeposits(1).size());
    }

    @Test
    void getClientDeposits_empty() throws SQLException {
        when(cdRepo.findByClientId(1)).thenReturn(List.of());
        assertTrue(service.getClientDeposits(1).isEmpty());
    }

    @Test
    void getClientDeposits_sqlException_rethrows() throws SQLException {
        when(cdRepo.findByClientId(1)).thenThrow(new SQLException());
        assertThrows(SQLException.class, () -> service.getClientDeposits(1));
    }

    // =========================================================
    // getActiveClientDeposits
    // =========================================================

    @Test
    void getActiveClientDeposits_filtersOutClosed() throws SQLException {
        ClientDeposit active = activeRecord(1, 1, 5000.0);
        ClientDeposit closed = new ClientDeposit(2, 1, 2, 3000.0, "2024-01-01", "CLOSED");
        when(cdRepo.findByClientId(1)).thenReturn(List.of(active, closed));

        List<ClientDeposit> result = service.getActiveClientDeposits(1);

        assertEquals(1, result.size());
        assertEquals("ACTIVE", result.get(0).getStatus());
    }

    @Test
    void getActiveClientDeposits_allActive_returnsAll() throws SQLException {
        when(cdRepo.findByClientId(1)).thenReturn(
                List.of(activeRecord(1, 1, 5000.0), activeRecord(2, 2, 3000.0)));
        assertEquals(2, service.getActiveClientDeposits(1).size());
    }

    @Test
    void getActiveClientDeposits_allClosed_returnsEmpty() throws SQLException {
        ClientDeposit c1 = new ClientDeposit(1, 1, 1, 5000.0, "2024-01-01", "CLOSED");
        ClientDeposit c2 = new ClientDeposit(2, 1, 2, 3000.0, "2024-01-01", "CLOSED");
        when(cdRepo.findByClientId(1)).thenReturn(List.of(c1, c2));
        assertTrue(service.getActiveClientDeposits(1).isEmpty());
    }

    @Test
    void getActiveClientDeposits_sqlException_rethrows() throws SQLException {
        when(cdRepo.findByClientId(1)).thenThrow(new SQLException());
        assertThrows(SQLException.class, () -> service.getActiveClientDeposits(1));
    }
}
