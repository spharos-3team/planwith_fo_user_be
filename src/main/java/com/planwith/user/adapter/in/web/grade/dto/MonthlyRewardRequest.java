package com.planwith.user.adapter.in.web.grade.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MonthlyRewardRequest {
    /** yyyy-MM; empty means current month */
    private String periodYm;
}
