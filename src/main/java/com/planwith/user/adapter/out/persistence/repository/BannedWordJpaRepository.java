package com.planwith.user.adapter.out.persistence.repository;

import com.planwith.user.adapter.out.persistence.entity.BannedWordJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BannedWordJpaRepository extends JpaRepository<BannedWordJpaEntity, Long> {

    @Query("select b.word from BannedWordJpaEntity b")
    List<String> findAllWords();
}
