package com.planwith.user.adapter.out.profanity;

import com.planwith.user.adapter.out.persistence.repository.BannedWordJpaRepository;
import com.planwith.user.application.port.out.ProfanityFilterPort;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfanityFilterAdapter implements ProfanityFilterPort {

    /** Always merged into the live dictionary (DB seed may store jamo variants only). */
    private static final Set<String> CORE_WORDS = Set.of(
            "시발", "씨발", "시팔", "씨팔", "병신", "개새끼", "좆", "지랄", "미친놈", "미친년"
    );

    private final BannedWordJpaRepository bannedWordJpaRepository;
    private final AtomicReference<Set<String>> bannedWords = new AtomicReference<>(normalizeAll(CORE_WORDS));

    @PostConstruct
    void loadBannedWords() {
        refresh();
    }

    public void refresh() {
        Set<String> fromDb = bannedWordJpaRepository.findAllWords().stream()
                .filter(word -> word != null && !word.isBlank())
                .map(this::normalize)
                .filter(word -> !word.isBlank())
                .collect(Collectors.toCollection(HashSet::new));
        fromDb.addAll(normalizeAll(CORE_WORDS));
        bannedWords.set(Set.copyOf(fromDb));
        log.info("Loaded {} banned words (db+core, NFKC-normalized)", fromDb.size());
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
        String normalized = normalize(text);
        return bannedWords.get().stream().anyMatch(normalized::contains);
    }

    private Set<String> normalizeAll(Set<String> words) {
        return words.stream().map(this::normalize).collect(Collectors.toUnmodifiableSet());
    }

    /** 자모/완성형 표기를 같은 형태로 맞추고 공백을 제거한다. */
    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        String compact = text.replaceAll("\\s+", "");
        return Normalizer.normalize(compact, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
    }
}
