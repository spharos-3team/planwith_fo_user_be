package com.planwith.user.application.service;

import com.planwith.user.application.dto.TermsInfo;
import com.planwith.user.application.port.in.GetActiveTermsUseCase;
import com.planwith.user.application.port.out.TermsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetActiveTermsService implements GetActiveTermsUseCase {

    private final TermsPort termsPort;

    @Override
    public List<TermsInfo> getActiveTerms() {
        return termsPort.findAllActive().stream()
                .map(terms -> TermsInfo.builder()
                        .id(terms.getId())
                        .title(terms.getTitle())
                        .contentUrl(terms.getContentUrl())
                        .required(terms.isRequired())
                        .build())
                .toList();
    }
}
