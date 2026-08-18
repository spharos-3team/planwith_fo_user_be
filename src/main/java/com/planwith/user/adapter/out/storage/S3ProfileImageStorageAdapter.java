package com.planwith.user.adapter.out.storage;

import com.planwith.user.application.port.out.ProfileImageStoragePort;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.Locale;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "aws.s3", name = "enabled", havingValue = "true")
public class S3ProfileImageStorageAdapter implements ProfileImageStoragePort {

    private final String bucket;
    private final String region;
    private final String accessKey;
    private final String secretKey;
    private final String keyPrefix;
    private final String publicBaseUrl;
    private final boolean publicReadAcl;

    public S3ProfileImageStorageAdapter(
            @Value("${aws.s3.bucket}") String bucket,
            @Value("${aws.s3.region:ap-northeast-2}") String region,
            @Value("${aws.s3.access-key:}") String accessKey,
            @Value("${aws.s3.secret-key:}") String secretKey,
            @Value("${aws.s3.key-prefix:profile-images/}") String keyPrefix,
            @Value("${aws.s3.public-base-url:}") String publicBaseUrl,
            @Value("${aws.s3.public-read-acl:false}") boolean publicReadAcl) {
        this.bucket = bucket;
        this.region = region;
        this.accessKey = accessKey == null ? "" : accessKey;
        this.secretKey = secretKey == null ? "" : secretKey;
        this.keyPrefix = normalizePrefix(keyPrefix);
        this.publicBaseUrl = publicBaseUrl == null ? "" : trimTrailingSlash(publicBaseUrl);
        this.publicReadAcl = publicReadAcl;
    }

    @Override
    public String store(byte[] imageBytes, String contentType, String originalFilename) {
        if (!StringUtils.hasText(bucket)) {
            throw new CustomException(ErrorCode.IMAGE_STORAGE_FAILED);
        }

        String extension = extractExtension(originalFilename, contentType);
        String key = keyPrefix + UUID.randomUUID() + "." + extension;

        try (S3Client client = buildClient()) {
            PutObjectRequest.Builder request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .contentLength((long) imageBytes.length);

            if (publicReadAcl) {
                request.acl(ObjectCannedACL.PUBLIC_READ);
            }

            client.putObject(request.build(), RequestBody.fromBytes(imageBytes));
            return buildPublicUrl(key);
        } catch (CustomException e) {
            throw e;
        } catch (S3Exception e) {
            log.error("S3 putObject failed: bucket={}, key={}, status={}", bucket, key, e.statusCode(), e);
            throw new CustomException(ErrorCode.IMAGE_STORAGE_FAILED);
        } catch (Exception e) {
            log.error("S3 upload failed: bucket={}, key={}", bucket, key, e);
            throw new CustomException(ErrorCode.IMAGE_STORAGE_FAILED);
        }
    }

    private String buildPublicUrl(String key) {
        if (StringUtils.hasText(publicBaseUrl)) {
            return publicBaseUrl + "/" + key;
        }
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }

    private S3Client buildClient() {
        var builder = S3Client.builder().region(Region.of(region));
        if (StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey)) {
            builder.credentialsProvider(
                    StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }
        return builder.build();
    }

    private static String normalizePrefix(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return "";
        }
        String value = prefix.trim();
        if (value.startsWith("/")) {
            value = value.substring(1);
        }
        return value.endsWith("/") ? value : value + "/";
    }

    private static String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private static String extractExtension(String originalFilename, String contentType) {
        if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        }
        if (contentType == null) {
            return "jpg";
        }
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/jpeg", "image/jpg" -> "jpg";
            default -> "jpg";
        };
    }
}
