CREATE TABLE transaction_service.transactions
(
    id                UUID PRIMARY KEY     DEFAULT uuid_generate_v4(),
    created           TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated           TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    user_id           UUID        NOT NULL,
    wallet_id         UUID        NOT NULL REFERENCES transaction_service.wallets (id),
    amount            DECIMAL     NOT NULL DEFAULT 0.0,
    type              VARCHAR(32) NOT NULL,
    status            VARCHAR(32) NOT NULL,
    comment           VARCHAR(256),
    fee               DECIMAL,
    target_wallet_id  UUID,   -- для transfer
    payment_method_id BIGINT, -- для deposit/withdrawal
    failure_reason    VARCHAR(256)
);