CREATE TABLE mestre.user_accounts (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(254) COLLATE "C" NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT uk_user_accounts_email UNIQUE (email),
    CONSTRAINT ck_user_accounts_name CHECK (char_length(btrim(name)) BETWEEN 1 AND 120),
    CONSTRAINT ck_user_accounts_email_canonical CHECK (
        email = lower(email) AND email = btrim(email)
        AND email !~ '[^!-~]' AND char_length(email) BETWEEN 3 AND 254
    ),
    CONSTRAINT ck_user_accounts_password_hash CHECK (char_length(password_hash) BETWEEN 1 AND 255),
    CONSTRAINT ck_user_accounts_role CHECK (role = 'STUDENT'),
    CONSTRAINT ck_user_accounts_status CHECK (status = 'ACTIVE')
);
