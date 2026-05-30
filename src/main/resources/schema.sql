CREATE TABLE IF NOT EXISTS banks (
    id     INTEGER PRIMARY KEY AUTOINCREMENT,
    name   TEXT    NOT NULL,
    rating REAL
);

CREATE TABLE IF NOT EXISTS deposits (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    bank_id            INTEGER REFERENCES banks(id),
    name               TEXT    NOT NULL,
    type               TEXT    CHECK(type IN ('TERM', 'SAVINGS', 'DEMAND')),
    currency           TEXT    DEFAULT 'UAH',
    min_amount         REAL    NOT NULL,
    interest_rate      REAL    NOT NULL,
    term_months        INTEGER,
    can_withdraw_early INTEGER DEFAULT 0,
    can_replenish      INTEGER DEFAULT 0,
    penalty_rate       REAL    DEFAULT 0
);

CREATE TABLE IF NOT EXISTS clients (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name TEXT    NOT NULL,
    last_name  TEXT    NOT NULL,
    email      TEXT
);

CREATE TABLE IF NOT EXISTS client_deposits (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    client_id  INTEGER REFERENCES clients(id),
    deposit_id INTEGER REFERENCES deposits(id),
    amount     REAL    NOT NULL,
    opened_at  TEXT    DEFAULT (datetime('now')),
    status     TEXT    DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS users (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    username      TEXT    NOT NULL UNIQUE,
    password_hash TEXT    NOT NULL,
    role          TEXT    DEFAULT 'USER'
);
