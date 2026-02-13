CREATE TABLE payment_service.payment_methods
(
    id                   UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    provider_id          UUID REFERENCES payment_service.payment_providers (id),
    type                 VARCHAR(32) NOT NULL,
    name                 VARCHAR(64) NOT NULL,
    is_active            BOOLEAN          DEFAULT TRUE,
    provider_unique_id   VARCHAR(128),
    provider_method_type VARCHAR(32),
    logo                 TEXT,
    profile_type         VARCHAR(24)      DEFAULT 'INDIVIDUAL',
    created_at           TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP
);