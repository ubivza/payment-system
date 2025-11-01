CREATE TABLE person_history.revinfo
(
    rev      BIGSERIAL PRIMARY KEY,
    revtstmp BIGINT
);

CREATE TABLE person_history.countries_history
(
    id            INTEGER                     NOT NULL,
    revision      BIGINT                      NOT NULL,
    revision_type SMALLINT                    NOT NULL,
    created       TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated       TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    name          VARCHAR(32),
    alpha2        VARCHAR(2),
    alpha3        VARCHAR(3),
    status        VARCHAR(32),

    CONSTRAINT pk_counties_history PRIMARY KEY (id, revision),
    CONSTRAINT fk_countries_history_rev FOREIGN KEY (revision) REFERENCES person_history.revinfo (rev)
);

CREATE INDEX IF NOT EXISTS idx_countries_history_revision ON person_history.countries_history (revision);

CREATE TABLE person_history.addresses_history
(
    id            UUID                        NOT NULL,
    revision      BIGINT                      NOT NULL,
    revision_type SMALLINT                    NOT NULL,
    created       TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated       TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    country_id    INTEGER                     NOT NULL,
    address       VARCHAR(128)                NOT NULL,
    zip_code      VARCHAR(32)                 NOT NULL,
    city          VARCHAR(32)                 NOT NULL,
    state         VARCHAR(32),
    archived      TIMESTAMP,

    CONSTRAINT pk_addresses_history PRIMARY KEY (id, revision),
    CONSTRAINT fk_addresses_history_rev FOREIGN KEY (revision) REFERENCES person_history.revinfo (rev)
);

CREATE INDEX IF NOT EXISTS idx_addresses_history_revision ON person_history.addresses_history (revision);


CREATE TABLE person_history.users_history
(
    id            UUID                        NOT NULL,
    revision      BIGINT                      NOT NULL,
    revision_type SMALLINT                    NOT NULL,
    created       TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated       TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    email         VARCHAR(1024)               NOT NULL,
    first_name    VARCHAR(64)                 NOT NULL,
    last_name     VARCHAR(64)                 NOT NULL,
    address_id    UUID                        NOT NULL,
    filled        BOOLEAN,
    secret_key    VARCHAR(32),

    CONSTRAINT pk_users_history PRIMARY KEY (id, revision),
    CONSTRAINT fk_users_history_rev FOREIGN KEY (revision) REFERENCES person_history.revinfo (rev)
);

CREATE INDEX IF NOT EXISTS idx_users_history_revision ON person_history.users_history (revision);

CREATE TABLE person_history.individuals_history
(
    id              UUID                        NOT NULL,
    revision        BIGINT                      NOT NULL,
    revision_type   SMALLINT                    NOT NULL,
    created         TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated         TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    passport_number VARCHAR(64)                 NOT NULL,
    phone_number    VARCHAR(64)                 NOT NULL,
    user_id         UUID                        NOT NULL,
    verified_at     TIMESTAMP                   NOT NULL,
    archived_at     TIMESTAMP                   NOT NULL,
    status          VARCHAR(32),

    CONSTRAINT pk_individuals_history PRIMARY KEY (id, revision),
    CONSTRAINT fk_individuals_history_rev FOREIGN KEY (revision) REFERENCES person_history.revinfo (rev)
);

CREATE INDEX IF NOT EXISTS idx_individuals_history_revision ON person_history.individuals_history (revision);