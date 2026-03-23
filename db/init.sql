CREATE TABLE accounts (
    account_id        UUID PRIMARY KEY,
    owner_id          UUID NOT NULL,
    status            VARCHAR(20) NOT NULL,
    balance_amount    NUMERIC(19,2) NOT NULL,
    balance_currency  VARCHAR(3) NOT NULL,
    updated_at        BIGINT NOT NULL,
    created_at        TIMESTAMP NOT NULL
);

CREATE TABLE transactions (
    transaction_id     UUID PRIMARY KEY,
    account_id         UUID NOT NULL,
    type               VARCHAR(10) NOT NULL,
    amount             NUMERIC(19,2) NOT NULL,
    currency           VARCHAR(3) NOT NULL,
    status             VARCHAR(15) NOT NULL,
    event_timestamp    BIGINT NOT NULL,
    created_at         TIMESTAMP DEFAULT NOW(),

    CONSTRAINT fk_transactions_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(account_id)
);

CREATE INDEX idx_transactions_account_id 
ON transactions(account_id);

CREATE INDEX idx_transactions_timestamp 
ON transactions(event_timestamp);

CREATE INDEX idx_accounts_updated_at 
ON accounts(updated_at);