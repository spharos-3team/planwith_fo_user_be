package com.planwith.user.application.port.in;

import com.planwith.user.application.dto.TermsInfo;

import java.util.List;

public interface GetActiveTermsUseCase {
    List<TermsInfo> getActiveTerms();
}
