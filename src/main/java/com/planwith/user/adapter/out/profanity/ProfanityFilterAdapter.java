package com.planwith.user.adapter.out.profanity;

import com.planwith.user.adapter.out.persistence.repository.BannedWordJpaRepository;
import com.planwith.user.application.port.out.ProfanityFilterPort;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfanityFilterAdapter implements ProfanityFilterPort {

    /** Fallback when banned_word table is empty (before seed SQL is applied). */
    private static final Set<String> FALLBACK_WORDS = Set.of(
            "시발", "씨발", "병신", "개새끼", "좆", "지랄", "미친놈", "미친년"
    );

    private final BannedWordJpaRepository bannedWordJpaRepository;
    private final AtomicReference<Set<String>> bannedWords = new AtomicReference<>(FALLBACK_WORDS);

    @PostConstruct
    void loadBannedWords() {
        refresh();
    }

    public void refresh() {
        Set<String> fromDb = bannedWordJpaRepository.findAllWords().stream()
                .filter(word -> word != null && !word.isBlank())
                .map(word -> word.replaceAll("\\s+", "").toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
        if (fromDb.isEmpty()) {
            bannedWords.set(FALLBACK_WORDS);
            log.info("banned_word table empty — using fallback dictionary ({} words)", FALLBACK_WORDS.size());
            return;
        }
        bannedWords.set(fromDb);
        log.info("Loaded {} banned words from database", fromDb.size());
    }

    @Override
    public void validate(String text) {
        if (containsProfanity(text)) {
            throw new CustomException(ErrorCode.PROFANITY_DETECTED);
        }
    }

    private boolean containsProfanity(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String normalized = text.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
        return bannedWords.get().stream().anyMatch(normalized::contains);
    }
}
