CREATE TABLE person.addresses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    created TIMESTAMP NOT NULL,
    updated TIMESTAMP NOT NULL,
    country_id INTEGER REFERENCES person.countries (id),
    address VARCHAR(128),
    zip_code VARCHAR(32),
    archived TIMESTAMP,
    city VARCHAR(32),
    state VARCHAR(32)
);