package com.planwith.user.adapter.out.storage;

import com.planwith.user.application.port.out.ImageModerationPort;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectModerationLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectModerationLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.ModerationLabel;
import software.amazon.awssdk.services.rekognition.model.RekognitionException;

import java.util.List;

@Slf4j
@Component
public class RekognitionImageModerationAdapter implements ImageModerationPort {

    private static final List<String> BLOCKED_TOP_LEVEL_CATEGORIES = List.of(
            "Explicit Nudity",
            "Violence",
            "Visually Disturbing",
            "Weapons"
    );

    private final boolean enabled;
    private final String accessKey;
    private final String secretKey;
    private final String region;
    private final float confidenceThreshold;

    public RekognitionImageModerationAdapter(
            @Value("${aws.rekognition.enabled:true}") boolean enabled,
            @Value("${aws.rekognition.access-key:}") String accessKey,
            @Value("${aws.rekognition.secret-key:}") String secretKey,
            @Value("${aws.rekognition.region:ap-northeast-2}") String region,
            @Value("${aws.rekognition.confidence-threshold:70}") float confidenceThreshold) {
        this.enabled = enabled;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
        this.confidenceThreshold = confidenceThreshold;
    }

    @Override
    public void validateSafeContent(byte[] imageBytes) {
        if (!enabled) {
            log.warn("aws.rekognition.enabled=false — skipping image moderation");
            return;
        }

        try (RekognitionClient client = buildClient()) {
            DetectModerationLabelsRequest request = DetectModerationLabelsRequest.builder()
                    .image(Image.builder()
                            .bytes(SdkBytes.fromByteArray(imageBytes))
                            .build())
                    .minConfidence(confidenceThreshold)
                    .build();

            DetectModerationLabelsResponse response = client.detectModerationLabels(request);

            boolean blocked = response.moderationLabels().stream()
                    .anyMatch(this::isBlockedLabel);

            if (blocked) {
                logDetectedLabels(response.moderationLabels());
                throw new CustomException(ErrorCode.INAPPROPRIATE_IMAGE);
            }
        } catch (CustomException e) {
            throw e;
        } catch (RekognitionException e) {
            log.error("Rekognition call failed", e);
            throw new CustomException(ErrorCode.IMAGE_MODERATION_FAILED);
        }
    }

    private boolean isBlockedLabel(ModerationLabel label) {
        String topLevelName = label.parentName() == null || label.parentName().isBlank()
                ? label.name()
                : label.parentName();
        return BLOCKED_TOP_LEVEL_CATEGORIES.contains(topLevelName);
    }

    private void logDetectedLabels(List<ModerationLabel> labels) {
        labels.forEach(l -> log.info("Rekognition label: {} (confidence {}%, parent {})",
                l.name(), l.confidence(), l.parentName()));
    }

    private RekognitionClient buildClient() {
        var builder = RekognitionClient.builder()
                .region(Region.of(region));

        if (!accessKey.isBlank() && !secretKey.isBlank()) {
            builder.credentialsProvider(
                    StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));
        }

        return builder.build();
    }
}
