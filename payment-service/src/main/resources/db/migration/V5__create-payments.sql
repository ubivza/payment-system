CREATE TABLE payment_service.payments
(
    id                      UUID PRIMARY KEY        DEFAULT uuid_generate_v4(),
    payment_method_id       UUID REFERENCES payment_service.payment_methods (id),
    external_transaction_id VARCHAR(128),
    internal_transaction_id VARCHAR(128),
    amount                  NUMERIC(18, 2) NOT NULL,
    currency                VARCHAR(3)     NOT NULL,
    status                  VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    created_at              TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP
);