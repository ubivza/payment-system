CREATE TABLE shedlock
(
    name       varchar(64)  not null primary key,
    lock_until timestamp    not null,
    locked_at  timestamp    not null,
    locked_by  varchar(255) not null
);