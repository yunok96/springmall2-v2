package com.choi.springmall2.service;

import com.choi.springmall2.error.exceptions.RedisSaveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisFileKeyServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisFileKeyService redisFileKeyService;

    private final String fileKey = "testFileKey";

    @Test
    @DisplayName("Redis 저장 성공 테스트")
    void saveTempFileKey_success() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), anyString(), any());

        // when
        assertDoesNotThrow(() -> redisFileKeyService.saveTempFileKey(fileKey));

        // then
        verify(valueOperations, times(1)).set(eq(fileKey), eq("1"), any());
    }

    @Test
    @DisplayName("Redis 저장 실패 테스트")
    void saveTempFileKey_fail() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doThrow(new RuntimeException("Redis 오류")).when(valueOperations).set(anyString(), anyString(), any());

        // when & then
        assertThrows(RuntimeException.class, () -> redisFileKeyService.saveTempFileKey(fileKey));
    }

    @Test
    @DisplayName("Redis 삭제 성공 테스트")
    void deleteTempFileKey_success() {
        // when
        redisFileKeyService.deleteTempFileKey(fileKey);

        // then
        verify(redisTemplate, times(1)).delete(fileKey);
    }

    @Test
    @DisplayName("Redis 삭제 실패 테스트 - 예외 발생")
    void deleteTempFileKey_fail() {
        // given
        doThrow(new RuntimeException("삭제 실패")).when(redisTemplate).delete(fileKey);

        // when & then
        assertThrows(RedisSaveException.class, () -> redisFileKeyService.deleteTempFileKey(fileKey));
    }

    @Test
    @DisplayName("Redis 키 존재 여부 확인 테스트 - 존재할 때")
    void exists_true() {
        // given
        when(redisTemplate.hasKey(fileKey)).thenReturn(true);

        // when
        boolean result = redisFileKeyService.exists(fileKey);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("Redis 키 존재 여부 확인 테스트 - 존재하지 않을 때")
    void exists_false() {
        // given
        when(redisTemplate.hasKey(fileKey)).thenReturn(false);

        // when
        boolean result = redisFileKeyService.exists(fileKey);

        // then
        assertFalse(result);
    }
}