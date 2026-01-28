CREATE TABLE currency_rate.rate_providers
(
    provider_code varchar(3) primary key,
    created       timestamp default now() not null,
    updated       timestamp,
    provider_name varchar(28)             not null UNIQUE,
    description   varchar(255),
    priority      integer                 not null,
    active        boolean   default true
);