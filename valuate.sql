DROP TABLE IF EXISTS feedback;
DROP TABLE IF EXISTS feedback_field;
DROP TABLE IF EXISTS web_resource;
DROP TABLE IF EXISTS smiley_user;

DROP TYPE IF EXISTS FIELD_TYPE;
CREATE TYPE FIELD_TYPE AS ENUM ('integer', 'text');


CREATE TABLE smiley_user (
    user_id SERIAL PRIMARY KEY,
    email VARCHAR(70) UNIQUE NOT NULL,
    first_name VARCHAR(20) NOT NULL,
    last_name VARCHAR(20) NOT NULL,
    password_hash VARCHAR(128),
    password_salt VARCHAR(64),
    registered TIMESTAMP NOT NULL DEFAULT current_timestamp,
    last_login TIMESTAMP DEFAULT NULL,
    email_confirmation_token,
    enabled BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE web_resource (
    resource_id BIGSERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES smile_user(user_id) ON DELETE CASCADE,
    url_prefix TEXT NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT current_timestamp,
    modified TIMESTAMP NOT NULL DEFAULT current_timestamp   -- mutable, for now
);

CREATE TABLE feedback_field (
    feedback_field_id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL REFERENCES web_resource(resource_id),
    type FIELD_TYPE NOT NULL,
    mandatory BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE feedback (
    feedback_id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL REFERENCES web_resource(resource_id),
    received TIMESTAMP NOT NULL DEFAULT current_timestamp,
    ip_address VARCHAR(39) NOT NULL, -- supports ipV6
    page_url TEXT NOT NULL
);

CREATE TABLE feedback_field_value (
    feedback_id BIGINT NOT NULL REFERENCES feedback(feedback_id) ON DELETE CASCADE,
    feedback_field_id BIGINT NOT NULL REFERENCES feedback_field(feedback_field_id) ON DELETE CASCADE,
    value TEXT NOT NULL
);
