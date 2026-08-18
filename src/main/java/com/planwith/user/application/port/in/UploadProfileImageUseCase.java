package com.planwith.user.application.port.in;

public interface UploadProfileImageUseCase {
    String upload(byte[] imageBytes, String contentType, String originalFilename);
}
