package com.choi.springmall2.service;

import com.choi.springmall2.error.exceptions.RedisSaveException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisFileKeyService {

    private final StringRedisTemplate redisTemplate;

    // 임시 파일 key 를 redis 에 저장
    public void saveTempFileKey(String fileKey) {
        try {
            redisTemplate.opsForValue().set(fileKey, "1", Duration.ofDays(1)); // 1일 후 만료
        } catch (Exception e) {
            throw new RedisSaveException("Redis 저장 중 오류 발생 : " + e);
        }
    }

    // 임시 파일 key 를 redis 에서 삭제
    public void deleteTempFileKey(String fileKey) {
        try {
            redisTemplate.delete(fileKey);
        } catch (Exception e) {
            throw new RedisSaveException("Redis 삭제 중 오류 발생 : " + e);
        }
    }

    // 특정 키 존재 여부 확인
    public boolean exists(String fileKey) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(fileKey));
    }
}
