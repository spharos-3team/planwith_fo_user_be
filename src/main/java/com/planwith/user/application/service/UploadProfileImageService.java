package com.planwith.user.application.service;

import com.planwith.user.application.port.in.UploadProfileImageUseCase;
import com.planwith.user.application.port.out.ImageModerationPort;
import com.planwith.user.application.port.out.ProfileImageStoragePort;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UploadProfileImageService implements UploadProfileImageUseCase {

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/jpg", "image/png", "image/webp");
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final int REQUIRED_WIDTH = 400;
    private static final int REQUIRED_HEIGHT = 400;

    private final ImageModerationPort imageModerationPort;
    private final ProfileImageStoragePort profileImageStoragePort;

    @Override
    public String upload(byte[] imageBytes, String contentType, String originalFilename) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_FORMAT);
        }
        validateBasics(imageBytes, contentType);
        imageModerationPort.validateSafeContent(imageBytes);
        return profileImageStoragePort.store(imageBytes, contentType, originalFilename);
    }

    private void validateBasics(byte[] bytes, String contentType) {
        if (bytes.length > MAX_FILE_SIZE) {
            throw new CustomException(ErrorCode.IMAGE_TOO_LARGE);
        }
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_FORMAT);
        }

        try (ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                throw new CustomException(ErrorCode.INVALID_IMAGE_FORMAT);
            }
            if (image.getWidth() != REQUIRED_WIDTH || image.getHeight() != REQUIRED_HEIGHT) {
                throw new CustomException(ErrorCode.INVALID_IMAGE_DIMENSION);
            }
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_FORMAT);
        }
    }
}
