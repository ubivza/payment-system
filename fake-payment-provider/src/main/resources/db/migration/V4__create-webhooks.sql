CREATE TABLE payment_provider.webhooks
(
    id               BIGSERIAL PRIMARY KEY,
    event_type       VARCHAR(50) NOT NULL,
    entity_id        UUID        NOT NULL,
    payload          JSONB,
    received_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notification_url VARCHAR(2048)
);