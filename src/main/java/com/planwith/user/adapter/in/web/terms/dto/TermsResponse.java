package com.planwith.user.adapter.in.web.terms.dto;

import com.planwith.user.application.dto.TermsInfo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TermsResponse {

    private Long id;
    private String title;
    private String contentUrl;
    private boolean required;

    public static TermsResponse from(TermsInfo info) {
        return TermsResponse.builder()
                .id(info.getId())
                .title(info.getTitle())
                .contentUrl(info.getContentUrl())
                .required(info.isRequired())
                .build();
    }
}
