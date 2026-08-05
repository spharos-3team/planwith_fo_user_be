package com.planwith.user.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Seeds minimal terms for local and test smoke runs.
 */
@Slf4j
@Component
@Profile({"local", "local-direct", "test"})
@RequiredArgsConstructor
public class LocalDataSeedRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        Integer terms = jdbcTemplate.queryForObject("select count(*) from terms", Integer.class);
        if (terms != null && terms == 0) {
            insertTerm(1L, "이용약관 동의", "REQUIRED", "/api/v1/terms/docs/service");
            insertTerm(2L, "개인정보 수집 및 이용 동의", "REQUIRED", "/api/v1/terms/docs/privacy");
            insertTerm(3L, "만 14세 이상입니다", "REQUIRED", "/api/v1/terms/docs/age");
            insertTerm(4L, "마케팅 정보 수신 동의 (선택)", "OPTIONAL", "/api/v1/terms/docs/marketing");
            log.info("Seeded default terms term_id=1..4");
        } else {
            updateContentIfBlank(1L, "/api/v1/terms/docs/service");
            updateContentIfBlank(2L, "/api/v1/terms/docs/privacy");
            updateContentIfBlank(3L, "/api/v1/terms/docs/age");
            updateContentIfBlank(4L, "/api/v1/terms/docs/marketing");
        }
    }

    private void insertTerm(long termId, String title, String termType, String content) {
        jdbcTemplate.update(
                """
                        insert into terms (term_id, term_uuid, title, term_type, version, content, is_active)
                        values (?, ?, ?, ?, '1.0', ?, true)
                        """,
                termId,
                UUID.randomUUID().toString(),
                title,
                termType,
                content
        );
    }

    private void updateContentIfBlank(long termId, String content) {
        jdbcTemplate.update(
                "update terms set content = ? where term_id = ? and (content is null or content = '')",
                content,
                termId
        );
    }
}
