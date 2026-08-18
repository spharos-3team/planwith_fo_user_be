package com.planwith.user.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "terms")
@Getter
@NoArgsConstructor
public class TermsJpaEntity {

    public static final String TERM_TYPE_REQUIRED = "REQUIRED";
    public static final String TERM_TYPE_OPTIONAL = "OPTIONAL";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "term_id")
    private Long id;

    @Column(name = "term_uuid", nullable = false, unique = true, length = 36)
    private String termUuid;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "term_type", nullable = false, length = 30)
    private String termType;

    @Column(length = 30)
    private String version;

    /** Docs path exposed as API contentUrl (e.g. /api/v1/terms/docs/service). */
    @Column(length = 500)
    private String content;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    public boolean isRequired() {
        return TERM_TYPE_REQUIRED.equalsIgnoreCase(termType);
    }
}
