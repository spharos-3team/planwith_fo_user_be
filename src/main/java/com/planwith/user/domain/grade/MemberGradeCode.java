package com.planwith.user.domain.grade;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public enum MemberGradeCode {
    ROOKIE(1, "새싹", 0, 0, 0, 10),
    LEAF(2, "잎새", 3, 10, 30, 20),
    TRAVELER(3, "여행가", 10, 100, 500, 30),
    EXPLORER(4, "탐험가", 30, 1_000, 5_000, 50),
    ADVENTURER(5, "모험가", 100, 10_000, 30_000, 70),
    MASTER(6, "PLAN&WITH 마스터", 200, 50_000, 150_000, 120);

    private final int sortOrder;
    private final String nameKo;
    private final long minStories;
    private final long minFollowers;
    private final long minLikes;
    private final int monthlyTokenAmount;

    MemberGradeCode(int sortOrder, String nameKo, long minStories, long minFollowers, long minLikes, int monthlyTokenAmount) {
        this.sortOrder = sortOrder;
        this.nameKo = nameKo;
        this.minStories = minStories;
        this.minFollowers = minFollowers;
        this.minLikes = minLikes;
        this.monthlyTokenAmount = monthlyTokenAmount;
    }

    public int sortOrder() {
        return sortOrder;
    }

    public String nameKo() {
        return nameKo;
    }

    public long minStories() {
        return minStories;
    }

    public long minFollowers() {
        return minFollowers;
    }

    public long minLikes() {
        return minLikes;
    }

    public int monthlyTokenAmount() {
        return monthlyTokenAmount;
    }

    public boolean isSatisfiedBy(long storyCount, long followerCount, long likeCount) {
        return storyCount >= minStories
                && followerCount >= minFollowers
                && likeCount >= minLikes;
    }

    public static List<MemberGradeCode> orderedAscending() {
        return Arrays.stream(values())
                .sorted(Comparator.comparingInt(MemberGradeCode::sortOrder))
                .toList();
    }

    public static MemberGradeCode fromCode(String code) {
        return MemberGradeCode.valueOf(code.trim().toUpperCase());
    }
}
