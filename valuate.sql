BEGIN;

DROP TABLE IF EXISTS feedback_attribute;
DROP TABLE IF EXISTS valuate_feedback;
DROP TABLE IF EXISTS question_attribute;
DROP TABLE IF EXISTS attribute_field;
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
    url_prefix TEXT NOT NULL,
    PRIMARY KEY (site_id, url_prefix)
);

CREATE TABLE valuate_question (
    site_id BIGINT NOT NULL REFERENCES valuate_site(site_id) ON DELETE CASCADE,
    question_id VARCHAR(32) NOT NULL, -- hex-encoded md5(question_text)
    lock BOOLEAN NOT NULL DEFAULT false,
    user_note TEXT,
    created TIMESTAMP NOT NULL DEFAULT current_timestamp,
    modified TIMESTAMP NOT NULL DEFAULT current_timestamp,
    PRIMARY KEY (site_id, question_id)
);

-- Content of this attribute_field table was originally intended to stored in columns of valuate_question and valuate_feedback tables,
-- but my superior kept inventing new attributes on daily basis and I didn't want Raymond Boyce and Edgar Codd to roll in their graves,
-- so I reorganized it into attribute_field, question_attribute and feedback_attribute tables.

CREATE TABLE attribute_field (
    attribute_field_id VARCHAR(256) PRIMARY KEY, -- attribute name in the HTML
    in_snippet_editor BOOLEAN NOT NULL DEFAULT false,
    provided_by_feedback BOOLEAN NOT NULL DEFAULT false,
    mandatory BOOLEAN NOT NULL default false,
    default_value TEXT DEFAULT NULL,
    admin_note TEXT DEFAULT NULL
);

INSERT INTO attribute_field (attribute_field_id, admin_note, in_snippet_editor) VALUES ('user-logo', 'URL to the branding image.', true);
INSERT INTO attribute_field (attribute_field_id, admin_note, in_snippet_editor) VALUES ('user-link', 'Link to the user''s website.', true);
INSERT INTO attribute_field (attribute_field_id, admin_note, in_snippet_editor) VALUES ('collapsed', 'Boolean flag that tells if the valuate window is collapsed.', true);
INSERT INTO attribute_field (attribute_field_id, admin_note, in_snippet_editor) VALUES ('title', 'Title of the valuate window.', true);
INSERT INTO attribute_field (attribute_field_id, admin_note, in_snippet_editor) VALUES ('lowest', 'Meaning of the lowest (leftmost) value.', true);
INSERT INTO attribute_field (attribute_field_id, admin_note, in_snippet_editor) VALUES ('highest', 'Meaning of the highest (rightmost) value.', true);
INSERT INTO attribute_field (attribute_field_id, admin_note, in_snippet_editor, default_value) VALUES ('emoji', 'Identifier of the emoji set.', true, 'bw');

-- Feedback attributes
INSERT INTO attribute_field (attribute_field_id, admin_note, provided_by_feedback, in_snippet_editor) VALUES ('question', 'Question, as seen by the valuator.', true, true);
INSERT INTO attribute_field (attribute_field_id, admin_note, provided_by_feedback) VALUES ('valuator-id', 'Username of the valuator, if authenticated.', true);
INSERT INTO attribute_field (attribute_field_id, admin_note, provided_by_feedback) VALUES ('reference', 'A place for an additional identifier, to be set by the user.', true);
INSERT INTO attribute_field (attribute_field_id, admin_note, provided_by_feedback) VALUES ('grade', 'Valuator''s grade.', true);
INSERT INTO attribute_field (attribute_field_id, admin_note, provided_by_feedback) VALUES ('comment', 'Valuator''s comment.', true);
INSERT INTO attribute_field (attribute_field_id, admin_note, provided_by_feedback) VALUES ('full-url', 'Valuator''s comment.', true);


CREATE TABLE question_attribute (
    site_id BIGINT NOT NULL,
    question_id VARCHAR(32) NOT NULL,
    attribute_field_id VARCHAR(256) NOT NULL,
    attribute_value TEXT NOT NULL,
    FOREIGN KEY (site_id, question_id) REFERENCES valuate_question(site_id, question_id) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY (site_id, question_id, attribute_field_id)
);

CREATE TABLE valuate_feedback (
    feedback_id BIGSERIAL PRIMARY KEY,
    site_id BIGINT NOT NULL,    -- Constant in the javascript
    question_id VARCHAR(32) NOT NULL,   -- Either provided by the attribute or computed as md5(question_text)
    received TIMESTAMP NOT NULL DEFAULT current_timestamp,  -- currentTimeMillis when the request is received
    valuator_ip VARCHAR(46) NOT NULL, -- supports ipV6
    valuator_user_agent TEXT NOT NULL,  -- comes as a http header
    FOREIGN KEY (site_id, question_id) REFERENCES valuate_question(site_id, question_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE feedback_attribute (
    feedback_id BIGSERIAL REFERENCES valuate_feedback(feedback_id) ON DELETE CASCADE,
    attribute_field_id VARCHAR(256) REFERENCES attribute_field(attribute_field_id) ON DELETE CASCADE ON UPDATE CASCADE,
    attribute_value TEXT NOT NULL,
    PRIMARY KEY (feedback_id, attribute_field_id)
);

COMMIT;
