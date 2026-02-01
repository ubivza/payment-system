CREATE TABLE payment_provider.merchants
(
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
        merchant_id VARCHAR(50) NOT NULL UNIQUE,
        secret_key  VARCHAR(255) NOT NULL,
        name        VARCHAR(100),
        created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);