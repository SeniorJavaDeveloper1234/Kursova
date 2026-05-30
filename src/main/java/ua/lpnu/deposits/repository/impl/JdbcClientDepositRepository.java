package ua.lpnu.deposits.repository.impl;

import ua.lpnu.deposits.model.ClientDeposit;
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
        // Reload to get the DB-generated opened_at timestamp
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
