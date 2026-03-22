-- =============================================================================
-- V1__init_schema.sql
-- Full initial schema for CanDor fintech wallet MVP.
--
-- NOTE FOR DEVELOPERS:
--   This migration runs on a fresh database.
--   If your local dev DB already has tables created by ddl-auto=update, run:
--     DROP SCHEMA public CASCADE;
--     CREATE SCHEMA public;
--   then restart the application to let Flyway run this migration cleanly.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- users
-- ---------------------------------------------------------------------------
CREATE TABLE users (
    id                  uuid            NOT NULL,
    first_name          VARCHAR(50)     NOT NULL,
    last_name           VARCHAR(50)     NOT NULL,
    other_names         VARCHAR(100),
    email               VARCHAR(150)    NOT NULL,
    phone               VARCHAR(20)     NOT NULL,
    password_hash       VARCHAR(255)    NOT NULL,
    role                VARCHAR(20)     NOT NULL DEFAULT 'ROLE_USER',
    kyc_level           VARCHAR(10)     NOT NULL DEFAULT 'TIER_0',
    account_status      VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    email_verified      BOOLEAN         NOT NULL DEFAULT false,
    phone_verified      BOOLEAN         NOT NULL DEFAULT false,
    biometric_enabled   BOOLEAN         NOT NULL DEFAULT false,
    mfa_enabled         BOOLEAN         NOT NULL DEFAULT true,
    last_login_at       TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT now(),
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_phone UNIQUE (phone)
);

-- ---------------------------------------------------------------------------
-- wallets
-- ---------------------------------------------------------------------------
CREATE TABLE wallets (
    id                          uuid            NOT NULL,
    user_id                     uuid            NOT NULL,
    balance                     DECIMAL(15,2)   NOT NULL DEFAULT 0.00,
    ledger_balance              DECIMAL(15,2)   NOT NULL DEFAULT 0.00,
    currency                    VARCHAR(3)      NOT NULL DEFAULT 'NGN',
    daily_transaction_limit     DECIMAL(15,2)   NOT NULL DEFAULT 50000.00,
    status                      VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    version                     BIGINT          NOT NULL DEFAULT 0,
    created_at                  TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at                  TIMESTAMP       NOT NULL DEFAULT now(),
    CONSTRAINT pk_wallets PRIMARY KEY (id),
    CONSTRAINT uq_wallets_user_id UNIQUE (user_id),
    CONSTRAINT fk_wallets_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ---------------------------------------------------------------------------
-- fee_configurations
-- ---------------------------------------------------------------------------
CREATE TABLE fee_configurations (
    id              uuid            NOT NULL,
    service_type    VARCHAR(30)     NOT NULL,
    fee_type        VARCHAR(15)     NOT NULL,
    fee_value       DECIMAL(10,4)   NOT NULL,
    min_fee         DECIMAL(10,2),
    max_fee         DECIMAL(10,2),
    is_active       BOOLEAN         NOT NULL DEFAULT true,
    created_by      uuid,
    updated_by      uuid,
    created_at      TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT now(),
    CONSTRAINT pk_fee_configurations PRIMARY KEY (id),
    CONSTRAINT uq_fee_configurations_service_type UNIQUE (service_type),
    CONSTRAINT fk_fee_config_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_fee_config_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)
);

