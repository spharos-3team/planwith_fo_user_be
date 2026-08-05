package com.planwith.user.application.port.out;

import com.planwith.user.domain.terms.Terms;

import java.util.List;

public interface TermsPort {

    List<Terms> findAllActive();

    List<Long> findRequiredActiveIds();
}
