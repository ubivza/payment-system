CREATE TABLE currency_rate.currencies
(
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    created     timestamp        default now() not null,
    updated     timestamp,
    code        varchar(3)                     not null UNIQUE,
    iso_code    integer                        not null UNIQUE,
    description varchar(64)                    not null,
    active      boolean          default true,
    symbol      varchar(2)
);