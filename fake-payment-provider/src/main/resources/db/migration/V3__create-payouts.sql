CREATE TABLE payment_provider.payouts
(
        id           BIGSERIAL PRIMARY KEY,
        merchant_id  UUID NOT NULL REFERENCES merchants(id),
        amount       NUMERIC(18,2) NOT NULL,
        currency     VARCHAR(3) NOT NULL,
        status       VARCHAR(20) NOT NULL,
        created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at   TIMESTAMP,
        external_id  VARCHAR(100),
        notification_url  VARCHAR(2048)
)
