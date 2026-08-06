-- Source-of-truth DDL for PlanWith FO user BE (MySQL).
-- Local profile uses spring.jpa.hibernate.ddl-auto=update, so Hibernate will add/alter
-- columns from entities. For a clean local E2E MySQL migrate from the old single-table model,
-- DROP legacy tables first if they still exist:
--   DROP TABLE IF EXISTS user_agreements, users, grade;
-- Then apply this script (or rely on ddl-auto + LocalDataSeedRunner for terms).

CREATE TABLE IF NOT EXISTS member (
    member_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_uuid CHAR(36) NOT NULL,
    phone_number VARCHAR(30) NULL,
    status VARCHAR(20) NOT NULL, -- ACTIVE | SUSPENDED | DELETED (soft withdraw uses DELETED)
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at DATETIME(6) NULL,
    UNIQUE KEY uk_member_uuid (member_uuid)
);

CREATE TABLE IF NOT EXISTS member_auths (
    auth_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    login_type VARCHAR(20) NOT NULL,
    email VARCHAR(100) NULL,
    password VARCHAR(100) NULL,
    social_id VARCHAR(100) NULL,
    last_login_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    KEY idx_member_auths_member_id (member_id),
    KEY idx_member_auths_local_email (login_type, email),
    KEY idx_member_auths_social (login_type, social_id),
    CONSTRAINT fk_member_auths_member FOREIGN KEY (member_id) REFERENCES member (member_id)
);

CREATE TABLE IF NOT EXISTS member_profile (
    member_id BIGINT PRIMARY KEY,
    member_uuid CHAR(36) NOT NULL,
    nickname VARCHAR(20) NOT NULL,
    profile_image VARCHAR(1000) NULL,
    profile_intro VARCHAR(100) NULL,
    grade VARCHAR(30) NULL,
    UNIQUE KEY uk_member_profile_nickname (nickname),
    CONSTRAINT fk_member_profile_member FOREIGN KEY (member_id) REFERENCES member (member_id)
);

CREATE TABLE IF NOT EXISTS follow (
    follow_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    follow_uuid CHAR(36) NOT NULL,
    follower_member_uuid CHAR(36) NOT NULL,
    followee_member_uuid CHAR(36) NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    UNIQUE KEY uk_follow_uuid (follow_uuid),
    UNIQUE KEY uk_follow_follower_followee (follower_member_uuid, followee_member_uuid)
);

CREATE TABLE IF NOT EXISTS terms (
    term_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    term_uuid CHAR(36) NOT NULL,
    title VARCHAR(100) NOT NULL,
    term_type VARCHAR(30) NOT NULL,
    version VARCHAR(30) NULL,
    content VARCHAR(500) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    UNIQUE KEY uk_terms_uuid (term_uuid)
);

-- content holds docs path (API field contentUrl). term_type REQUIRED|OPTIONAL maps API required flag.
INSERT INTO terms (term_id, term_uuid, title, term_type, version, content, is_active)
SELECT 1, '11111111-1111-1111-1111-111111111111', '이용약관 동의', 'REQUIRED', '1.0', '/api/v1/terms/docs/service', 1
WHERE NOT EXISTS (SELECT 1 FROM terms WHERE term_id = 1);

INSERT INTO terms (term_id, term_uuid, title, term_type, version, content, is_active)
SELECT 2, '22222222-2222-2222-2222-222222222222', '개인정보 수집 및 이용 동의', 'REQUIRED', '1.0', '/api/v1/terms/docs/privacy', 1
WHERE NOT EXISTS (SELECT 1 FROM terms WHERE term_id = 2);

INSERT INTO terms (term_id, term_uuid, title, term_type, version, content, is_active)
SELECT 3, '33333333-3333-3333-3333-333333333333', '만 14세 이상입니다', 'REQUIRED', '1.0', '/api/v1/terms/docs/age', 1
WHERE NOT EXISTS (SELECT 1 FROM terms WHERE term_id = 3);

INSERT INTO terms (term_id, term_uuid, title, term_type, version, content, is_active)
SELECT 4, '44444444-4444-4444-4444-444444444444', '마케팅 정보 수신 동의 (선택)', 'OPTIONAL', '1.0', '/api/v1/terms/docs/marketing', 1
WHERE NOT EXISTS (SELECT 1 FROM terms WHERE term_id = 4);

