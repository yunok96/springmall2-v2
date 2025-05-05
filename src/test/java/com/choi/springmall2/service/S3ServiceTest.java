package com.choi.springmall2.service;

import com.choi.springmall2.error.exceptions.PreSignedUrlCreationException;
import com.choi.springmall2.error.exceptions.S3FileOperationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Presigner s3Presigner;
    @Mock
    private RedisFileKeyService redisFileKeyService;
    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3Service s3Service;

    @Test
    @DisplayName("PreSigned URL 생성 성공")
    void createPreSignedUrl_Success() throws MalformedURLException {
        // Given
        String path = "test/path/file.txt";
        URL expectedUrl = new URL("https://test-bucket.s3.amazonaws.com/test/path/file.txt?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20230101T000000Z&X-Amz-Expires=60&X-Amz-Signature=testsignature&X-Amz-SignedHeaders=host");

        PresignedPutObjectRequest mockPreSignedRequest = mock(PresignedPutObjectRequest.class); // PresignedPutObjectRequest를 Mock으로 생성
        when(mockPreSignedRequest.url()).thenReturn(expectedUrl); // URL() 메서드가 expectedUrl 반환하도록 설정

        String bucketName = "test-bucket";
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(path)
                .build();

        long presignedUrlDuration = 60;
        PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignedUrlDuration))
                .putObjectRequest(putObjectRequest)
                .build();

        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockPreSignedRequest);

        // When
        String actualUrl = s3Service.createPreSignedUrl(path);

        // Then
        assertEquals(expectedUrl.toString(), actualUrl);
        verify(s3Presigner, times(1)).presignPutObject(any(PutObjectPresignRequest.class));
        verify(mockPreSignedRequest, times(1)).url(); // URL() 메서드 호출 확인
    }

    @Test
    @DisplayName("PreSigned URL 생성 시 S3 키가 null 또는 빈 문자열인 경우 IllegalArgumentException 발생")
    void createPreSignedUrl_Exception() {
        // Given
        String path = "test/path/file.txt";

        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenThrow(new RuntimeException("PreSigner 오류 발생"));

        // When
        PreSignedUrlCreationException exception = assertThrows(PreSignedUrlCreationException.class,
                () -> s3Service.createPreSignedUrl(path));

        // Then
        assertTrue(exception.getMessage().contains("PreSigned URL 생성 중 오류 발생"));
        verify(s3Presigner, times(1)).presignPutObject(any(PutObjectPresignRequest.class));
    }

    @Test
    @DisplayName("임시 파일 이동 성공")
    void moveFromTemp_Success() {
        // Given
        String fileKey = "file.txt";

        // When
        s3Service.moveFromTemp(fileKey);

        // Then
        verify(s3Client, times(1)).copyObject(any(CopyObjectRequest.class));
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("임시 파일 이동 시 S3 키가 null 또는 빈 문자열인 경우 IllegalArgumentException 발생")
    void moveFromTemp_IllegalArgumentException() {
        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> s3Service.moveFromTemp(null));

        // Then
        assertEquals("file key must not be null or blank", exception.getMessage());
        verifyNoInteractions(s3Client);
    }

    @Test
    @DisplayName("임시 파일 복사 실패 시 S3FileOperationException 발생 > 파일 삭제 호출 안됨 확인")
    void moveFromTemp_S3FileOperationException() {
        // Given
        String fileKey = "file.txt";
        when(s3Client.copyObject(any(CopyObjectRequest.class))).thenThrow(S3Exception.builder().message("S3 복사 오류").build());

        // When
        S3FileOperationException exception = assertThrows(S3FileOperationException.class,
                () -> s3Service.moveFromTemp(fileKey));

        // Then
        assertEquals("임시 파일 복사 실패: S3 복사 오류", exception.getMessage());
        verify(s3Client, times(1)).copyObject(any(CopyObjectRequest.class));
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }
}