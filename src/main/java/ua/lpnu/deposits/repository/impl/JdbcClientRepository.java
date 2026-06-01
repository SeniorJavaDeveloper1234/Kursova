package ua.lpnu.deposits.repository.impl;

import ua.lpnu.deposits.model.Client;
import ua.lpnu.deposits.repository.ClientRepository;
import ua.lpnu.deposits.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link ClientRepository}.
 * All SQL is confined to this class; no SQL appears in the service layer.
 */
public class JdbcClientRepository implements ClientRepository {

    /** Creates a new {@code JdbcClientRepository} using the shared {@link ua.lpnu.deposits.util.DatabaseConnection}. */
    public JdbcClientRepository() {}

    private Connection connection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    /** {@inheritDoc} */
    @Override
    public Client save(Client client) throws SQLException {
        if (client.getId() == 0) {
            return insert(client);
        }
        return update(client);
    }

    private Client insert(Client client) throws SQLException {
        String sql = "INSERT INTO clients (first_name, last_name, email) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, client.getFirstName());
            ps.setString(2, client.getLastName());
            ps.setString(3, client.getEmail());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    client.setId(keys.getInt(1));
                }
            }
        }
        return client;
    }

    private Client update(Client client) throws SQLException {
        String sql = "UPDATE clients SET first_name = ?, last_name = ?, email = ? WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, client.getFirstName());
            ps.setString(2, client.getLastName());
            ps.setString(3, client.getEmail());
            ps.setInt(4, client.getId());
            ps.executeUpdate();
        }
        return client;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Client> findById(int id) throws SQLException {
        String sql = "SELECT id, first_name, last_name, email FROM clients WHERE id = ?";
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
    public List<Client> findAll() throws SQLException {
        String sql = "SELECT id, first_name, last_name, email FROM clients ORDER BY last_name, first_name";
        try (PreparedStatement ps = connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Client> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Client> findByLastName(String lastName) throws SQLException {
        String sql = "SELECT id, first_name, last_name, email FROM clients"
                + " WHERE last_name LIKE ? ORDER BY last_name, first_name";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, "%" + lastName + "%");
            try (ResultSet rs = ps.executeQuery()) {
                List<Client> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
                return result;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Client> findByEmail(String email) throws SQLException {
        String sql = "SELECT id, first_name, last_name, email FROM clients WHERE email = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, email);
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
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM clients WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Client mapRow(ResultSet rs) throws SQLException {
        return new Client(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email")
        );
    }
}
