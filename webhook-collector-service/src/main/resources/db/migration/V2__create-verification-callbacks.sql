CREATE TABLE webhook_collector_service.verification_callbacks
(
    id              uuid      DEFAULT uuid_generate_v4() PRIMARY KEY,
    created_at      TIMESTAMP DEFAULT now() NOT NULL,
    updated_at      TIMESTAMP DEFAULT now() NOT NULL,
    body            jsonb                   NOT NULL,
    transaction_uid uuid,
    profile_uid     uuid                    NOT NULL,
    status          VARCHAR(25),
    type            VARCHAR(255)
);