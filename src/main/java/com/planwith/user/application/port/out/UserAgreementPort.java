package com.planwith.user.application.port.out;

import java.util.List;

public interface UserAgreementPort {

    void saveAgreements(String memberUuid, List<Long> termsIds);
}
