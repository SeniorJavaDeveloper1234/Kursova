package ua.lpnu.deposits.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Seeds the database with realistic test data if the tables are empty.
 * Safe to call on every startup — all inserts are guarded by a row-count check.
 */
public class DataSeeder {

    private static final AppLogger log = AppLogger.getLogger(DataSeeder.class);

    private DataSeeder() {}

    /**
     * Inserts banks, deposits, and clients if their respective tables are empty.
     *
     * @throws SQLException if any database operation fails
     */
    public static void seedIfEmpty() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (isEmpty(conn, "banks")) {
            seedBanks(conn);
        }
        if (isEmpty(conn, "deposits")) {
            seedDeposits(conn);
        }
        if (isEmpty(conn, "clients")) {
            seedClients(conn);
        }
    }

    private static boolean isEmpty(Connection conn, String table) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    private static void seedBanks(Connection conn) throws SQLException {
        String sql =
            "INSERT INTO banks (name, rating) VALUES " +
            "('ПриватБанк', 4.8)," +
            "('Монобанк', 4.9)," +
            "('ПУМБ', 4.2)," +
            "('Райффайзен Банк', 4.5)," +
            "('ОТП Банк', 4.3)";
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        }
        log.info("DataSeeder: seeded 5 banks");
    }

    private static void seedDeposits(Connection conn) throws SQLException {
        String sql =
            "INSERT INTO deposits " +
            "(bank_id, name, type, currency, min_amount, interest_rate, term_months," +
            " can_withdraw_early, can_replenish, penalty_rate) VALUES " +
            "(1, 'Строковий Плюс',     'TERM',    'UAH', 5000,  16.5, 12, 0, 0, 2.0)," +
            "(1, 'Накопичувальний',     'SAVINGS', 'UAH', 1000,  13.0, 24, 0, 1, 0.0)," +
            "(1, 'До запитання',        'DEMAND',  'UAH', 500,   10.0, NULL, 1, 1, 0.0)," +
            "(2, 'Монодепозит 12M',     'TERM',    'UAH', 3000,  17.0, 12, 0, 0, 3.0)," +
            "(2, 'Моно Збереження',     'SAVINGS', 'UAH', 2000,  14.5, 18, 0, 1, 0.0)," +
            "(2, 'Моно Флекс',          'DEMAND',  'UAH', 1000,  11.0, NULL, 1, 1, 0.0)," +
            "(3, 'ПУМБ Класик',         'TERM',    'UAH', 10000, 15.5, 6,  1, 0, 4.0)," +
            "(3, 'ПУМБ Розумний',       'SAVINGS', 'UAH', 5000,  12.5, 12, 0, 1, 0.0)," +
            "(3, 'ПУМБ Поточний',       'DEMAND',  'UAH', 1000,  10.5, NULL, 1, 1, 0.0)," +
            "(4, 'Райффайзен Преміум',  'TERM',    'UAH', 8000,  16.0, 9,  0, 0, 2.5)," +
            "(4, 'Райффайзен Зростання','SAVINGS', 'UAH', 3000,  13.5, 24, 0, 1, 0.0)," +
            "(4, 'Райффайзен Вільний',  'DEMAND',  'UAH', 2000,  10.0, NULL, 1, 1, 0.0)," +
            "(5, 'ОТП Строковий',       'TERM',    'UAH', 5000,  15.0, 3,  1, 0, 3.5)," +
            "(5, 'ОТП Накопичення',     'SAVINGS', 'UAH', 2000,  12.0, 18, 0, 1, 0.0)," +
            "(5, 'ОТП Гнучкий',         'DEMAND',  'UAH', 1000,  10.0, NULL, 1, 1, 0.0)";

        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        }
        log.info("DataSeeder: seeded 15 deposits");
    }

    private static void seedClients(Connection conn) throws SQLException {
        String sql =
            "INSERT INTO clients (first_name, last_name, email) VALUES " +
            "('Олексій',  'Коваленко',  'o.kovalenko@email.ua')," +
            "('Наталія',  'Шевченко',   'n.shevchenko@email.ua')," +
            "('Дмитро',   'Бондаренко', 'd.bondarenko@email.ua')," +
            "('Ірина',    'Мельник',    'i.melnyk@email.ua')," +
            "('Василь',   'Ткаченко',   'v.tkachenko@email.ua')";
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        }
        log.info("DataSeeder: seeded 5 clients");
    }
}
