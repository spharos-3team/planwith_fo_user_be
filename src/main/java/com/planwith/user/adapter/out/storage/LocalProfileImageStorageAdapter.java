package com.planwith.user.adapter.out.storage;

import com.planwith.user.application.port.out.ProfileImageStoragePort;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "aws.s3", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LocalProfileImageStorageAdapter implements ProfileImageStoragePort {

    private final String uploadDir;
    private final String publicBaseUrl;

    public LocalProfileImageStorageAdapter(
            @Value("${file.upload-dir:/tmp/planwith-uploads}") String uploadDir,
            @Value("${file.public-base-url:/files}") String publicBaseUrl) {
        this.uploadDir = uploadDir;
        this.publicBaseUrl = publicBaseUrl.endsWith("/")
                ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
                : publicBaseUrl;
    }

    @Override
    public String store(byte[] imageBytes, String contentType, String originalFilename) {
        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);

            String ext = extractExtension(originalFilename);
            String fileName = UUID.randomUUID() + "." + ext;
            Path target = dir.resolve(fileName);
            Files.write(target, imageBytes);

            return publicBaseUrl + "/" + fileName;
        } catch (IOException e) {
            throw new CustomException(ErrorCode.IMAGE_STORAGE_FAILED);
        }
    }

    private String extractExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename) || !originalFilename.contains(".")) {
            return "jpg";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
    }
}
