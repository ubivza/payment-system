CREATE TABLE payment_service.payment_method_required_fields
(
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    created_at          TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP,
    payment_method_id   UUID REFERENCES payment_service.payment_methods (id),
    payment_type        VARCHAR(64)  NOT NULL,
    country_alpha3_code VARCHAR(3),
    name                VARCHAR(128) NOT NULL,
    data_type           VARCHAR(128) NOT NULL,
    validation_type     VARCHAR(128),
    validation_rule     VARCHAR(256),
    default_value       VARCHAR(128),
    values_options      TEXT,
    description         VARCHAR(255),
    placeholder         VARCHAR(255),
    representation_name VARCHAR(255),
    language            VARCHAR(2),
    is_active           BOOLEAN          DEFAULT TRUE,
    UNIQUE (language, name, payment_method_id, payment_type, country_alpha3_code)
);