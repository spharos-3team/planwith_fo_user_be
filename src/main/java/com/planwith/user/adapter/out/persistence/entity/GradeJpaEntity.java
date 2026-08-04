package com.planwith.user.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "grade")
@Getter
@NoArgsConstructor
public class GradeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(name = "monthly_token")
    private Integer monthlyToken;

    @Column(name = "condition_text", length = 255)
    private String conditionText;

    @Column(length = 255)
    private String benefit;
}
