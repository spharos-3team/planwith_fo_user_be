package com.planwith.user.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Seeds minimal grade/terms for local and test smoke runs (temp-fe).
 */
@Slf4j
@Component
@Profile({"local", "local-direct", "test"})
@RequiredArgsConstructor
public class LocalDataSeedRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        Integer grades = jdbcTemplate.queryForObject("select count(*) from grade", Integer.class);
        if (grades != null && grades == 0) {
            jdbcTemplate.update(
                    "insert into grade (id, name, monthly_token, condition_text, benefit) values (1, '일반회원', 0, '가입 시 기본 지급', '기본 혜택')"
            );
            log.info("Seeded default grade id=1");
        }

        Integer terms = jdbcTemplate.queryForObject("select count(*) from terms", Integer.class);
        if (terms != null && terms == 0) {
            jdbcTemplate.update(
                    "insert into terms (id, title, content_url, is_required, display_order, is_active) values (1, '이용약관 동의', '/api/v1/terms/docs/service', true, 1, true)"
            );
            jdbcTemplate.update(
                    "insert into terms (id, title, content_url, is_required, display_order, is_active) values (2, '개인정보 수집 및 이용 동의', '/api/v1/terms/docs/privacy', true, 2, true)"
            );
            jdbcTemplate.update(
                    "insert into terms (id, title, content_url, is_required, display_order, is_active) values (3, '만 14세 이상입니다', '/api/v1/terms/docs/age', true, 3, true)"
            );
            jdbcTemplate.update(
                    "insert into terms (id, title, content_url, is_required, display_order, is_active) values (4, '마케팅 정보 수신 동의 (선택)', '/api/v1/terms/docs/marketing', false, 4, true)"
            );
            log.info("Seeded default terms id=1..4");
        } else {
            // Backfill content URLs for local DBs created before docs existed.
            updateContentUrlIfBlank(1L, "/api/v1/terms/docs/service");
            updateContentUrlIfBlank(2L, "/api/v1/terms/docs/privacy");
            updateContentUrlIfBlank(3L, "/api/v1/terms/docs/age");
            updateContentUrlIfBlank(4L, "/api/v1/terms/docs/marketing");
        }
    }

    private void updateContentUrlIfBlank(long id, String contentUrl) {
        jdbcTemplate.update(
                "update terms set content_url = ? where id = ? and (content_url is null or content_url = '')",
                contentUrl,
                id
        );
    }
}
