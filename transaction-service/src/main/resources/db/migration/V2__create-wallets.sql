CREATE TABLE transaction_service.wallets
(
    id             UUID PRIMARY KEY     DEFAULT uuid_generate_v4(),
    created        TIMESTAMP   NOT NULL DEFAULT now(),
    updated        TIMESTAMP,
    name           VARCHAR(32) NOT NULL,
    wallet_type_id UUID        NOT NULL REFERENCES transaction_service.wallet_types (id),
    user_id        UUID        NOT NULL,
    status         VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    balance        DECIMAL     NOT NULL DEFAULT 0.0,
    archived_at    TIMESTAMP
);