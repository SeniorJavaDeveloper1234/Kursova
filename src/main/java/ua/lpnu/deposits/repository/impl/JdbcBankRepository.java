package ua.lpnu.deposits.repository.impl;

import ua.lpnu.deposits.model.Bank;
import ua.lpnu.deposits.repository.BankRepository;
import ua.lpnu.deposits.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link BankRepository}.
 * All SQL is confined to this class; no SQL appears in the service layer.
 */
public class JdbcBankRepository implements BankRepository {

    private Connection connection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    /** {@inheritDoc} */
    @Override
    public Bank save(Bank bank) throws SQLException {
        if (bank.getId() == 0) {
            return insert(bank);
        }
        return update(bank);
    }

    private Bank insert(Bank bank) throws SQLException {
        String sql = "INSERT INTO banks (name, rating) VALUES (?, ?)";
        try (PreparedStatement ps = connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, bank.getName());
            ps.setDouble(2, bank.getRating());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    bank.setId(keys.getInt(1));
                }
            }
        }
        return bank;
    }

    private Bank update(Bank bank) throws SQLException {
        String sql = "UPDATE banks SET name = ?, rating = ? WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, bank.getName());
            ps.setDouble(2, bank.getRating());
            ps.setInt(3, bank.getId());
            ps.executeUpdate();
        }
        return bank;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Bank> findById(int id) throws SQLException {
        String sql = "SELECT id, name, rating FROM banks WHERE id = ?";
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
    public List<Bank> findAll() throws SQLException {
        String sql = "SELECT id, name, rating FROM banks ORDER BY name";
        try (PreparedStatement ps = connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Bank> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Bank> findByName(String name) throws SQLException {
        String sql = "SELECT id, name, rating FROM banks WHERE name LIKE ? ORDER BY name";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                List<Bank> result = new ArrayList<>();
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
        String sql = "DELETE FROM banks WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Bank mapRow(ResultSet rs) throws SQLException {
        return new Bank(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("rating")
        );
    }
}
