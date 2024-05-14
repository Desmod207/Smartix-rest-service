DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS application_user;
DROP TYPE IF EXISTS gender_type;

CREATE TYPE gender_type AS ENUM (
    'MALE',
    'FEMALE'
);

CREATE TABLE IF NOT EXISTS application_user (
    id SERIAL,
    login VARCHAR(50) NOT NULL,
    password TEXT NOT NULL,
    balance BIGINT NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    patronymic VARCHAR(100),
    email VARCHAR(255),
    gender gender_type,
    birthday DATE,
    UNIQUE (id),
    UNIQUE (login),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS payment (
    id SERIAL PRIMARY KEY,
    date TIMESTAMP,
    phone VARCHAR(25),
    amount BIGINT,
    user_id INT
);

ALTER TABLE payment
    ADD FOREIGN KEY (user_id) REFERENCES application_user(id) ON DELETE CASCADE;