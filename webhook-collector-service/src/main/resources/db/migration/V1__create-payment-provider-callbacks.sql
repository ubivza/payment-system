CREATE TABLE webhook_collector_service.payment_provider_callbacks
(
    id                       uuid      DEFAULT uuid_generate_v4() PRIMARY KEY,
    created_at               TIMESTAMP DEFAULT now() NOT NULL,
    updated_at               TIMESTAMP DEFAULT now() NOT NULL,
    body jsonb                    NOT NULL,
    provider_transaction_uid uuid,
    type                     VARCHAR(255),
    provider                 VARCHAR(255)
);