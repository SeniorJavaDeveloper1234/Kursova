package ua.lpnu.deposits.repository.impl;

import ua.lpnu.deposits.model.User;
import ua.lpnu.deposits.repository.UserRepository;
import ua.lpnu.deposits.util.DatabaseConnection;
import ua.lpnu.deposits.util.PasswordUtil;

import java.sql.*;
import java.util.Optional;

/**
 * JDBC implementation of {@link UserRepository}.
 * All SQL lives here; no SQL is allowed in services or controllers.
 */
public class JdbcUserRepository implements UserRepository {

    /** Creates a new {@code JdbcUserRepository} using the shared {@link ua.lpnu.deposits.util.DatabaseConnection}. */
    public JdbcUserRepository() {}

    private Connection conn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, role FROM users WHERE username = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean authenticate(String username, String plainText) throws SQLException {
        Optional<User> userOpt = findByUsername(username);
        if (userOpt.isEmpty()) return false;
        return PasswordUtil.verify(plainText, userOpt.get().getPasswordHash());
    }

    @Override
    public void save(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole());
            ps.executeUpdate();
        }
    }

    @Override
    public void ensureDefaultAdmin() throws SQLException {
        String countSql = "SELECT COUNT(*) FROM users";
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                save(new User(0, "admin", PasswordUtil.hash("admin"), "ADMIN"));
                save(new User(0, "user",  PasswordUtil.hash("user"),  "USER"));
            }
        }
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("role")
        );
    }
}
