CREATE TABLE payment_service.payment_method_definitions
(
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_method_id   UUID REFERENCES payment_service.payment_methods (id),
    currency_code       VARCHAR(3),
    country_alpha3_code VARCHAR(3),
    is_all_currencies   BOOLEAN          DEFAULT FALSE,
    is_all_countries    BOOLEAN          DEFAULT FALSE,
    is_priority         BOOLEAN          DEFAULT FALSE,
    is_active           BOOLEAN          DEFAULT TRUE,
    created_at          TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP
);