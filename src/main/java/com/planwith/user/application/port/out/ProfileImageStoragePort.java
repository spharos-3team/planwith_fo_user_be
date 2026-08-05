package com.planwith.user.application.port.out;

public interface ProfileImageStoragePort {

    String store(byte[] imageBytes, String contentType, String originalFilename);
}
