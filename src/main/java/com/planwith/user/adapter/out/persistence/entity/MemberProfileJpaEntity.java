package com.planwith.user.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberProfileJpaEntity {

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "member_uuid", nullable = false, length = 36)
    private String memberUuid;

    @Column(nullable = false, unique = true, length = 20)
    private String nickname;

    @Column(name = "profile_image", length = 1000)
    private String profileImage;

    @Column(name = "profile_intro", length = 100)
    private String profileIntro;

    @Column(length = 30)
    private String grade;

    @Builder
    public MemberProfileJpaEntity(Long memberId, String memberUuid, String nickname, String profileImage,
                                  String profileIntro, String grade) {
        this.memberId = memberId;
        this.memberUuid = memberUuid;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.profileIntro = profileIntro;
        this.grade = grade;
    }

    public void apply(String nickname, String profileImage, String profileIntro, String grade) {
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.profileIntro = profileIntro;
        this.grade = grade;
    }
}
