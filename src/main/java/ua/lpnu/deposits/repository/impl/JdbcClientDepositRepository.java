package ua.lpnu.deposits.repository.impl;

import ua.lpnu.deposits.model.*;
import ua.lpnu.deposits.repository.ClientDepositRepository;
import ua.lpnu.deposits.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link ClientDepositRepository}.
 * All SQL is confined to this class; no SQL appears in the service layer.
 */
public class JdbcClientDepositRepository implements ClientDepositRepository {

    /** Creates a new {@code JdbcClientDepositRepository} using the shared {@link ua.lpnu.deposits.util.DatabaseConnection}. */
    public JdbcClientDepositRepository() {}

    private Connection connection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    /** {@inheritDoc} */
    @Override
    public ClientDeposit save(ClientDeposit clientDeposit) throws SQLException {
        if (clientDeposit.getId() == 0) {
            return insert(clientDeposit);
        }
        return update(clientDeposit);
    }

    private ClientDeposit insert(ClientDeposit cd) throws SQLException {
        String sql = "INSERT INTO client_deposits (client_id, deposit_id, amount, status)"
                + " VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, cd.getClientId());
            ps.setInt(2, cd.getDepositId());
            ps.setDouble(3, cd.getAmount());
            ps.setString(4, cd.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    cd.setId(keys.getInt(1));
                }
            }
        }
        return findById(cd.getId()).orElse(cd);
    }

    private ClientDeposit update(ClientDeposit cd) throws SQLException {
        String sql = "UPDATE client_deposits SET client_id = ?, deposit_id = ?, amount = ?, status = ?"
                + " WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, cd.getClientId());
            ps.setInt(2, cd.getDepositId());
            ps.setDouble(3, cd.getAmount());
            ps.setString(4, cd.getStatus());
            ps.setInt(5, cd.getId());
            ps.executeUpdate();
        }
        return cd;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<ClientDeposit> findById(int id) throws SQLException {
        String sql = "SELECT id, client_id, deposit_id, amount, opened_at, status"
                + " FROM client_deposits WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public List<ClientDeposit> findAll() throws SQLException {
        String sql = "SELECT id, client_id, deposit_id, amount, opened_at, status"
                + " FROM client_deposits ORDER BY opened_at DESC";
        try (PreparedStatement ps = connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<ClientDeposit> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<ClientDeposit> findByClientId(int clientId) throws SQLException {
        String sql = "SELECT id, client_id, deposit_id, amount, opened_at, status"
                + " FROM client_deposits WHERE client_id = ? ORDER BY opened_at DESC";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                List<ClientDeposit> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
                return result;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<ClientDepositDetail> findDetailedByClientId(int clientId) throws SQLException {
        String sql = "SELECT cd.id, cd.amount, cd.opened_at, cd.status,"
                + " d.id AS deposit_id, d.name AS deposit_name, d.type,"
                + " d.interest_rate, d.term_months, d.can_withdraw_early,"
                + " d.penalty_rate, d.min_amount, d.bank_id, d.can_replenish, d.currency,"
                + " b.name AS bank_name"
                + " FROM client_deposits cd"
                + " JOIN deposits d ON cd.deposit_id = d.id"
                + " JOIN banks b ON d.bank_id = b.id"
                + " WHERE cd.client_id = ?"
                + " ORDER BY cd.opened_at DESC";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                List<ClientDepositDetail> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapDetailRow(rs));
                }
                return result;
            }
        }
    }

    private ClientDepositDetail mapDetailRow(ResultSet rs) throws SQLException {
        int id            = rs.getInt("id");
        double amount     = rs.getDouble("amount");
        String openedAt   = rs.getString("opened_at");
        String status     = rs.getString("status");
        String depositName = rs.getString("deposit_name");
        String bankName   = rs.getString("bank_name");
        String type       = rs.getString("type");
        DepositType depositType = DepositType.valueOf(type);

        int    depositId    = rs.getInt("deposit_id");
        int    bankId       = rs.getInt("bank_id");
        String currency     = rs.getString("currency");
        double minAmount    = rs.getDouble("min_amount");
        double interestRate = rs.getDouble("interest_rate");
        int    termMonths   = rs.getInt("term_months");
        double penaltyRate  = rs.getDouble("penalty_rate");

        Deposit deposit = switch (type) {
            case "TERM"    -> new TermDeposit(depositId, bankId, depositName, currency,
                                    minAmount, interestRate, termMonths, penaltyRate);
            case "SAVINGS" -> new SavingsDeposit(depositId, bankId, depositName, currency,
                                    minAmount, interestRate, termMonths);
            case "DEMAND"  -> new DemandDeposit(depositId, bankId, depositName, currency,
                                    minAmount, interestRate);
            default -> throw new IllegalArgumentException("Unknown deposit type in DB: " + type);
        };

        return new ClientDepositDetail(id, depositName, bankName, depositType,
                amount, openedAt, status, deposit);
    }

    /** {@inheritDoc} */
    @Override
    public List<ClientDeposit> findByDepositId(int depositId) throws SQLException {
        String sql = "SELECT id, client_id, deposit_id, amount, opened_at, status"
                + " FROM client_deposits WHERE deposit_id = ? ORDER BY opened_at DESC";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, depositId);
            try (ResultSet rs = ps.executeQuery()) {
                List<ClientDeposit> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
                return result;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<ClientDeposit> findByStatus(String status) throws SQLException {
        String sql = "SELECT id, client_id, deposit_id, amount, opened_at, status"
                + " FROM client_deposits WHERE status = ? ORDER BY opened_at DESC";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                List<ClientDeposit> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
                return result;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM client_deposits WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private ClientDeposit mapRow(ResultSet rs) throws SQLException {
        return new ClientDeposit(
                rs.getInt("id"),
                rs.getInt("client_id"),
                rs.getInt("deposit_id"),
                rs.getDouble("amount"),
                rs.getString("opened_at"),
                rs.getString("status")
        );
    }
}
