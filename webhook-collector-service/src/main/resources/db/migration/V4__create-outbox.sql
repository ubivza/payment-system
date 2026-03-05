CREATE TABLE webhook_collector_service.outbox
(
    id             bigserial PRIMARY KEY,
    event_type     TEXT                    NOT NULL,
    transaction_id uuid                    not null,
    payload        jsonb                   NOT NULL,
    created_at     TIMESTAMP DEFAULT now() NOT NULL,
    constraint outbox_unique unique (event_type, transaction_id)
);