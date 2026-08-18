package com.planwith.user.adapter.out.persistence.repository;

import com.planwith.user.adapter.out.persistence.entity.GradeMemberJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeMemberJpaRepository extends JpaRepository<GradeMemberJpaEntity, String> {

    List<GradeMemberJpaEntity> findAll();
}
