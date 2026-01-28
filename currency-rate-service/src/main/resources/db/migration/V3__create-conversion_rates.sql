CREATE TABLE currency_rate.conversion_rates
(
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    created          timestamp        default now() not null,
    updated          timestamp,
    source_code      varchar(3)                     not null references currency_rate.currencies (code),
    destination_code varchar(3)                     not null references currency_rate.currencies (code),
    rate_begin_time  timestamp        default now() not null,
    rate_end_time    timestamp                      not null,
    rate             numeric                        not null
)
