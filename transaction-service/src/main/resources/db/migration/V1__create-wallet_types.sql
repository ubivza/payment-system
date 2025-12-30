CREATE TABLE transaction_service.wallet_types
(
    id            UUID PRIMARY KEY     DEFAULT uuid_generate_v4(),
    created       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated       TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    name          VARCHAR(32) NOT NULL,
    currency_code VARCHAR(3)  NOT NULL,
    status        VARCHAR(18) NOT NULL,
    archived_at   TIMESTAMP,
    user_type     VARCHAR(15),
    creator       VARCHAR(255),
    modifier      VARCHAR(255)
);