package com.choi.springmall2.service;

import com.choi.springmall2.error.exceptions.PreSignedUrlCreationException;
import com.choi.springmall2.error.exceptions.S3FileOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    @Value("${bucket.product.temp.path}")
    private String tempProductFilePath;
    @Value("${bucket.product.real.path}")
    private String realProductFilePath;
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${presigned.url.expiration}")
    private long duration;

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final RedisFileKeyService redisFileKeyService;

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

    public void moveFromTemp(String fileKey) {
        // 파일 이동 전 경로 인자 검증
        if (fileKey == null || fileKey.isBlank()) {
            throw new IllegalArgumentException("file key must not be null or blank");
        }

        String tempKey = tempProductFilePath + fileKey;
        String finalKey = realProductFilePath + fileKey;

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

        redisFileKeyService.deleteTempFileKey(fileKey); // Redis 에서 임시 파일 키 삭제
    }

    // 임시 폴더에 있는 파일 삭제
    public List<String> deleteTempFolderFiles() {
        try {
            String directoryPath = "temp/";

            // 객체 목록을 조회
            ListObjectsV2Request listObjects = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(directoryPath) // 임시 폴더 지정
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listObjects);

            // 삭제된 객체 목록
            List<String> deletedKeys = new ArrayList<>();

            // 각 객체를 삭제
            for (S3Object s3Object : listResponse.contents()) {

                String key = s3Object.key().substring(s3Object.key().lastIndexOf("/") + 1);

                if (!redisFileKeyService.exists(key)) { // Redis key 에 없을 경우 삭제
                    DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(s3Object.key())
                            .build();
                    s3Client.deleteObject(deleteRequest);

                    deletedKeys.add(key);
                }
            }

            return deletedKeys;
        } catch (S3Exception e) {
            throw new S3FileOperationException("파일 삭제 실패: " + e.getMessage());
        } catch (Exception e) {
            throw new S3FileOperationException("파일 삭제 중 예상치 못한 오류 발생: " + e.getMessage());
        }
    }
}
