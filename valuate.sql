BEGIN;

DROP TABLE IF EXISTS valuate_feedback;
DROP TABLE IF EXISTS valuate_question;
DROP TABLE IF EXISTS site_url_prefix;
DROP TABLE IF EXISTS valuate_site;
DROP TABLE IF EXISTS valuate_user;

CREATE TABLE valuate_user (
    user_id SERIAL PRIMARY KEY,
    email VARCHAR(70) UNIQUE NOT NULL,
    name VARCHAR(40) NOT NULL,
    password_hash VARCHAR(128),
    password_salt VARCHAR(64),
    registered TIMESTAMP NOT NULL DEFAULT current_timestamp,
    last_login TIMESTAMP DEFAULT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    enabled BOOLEAN NOT NULL DEFAULT true,
    administrator BOOLEAN NOT NULL default false
);

CREATE TABLE valuate_site (
    site_id BIGSERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES valuate_user(user_id) ON DELETE CASCADE,
    site_name VARCHAR(64),
    created TIMESTAMP NOT NULL DEFAULT current_timestamp,
    modified TIMESTAMP NOT NULL DEFAULT current_timestamp,
    spam_protect BOOLEAN NOT NULL DEFAULT false,
    UNIQUE (user_id, site_name)
);

CREATE TABLE site_url_prefix (
    site_id BIGINT NOT NULL REFERENCES valuate_site(site_id) ON DELETE CASCADE,
    url_prefix TEXT UNIQUE NOT NULL,
    PRIMARY KEY (site_id, url_prefix)
);

CREATE TABLE valuate_question (
    site_id BIGINT NOT NULL REFERENCES valuate_site(site_id) ON DELETE CASCADE,
    question_id VARCHAR(32), -- hex-encoded md5(question_text)
    lock BOOLEAN NOT NULL DEFAULT false,
    question_text TEXT NOT NULL,
    lowest VARCHAR(40),
    highest VARCHAR(40),
    emoji_set_id VARCHAR(20) NOT NULL DEFAULT 'bw',
    user_logo_url TEXT,
    user_url TEXT,
    user_note TEXT,
    PRIMARY KEY (site_id, question_id)
);

CREATE TABLE valuate_feedback (
    feedback_id BIGSERIAL PRIMARY KEY,
    site_id BIGINT NOT NULL,
    question_id VARCHAR(32) NOT NULL,
    received TIMESTAMP NOT NULL DEFAULT current_timestamp,
    full_url TEXT NOT NULL,
    valuator_ip VARCHAR(46) NOT NULL, -- supports ipV6
    valuator_user_agent TEXT NOT NULL,
    valuator_id TEXT DEFAULT NULL,  -- client-side username (if authenticated)
    reference TEXT DEFAULT NULL,    -- client-side whatever
    question_text TEXT,
    lowest VARCHAR(40),
    highest VARCHAR(40),
    grade INTEGER NOT NULL,
    comment TEXT,
    FOREIGN KEY (site_id, question_id) REFERENCES valuate_question(site_id, question_id) ON DELETE CASCADE ON UPDATE CASCADE
);

COMMIT;