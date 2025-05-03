package com.choi.springmall2.service;

import com.choi.springmall2.error.exceptions.PreSignedUrlCreationException;
import com.choi.springmall2.error.exceptions.S3FileOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class S3Service {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${presigned.url.expiration}")
    private long duration;
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    public String createPreSignedUrl(String path) {
        try {
            // S3 업로드 버킷 설정
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(path)
                    .build();

            // 해당 요청에 대한 서명 PreSigned 제한시간 설정
            PutObjectPresignRequest preSignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(duration))
                    .putObjectRequest(putObjectRequest)
                    .build();

            // PreSigned URL 반환
            return s3Presigner.presignPutObject(preSignRequest).url().toString();
        } catch (Exception e) {
            throw new PreSignedUrlCreationException("PreSigned URL 생성 중 오류 발생: " + e.getMessage());
        }
    }

    public void moveFromTemp(String tempKey, String finalKey) {
        // 파일 이동 전 경로 인자 검증
        if (tempKey == null || finalKey == null || tempKey.isBlank() || finalKey.isBlank()) {
            throw new IllegalArgumentException("S3 key must not be null or blank");
        }

        try {
            // 복사 요청
            CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                    .sourceBucket(bucket)
                    .sourceKey(tempKey)
                    .destinationBucket(bucket)
                    .destinationKey(finalKey)
                    .build();
            s3Client.copyObject(copyRequest);
        } catch (S3Exception e) {
            throw new S3FileOperationException("임시 파일 복사 실패: " + e.getMessage());
        } catch (Exception e) {
            throw new S3FileOperationException("임시 파일 복사 중 예상치 못한 오류 발생: " + e.getMessage());
        }

        try {
            // 임시 파일 삭제
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(tempKey)
                    .build();
            s3Client.deleteObject(deleteRequest);
        } catch (S3Exception e) {
            throw new S3FileOperationException("임시 파일 삭제 실패: " + e.getMessage());
        } catch (Exception e) {
            throw new S3FileOperationException("임시 파일 삭제 중 예상치 못한 오류 발생: " + e.getMessage());
        }
    }
}