-- ---------------------------------------------------------------------------
-- saved_beneficiaries
-- ---------------------------------------------------------------------------
CREATE TABLE saved_beneficiaries (
    id              uuid        NOT NULL,
    user_id         uuid        NOT NULL,
    type            VARCHAR(20) NOT NULL,
    nickname        VARCHAR(50),
    phone_number    VARCHAR(20),
    network         VARCHAR(20),
    meter_number    VARCHAR(20),
    disco           VARCHAR(50),
    bank_code       VARCHAR(10),
    bank_name       VARCHAR(100),
    account_number  VARCHAR(20),
    account_name    VARCHAR(100),
    is_active       BOOLEAN     NOT NULL DEFAULT true,
    created_at      TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT pk_saved_beneficiaries PRIMARY KEY (id),
    CONSTRAINT fk_beneficiaries_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ---------------------------------------------------------------------------
-- notification_preferences
-- ---------------------------------------------------------------------------
CREATE TABLE notification_preferences (
    id                      uuid            NOT NULL,
    user_id                 uuid            NOT NULL,
    push_enabled            BOOLEAN         NOT NULL DEFAULT true,
    email_enabled           BOOLEAN         NOT NULL DEFAULT true,
    sms_enabled             BOOLEAN         NOT NULL DEFAULT true,
    transaction_alerts      BOOLEAN         NOT NULL DEFAULT true,
    low_balance_alerts      BOOLEAN         NOT NULL DEFAULT true,
    promotional_alerts      BOOLEAN         NOT NULL DEFAULT false,
    low_balance_threshold   DECIMAL(10,2)   DEFAULT 500.00,
    updated_at              TIMESTAMP       NOT NULL DEFAULT now(),
    CONSTRAINT pk_notification_preferences PRIMARY KEY (id),
    CONSTRAINT uq_notification_preferences_user_id UNIQUE (user_id),
    CONSTRAINT fk_notif_prefs_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ---------------------------------------------------------------------------
-- kyc_documents
-- ---------------------------------------------------------------------------
CREATE TABLE kyc_documents (
    id                  uuid        NOT NULL,
    user_id             uuid        NOT NULL,
    document_type       VARCHAR(30) NOT NULL,
    document_number     VARCHAR(50) NOT NULL,
    document_url        VARCHAR(500),
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    verified_by         uuid,
    verified_at         TIMESTAMP,
    rejection_reason    VARCHAR(255),
    created_at          TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT pk_kyc_documents PRIMARY KEY (id),
    CONSTRAINT fk_kyc_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_kyc_verified_by FOREIGN KEY (verified_by) REFERENCES users (id)
);

-- ---------------------------------------------------------------------------
-- otp_tokens
-- ---------------------------------------------------------------------------
CREATE TABLE otp_tokens (
    id          uuid        NOT NULL,
    user_id     uuid        NOT NULL,
    otp_code    VARCHAR(10) NOT NULL,
    purpose     VARCHAR(20) NOT NULL,
    expires_at  TIMESTAMP   NOT NULL,
    used_at     TIMESTAMP,
    is_used     BOOLEAN     NOT NULL DEFAULT false,
    created_at  TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT pk_otp_tokens PRIMARY KEY (id),
    CONSTRAINT fk_otp_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ---------------------------------------------------------------------------
-- refresh_tokens
-- ---------------------------------------------------------------------------
CREATE TABLE refresh_tokens (
    id          uuid        NOT NULL,
    user_id     uuid        NOT NULL,
    token_hash  VARCHAR(255) NOT NULL,
    device_info VARCHAR(255),
    ip_address  VARCHAR(45),
    expires_at  TIMESTAMP   NOT NULL,
    revoked_at  TIMESTAMP,
    is_revoked  BOOLEAN     NOT NULL DEFAULT false,
    created_at  TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uq_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ---------------------------------------------------------------------------
-- api_provider_configs
-- ---------------------------------------------------------------------------
CREATE TABLE api_provider_configs (
    id                  uuid        NOT NULL,
    service_type        VARCHAR(30) NOT NULL,
    provider_name       VARCHAR(50) NOT NULL,
    is_active           BOOLEAN     NOT NULL DEFAULT false,
    base_url            VARCHAR(255) NOT NULL,
    api_key_ref         VARCHAR(255) NOT NULL,
    webhook_secret_ref  VARCHAR(255),
    config_metadata     jsonb,
    updated_by          uuid,
    created_at          TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT pk_api_provider_configs PRIMARY KEY (id),
    CONSTRAINT fk_api_config_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)
);

-- ---------------------------------------------------------------------------
-- audit_logs  (no FK on actor_id — records must survive user deletion)
-- ---------------------------------------------------------------------------
CREATE TABLE audit_logs (
    id              uuid        NOT NULL,
    actor_id        uuid,
    actor_role      VARCHAR(20) NOT NULL,
    action          VARCHAR(100) NOT NULL,
    resource_type   VARCHAR(50),
    resource_id     VARCHAR(100),
    details         jsonb,
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(255),
    created_at      TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT pk_audit_logs PRIMARY KEY (id)
);

-- ---------------------------------------------------------------------------
-- transactions
-- ---------------------------------------------------------------------------
CREATE TABLE transactions (
    id                  uuid            NOT NULL,
    reference           VARCHAR(50)     NOT NULL,
    user_id             uuid            NOT NULL,
    wallet_id           uuid            NOT NULL,
    type                VARCHAR(30)     NOT NULL,
    amount              DECIMAL(15,2)   NOT NULL,
    fee                 DECIMAL(15,2)   NOT NULL DEFAULT 0.00,
    total_deducted      DECIMAL(15,2)   NOT NULL,
    direction           VARCHAR(10)     NOT NULL,
    status              VARCHAR(20)     NOT NULL,
    provider            VARCHAR(50),
    provider_reference  VARCHAR(100),
    provider_response   TEXT,
    description         VARCHAR(255),
    metadata            jsonb,
    failure_reason      VARCHAR(255),
    ip_address          VARCHAR(45),
    completed_at        TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT now(),
    CONSTRAINT pk_transactions PRIMARY KEY (id),
    CONSTRAINT uq_transactions_reference UNIQUE (reference),
    CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_transactions_wallet FOREIGN KEY (wallet_id) REFERENCES wallets (id)
);

-- ---------------------------------------------------------------------------
-- airtime_purchases
-- ---------------------------------------------------------------------------
CREATE TABLE airtime_purchases (
    id              uuid            NOT NULL,
    transaction_id  uuid            NOT NULL,
    recipient_phone VARCHAR(20)     NOT NULL,
    network         VARCHAR(15)     NOT NULL,
    amount          DECIMAL(10,2)   NOT NULL,
    is_gift         BOOLEAN         NOT NULL DEFAULT false,
    created_at      TIMESTAMP       NOT NULL DEFAULT now(),
    CONSTRAINT pk_airtime_purchases PRIMARY KEY (id),
    CONSTRAINT uq_airtime_transaction_id UNIQUE (transaction_id),
    CONSTRAINT fk_airtime_transaction FOREIGN KEY (transaction_id) REFERENCES transactions (id)
);

-- ---------------------------------------------------------------------------
-- data_purchases
-- ---------------------------------------------------------------------------
CREATE TABLE data_purchases (
    id              uuid            NOT NULL,
    transaction_id  uuid            NOT NULL,
    recipient_phone VARCHAR(20)     NOT NULL,
    network         VARCHAR(15)     NOT NULL,
    bundle_id       VARCHAR(50)     NOT NULL,
    bundle_name     VARCHAR(100)    NOT NULL,
    validity        VARCHAR(50)     NOT NULL,
    amount          DECIMAL(10,2)   NOT NULL,
    is_gift         BOOLEAN         NOT NULL DEFAULT false,
    created_at      TIMESTAMP       NOT NULL DEFAULT now(),
    CONSTRAINT pk_data_purchases PRIMARY KEY (id),
    CONSTRAINT uq_data_transaction_id UNIQUE (transaction_id),
    CONSTRAINT fk_data_transaction FOREIGN KEY (transaction_id) REFERENCES transactions (id)
);

-- ---------------------------------------------------------------------------
-- electricity_purchases
-- ---------------------------------------------------------------------------
CREATE TABLE electricity_purchases (
    id               uuid            NOT NULL,
    transaction_id   uuid            NOT NULL,
    disco            VARCHAR(50)     NOT NULL,
    meter_number     VARCHAR(20)     NOT NULL,
    meter_type       VARCHAR(10)     NOT NULL,
    customer_name    VARCHAR(100),
    customer_address VARCHAR(255),
    amount           DECIMAL(10,2)   NOT NULL,
    units            VARCHAR(20),
    token            VARCHAR(50),
    created_at       TIMESTAMP       NOT NULL DEFAULT now(),
    CONSTRAINT pk_electricity_purchases PRIMARY KEY (id),
    CONSTRAINT uq_electricity_transaction_id UNIQUE (transaction_id),
    CONSTRAINT fk_electricity_transaction FOREIGN KEY (transaction_id) REFERENCES transactions (id)
);

-- ---------------------------------------------------------------------------
-- bank_transfers
-- ---------------------------------------------------------------------------
CREATE TABLE bank_transfers (
    id                          uuid            NOT NULL,
    transaction_id              uuid            NOT NULL,
    recipient_bank_code         VARCHAR(10)     NOT NULL,
    recipient_bank_name         VARCHAR(100)    NOT NULL,
    recipient_account_number    VARCHAR(20)     NOT NULL,
    recipient_account_name      VARCHAR(100)    NOT NULL,
    amount                      DECIMAL(15,2)   NOT NULL,
    narration                   VARCHAR(255),
    provider_transfer_id        VARCHAR(100),
    created_at                  TIMESTAMP       NOT NULL DEFAULT now(),
    CONSTRAINT pk_bank_transfers PRIMARY KEY (id),
    CONSTRAINT uq_bank_transfer_transaction_id UNIQUE (transaction_id),
    CONSTRAINT fk_bank_transfer_transaction FOREIGN KEY (transaction_id) REFERENCES transactions (id)
);

-- ---------------------------------------------------------------------------
-- notifications
-- ---------------------------------------------------------------------------
CREATE TABLE notifications (
    id              uuid        NOT NULL,
    user_id         uuid        NOT NULL,
    type            VARCHAR(20) NOT NULL,
    title           VARCHAR(100) NOT NULL,
    body            TEXT        NOT NULL,
    is_read         BOOLEAN     NOT NULL DEFAULT false,
    transaction_id  uuid,
    channel         VARCHAR(10) NOT NULL,
    sent_at         TIMESTAMP,
    created_at      TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_notifications_transaction FOREIGN KEY (transaction_id) REFERENCES transactions (id)
);

-- ---------------------------------------------------------------------------
-- fraud_flags
-- ---------------------------------------------------------------------------
CREATE TABLE fraud_flags (
    id                  uuid        NOT NULL,
    user_id             uuid        NOT NULL,
    transaction_id      uuid,
    flag_type           VARCHAR(25) NOT NULL,
    description         TEXT        NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    reviewed_by         uuid,
    reviewed_at         TIMESTAMP,
    resolution_notes    TEXT,
    created_at          TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT pk_fraud_flags PRIMARY KEY (id),
    CONSTRAINT fk_fraud_flags_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_fraud_flags_transaction FOREIGN KEY (transaction_id) REFERENCES transactions (id),
    CONSTRAINT fk_fraud_flags_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES users (id)
);

-- ---------------------------------------------------------------------------
-- password_reset_tokens
-- ---------------------------------------------------------------------------
CREATE TABLE password_reset_tokens (
    id          uuid        NOT NULL,
    token_hash  VARCHAR(255) NOT NULL,
    user_id     uuid        NOT NULL,
    expires_at  TIMESTAMP   NOT NULL,
    used        BOOLEAN     NOT NULL DEFAULT false,
    CONSTRAINT pk_password_reset_tokens PRIMARY KEY (id),
    CONSTRAINT uq_password_reset_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ---------------------------------------------------------------------------
-- addresses
-- ---------------------------------------------------------------------------
CREATE TABLE addresses (
    id              uuid            NOT NULL,
    user_id         uuid            NOT NULL,
    street_address  VARCHAR(255)    NOT NULL,
    city            VARCHAR(100)    NOT NULL,
    state           VARCHAR(100)    NOT NULL,
    country         VARCHAR(100)    NOT NULL,
    postal_code     VARCHAR(20),
    address_type    VARCHAR(10)     NOT NULL DEFAULT 'HOME',
    is_primary      BOOLEAN         NOT NULL DEFAULT false,
    created_at      TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT now(),
    CONSTRAINT pk_addresses PRIMARY KEY (id),
    CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- =============================================================================
-- Indexes (from PRD section 12)
-- =============================================================================

-- transactions
CREATE INDEX idx_transactions_user_id    ON transactions (user_id);
CREATE INDEX idx_transactions_status     ON transactions (status);
CREATE INDEX idx_transactions_type       ON transactions (type);
CREATE INDEX idx_transactions_created_at ON transactions (created_at DESC);
CREATE INDEX idx_transactions_reference  ON transactions (reference);

-- wallets
CREATE UNIQUE INDEX idx_wallets_user_id ON wallets (user_id);

-- notifications
CREATE INDEX idx_notifications_user_id ON notifications (user_id);
CREATE INDEX idx_notifications_is_read ON notifications (is_read);

-- saved_beneficiaries
CREATE INDEX idx_beneficiaries_user_id ON saved_beneficiaries (user_id);

-- otp_tokens
CREATE INDEX idx_otp_user_purpose ON otp_tokens (user_id, purpose);
CREATE INDEX idx_otp_expires_at   ON otp_tokens (expires_at);

-- fraud_flags
CREATE INDEX idx_fraud_flags_user_id ON fraud_flags (user_id);
CREATE INDEX idx_fraud_flags_status  ON fraud_flags (status);

-- audit_logs
CREATE INDEX idx_audit_logs_actor_id   ON audit_logs (actor_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at DESC);

-- addresses
CREATE INDEX idx_addresses_user_id ON addresses (user_id);
