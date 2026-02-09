CREATE TABLE payment_provider.transactions
(
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    merchant_id      UUID           NOT NULL REFERENCES payment_provider.merchants (id),
    amount           NUMERIC(18, 2) NOT NULL,
    currency         VARCHAR(3)     NOT NULL,
    method           VARCHAR(50)    NOT NULL,
    status           VARCHAR(20)    NOT NULL,
    created_at       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP,
    description      VARCHAR(255),
    external_id      VARCHAR(100),
    notification_url VARCHAR(2048)
);