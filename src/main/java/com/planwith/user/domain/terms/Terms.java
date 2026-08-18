package com.planwith.user.domain.terms;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Terms {

    private final Long id;
    private final String title;
    private final String contentUrl;
    private final boolean required;
    private final int displayOrder;
    private final boolean active;
}
