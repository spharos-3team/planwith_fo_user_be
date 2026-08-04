-- 참고용 DDL (ddl-auto: update 사용 시 자동 생성되지만, 운영 전환 시 이 파일 기준으로 관리하세요)

CREATE TABLE IF NOT EXISTS grade (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    monthly_token INT,
    condition_text VARCHAR(255),
    benefit VARCHAR(255)
);

INSERT INTO grade (id, name, monthly_token, condition_text, benefit)
SELECT 1, '일반회원', 0, '가입 시 기본 지급', '기본 혜택'
WHERE NOT EXISTS (SELECT 1 FROM grade WHERE id = 1);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    grade_id BIGINT,
    follow_id BIGINT,
    email VARCHAR(100) UNIQUE,
    password VARCHAR(100),
    nickname VARCHAR(100) NOT NULL UNIQUE,
    profile_image VARCHAR(255),
    introduction VARCHAR(100),
    login_type VARCHAR(20) NOT NULL,
    provider_id VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS email_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email)
);

-- Refresh tokens are stored hashed in Redis (auth:refresh:*), not MySQL.

CREATE TABLE IF NOT EXISTS terms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    content_url VARCHAR(255),
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 필수 약관 3개 + 선택 약관 예시 1개 (본문: /api/v1/terms/docs/{slug})
INSERT INTO terms (id, title, content_url, is_required, display_order, is_active)
SELECT 1, '이용약관 동의', '/api/v1/terms/docs/service', TRUE, 1, TRUE
WHERE NOT EXISTS (SELECT 1 FROM terms WHERE id = 1);

INSERT INTO terms (id, title, content_url, is_required, display_order, is_active)
SELECT 2, '개인정보 수집 및 이용 동의', '/api/v1/terms/docs/privacy', TRUE, 2, TRUE
WHERE NOT EXISTS (SELECT 1 FROM terms WHERE id = 2);

INSERT INTO terms (id, title, content_url, is_required, display_order, is_active)
SELECT 3, '만 14세 이상입니다', '/api/v1/terms/docs/age', TRUE, 3, TRUE
WHERE NOT EXISTS (SELECT 1 FROM terms WHERE id = 3);

INSERT INTO terms (id, title, content_url, is_required, display_order, is_active)
SELECT 4, '마케팅 정보 수신 동의 (선택)', '/api/v1/terms/docs/marketing', FALSE, 4, TRUE
WHERE NOT EXISTS (SELECT 1 FROM terms WHERE id = 4);

CREATE TABLE IF NOT EXISTS user_agreements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    terms_id BIGINT NOT NULL,
    agreed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
);

-- 비속어 사전 (시드: sql/banned_word_seed_lol2020.sql — 수동 1회 실행)
CREATE TABLE IF NOT EXISTS banned_word (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    word VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    UNIQUE KEY uk_banned_word_word (word)
);