CREATE TABLE IF NOT EXISTS member_term_agreements (
    agreement_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    term_id BIGINT NOT NULL,
    member_uuid CHAR(36) NOT NULL,
    agreed TINYINT(1) NOT NULL,
    agreed_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    KEY idx_member_term_agreements_member_uuid (member_uuid),
    KEY idx_member_term_agreements_term_id (term_id)
);

CREATE TABLE IF NOT EXISTS email_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL,
    verified TINYINT(1) NOT NULL DEFAULT 0,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_email (email)
);

-- Refresh tokens are stored hashed in Redis (auth:refresh:*), not MySQL.

-- 비속어 사전 (시드: sql/banned_word_seed_lol2020.sql — 수동 1회 실행)
CREATE TABLE IF NOT EXISTS banned_word (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    word VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    UNIQUE KEY uk_banned_word_word (word)
);

-- Member grade system (aligned to grade ERD)
-- If migrating from the previous grade tables locally:
--   DROP TABLE IF EXISTS grade_reward_history, member_grade_metric, member_grade, grade_member, grade_benefit, grade_condition, grade;

CREATE TABLE IF NOT EXISTS grade (
    grade_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    grade_code VARCHAR(30) NOT NULL,
    grade_name VARCHAR(50) NOT NULL,
    grade_level INT NOT NULL,
    description TEXT NULL,
    UNIQUE KEY uk_grade_code (grade_code),
    UNIQUE KEY uk_grade_level (grade_level)
);

CREATE TABLE IF NOT EXISTS grade_condition (
    condition_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    grade_id BIGINT NOT NULL,
    metric_type VARCHAR(50) NOT NULL,
    condition_name VARCHAR(100) NOT NULL,
    threshold_value BIGINT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    description TEXT NULL,
    UNIQUE KEY uk_grade_condition (grade_id, metric_type),
    CONSTRAINT fk_grade_condition_grade FOREIGN KEY (grade_id) REFERENCES grade (grade_id)
);

CREATE TABLE IF NOT EXISTS grade_benefit (
    benefit_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    grade_id BIGINT NOT NULL,
    benefit_code VARCHAR(50) NOT NULL,
    benefit_name VARCHAR(100) NOT NULL,
    benefit_value VARCHAR(200) NULL,
    description TEXT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_grade_benefit (grade_id, benefit_code),
    CONSTRAINT fk_grade_benefit_grade FOREIGN KEY (grade_id) REFERENCES grade (grade_id)
);

-- PK is member_uuid (one current grade per member). grade_id is FK, not PK.
CREATE TABLE IF NOT EXISTS grade_member (
    member_uuid CHAR(36) NOT NULL PRIMARY KEY,
    grade_uuid CHAR(36) NOT NULL,
    grade_id BIGINT NOT NULL,
    grade_status VARCHAR(20) NOT NULL,
    grade_assigned_at DATETIME(6) NOT NULL,
    last_evaluated_at DATETIME(6) NULL,
    UNIQUE KEY uk_grade_member_uuid (grade_uuid),
    CONSTRAINT fk_grade_member_grade FOREIGN KEY (grade_id) REFERENCES grade (grade_id)
);

CREATE TABLE IF NOT EXISTS member_grade_metric (
    metric_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_uuid CHAR(36) NOT NULL,
    metric_type VARCHAR(50) NOT NULL,
    current_value BIGINT NOT NULL DEFAULT 0,
    source_service VARCHAR(50) NOT NULL,
    source_version BIGINT NOT NULL DEFAULT 0,
    synchronized_at DATETIME(6) NOT NULL,
    UNIQUE KEY uk_member_grade_metric (member_uuid, metric_type)
);

CREATE TABLE IF NOT EXISTS grade_reward_history (
    reward_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_uuid CHAR(36) NOT NULL,
    grade_id BIGINT NOT NULL,
    reward_month CHAR(7) NOT NULL,
    token_amount INT NOT NULL,
    reward_status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    UNIQUE KEY uk_grade_reward_month (member_uuid, reward_month),
    KEY idx_grade_reward_member_uuid (member_uuid),
    CONSTRAINT fk_grade_reward_grade FOREIGN KEY (grade_id) REFERENCES grade (grade_id)
);
