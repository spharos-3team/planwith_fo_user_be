package com.planwith.user.application.port.out;

public interface ImageModerationPort {

    void validateSafeContent(byte[] imageBytes);
}
