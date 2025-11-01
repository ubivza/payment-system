CREATE TABLE person.users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    secret_key VARCHAR(32),
    email VARCHAR(1024),
    created TIMESTAMP NOT NULL,
    updated TIMESTAMP NOT NULL,
    first_name VARCHAR(32),
    last_name VARCHAR(32),
    filled BOOLEAN,
    address_id UUID REFERENCES person.addresses(id)
);