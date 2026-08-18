package com.planwith.user.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TermsInfo {

    private final Long id;
    private final String title;
    private final String contentUrl;
    private final boolean required;
}
