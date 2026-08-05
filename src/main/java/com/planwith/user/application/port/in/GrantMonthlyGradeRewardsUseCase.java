package com.planwith.user.application.port.in;

public interface GrantMonthlyGradeRewardsUseCase {
    /** @return number of newly created reward rows */
    int grantForPeriod(String periodYm);
}
