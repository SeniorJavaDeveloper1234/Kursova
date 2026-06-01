package ua.lpnu.deposits.repository.impl;

import ua.lpnu.deposits.model.*;
import ua.lpnu.deposits.repository.DepositRepository;
import ua.lpnu.deposits.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link DepositRepository}.
 * The {@link #mapRow} method dispatches on the {@code type} column to
 * instantiate the correct {@link Deposit} subclass.
 * All SQL is confined to this class; no SQL appears in the service layer.
 */
public class JdbcDepositRepository implements DepositRepository {

    /** Creates a new {@code JdbcDepositRepository} using the shared {@link ua.lpnu.deposits.util.DatabaseConnection}. */
    public JdbcDepositRepository() {}

    private static final String SELECT_COLUMNS =
            "SELECT id, bank_id, name, type, currency, min_amount, interest_rate,"
            + " term_months, can_withdraw_early, can_replenish, penalty_rate FROM deposits";

    private Connection connection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    /** {@inheritDoc} */
    @Override
    public Deposit save(Deposit deposit) throws SQLException {
        if (deposit.getId() == 0) {
            return insert(deposit);
        }
        return update(deposit);
    }

    private Deposit insert(Deposit deposit) throws SQLException {
        String sql = "INSERT INTO deposits"
                + " (bank_id, name, type, currency, min_amount, interest_rate,"
                + "  term_months, can_withdraw_early, can_replenish, penalty_rate)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindDepositFields(ps, deposit);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    deposit.setId(keys.getInt(1));
                }
            }
        }
        return deposit;
    }

    private Deposit update(Deposit deposit) throws SQLException {
        String sql = "UPDATE deposits SET"
                + " bank_id = ?, name = ?, type = ?, currency = ?, min_amount = ?,"
                + " interest_rate = ?, term_months = ?, can_withdraw_early = ?,"
                + " can_replenish = ?, penalty_rate = ?"
                + " WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            bindDepositFields(ps, deposit);
            ps.setInt(11, deposit.getId());
            ps.executeUpdate();
        }
        return deposit;
    }

    /**
     * Binds the common deposit fields to parameters 1-10 of a prepared statement.
     * Used by both insert and update to avoid duplication.
     */
    private void bindDepositFields(PreparedStatement ps, Deposit d) throws SQLException {
        ps.setInt(1, d.getBankId());
        ps.setString(2, d.getName());
        ps.setString(3, d.getType().name());
        ps.setString(4, d.getCurrency());
        ps.setDouble(5, d.getMinAmount());
        ps.setDouble(6, d.getInterestRate());
        ps.setInt(7, d.getTermMonths());
        ps.setInt(8, d.isCanWithdrawEarly() ? 1 : 0);
        ps.setInt(9, d.isCanReplenish() ? 1 : 0);
        ps.setDouble(10, d.getPenaltyRate());
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Deposit> findById(int id) throws SQLException {
        String sql = SELECT_COLUMNS + " WHERE id = ?";
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
    public List<Deposit> findAll() throws SQLException {
        String sql = SELECT_COLUMNS + " ORDER BY name";
        try (PreparedStatement ps = connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return collectRows(rs);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Deposit> findByBankId(int bankId) throws SQLException {
        String sql = SELECT_COLUMNS + " WHERE bank_id = ? ORDER BY name";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, bankId);
            try (ResultSet rs = ps.executeQuery()) {
                return collectRows(rs);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Deposit> findByType(DepositType type) throws SQLException {
        String sql = SELECT_COLUMNS + " WHERE type = ? ORDER BY name";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                return collectRows(rs);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Deposit> findByCurrency(String currency) throws SQLException {
        String sql = SELECT_COLUMNS + " WHERE currency = ? ORDER BY name";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, currency);
            try (ResultSet rs = ps.executeQuery()) {
                return collectRows(rs);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Deposit> findByInterestRateBetween(double minRate, double maxRate) throws SQLException {
        String sql = SELECT_COLUMNS + " WHERE interest_rate BETWEEN ? AND ? ORDER BY interest_rate DESC";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setDouble(1, minRate);
            ps.setDouble(2, maxRate);
            try (ResultSet rs = ps.executeQuery()) {
                return collectRows(rs);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM deposits WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Maps the current row of {@code rs} to the correct {@link Deposit} subclass
     * based on the {@code type} column value.
     *
     * @param rs a ResultSet positioned on a valid row
     * @return a concrete {@link Deposit} instance
     * @throws SQLException              on column access failure
     * @throws IllegalArgumentException  if the type value is unrecognised
     */
    private Deposit mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int bankId = rs.getInt("bank_id");
        String name = rs.getString("name");
        String type = rs.getString("type");
        String currency = rs.getString("currency");
        double minAmount = rs.getDouble("min_amount");
        double interestRate = rs.getDouble("interest_rate");
        int termMonths = rs.getInt("term_months");
        double penaltyRate = rs.getDouble("penalty_rate");

        return switch (type) {
            case "TERM" -> new TermDeposit(id, bankId, name, currency,
                    minAmount, interestRate, termMonths, penaltyRate);
            case "SAVINGS" -> new SavingsDeposit(id, bankId, name, currency,
                    minAmount, interestRate, termMonths);
            case "DEMAND" -> new DemandDeposit(id, bankId, name, currency,
                    minAmount, interestRate);
            default -> throw new IllegalArgumentException("Unknown deposit type in DB: " + type);
        };
    }

    private List<Deposit> collectRows(ResultSet rs) throws SQLException {
        List<Deposit> result = new ArrayList<>();
        while (rs.next()) {
            result.add(mapRow(rs));
        }
        return result;
    }
}
