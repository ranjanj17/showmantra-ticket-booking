package com.showmantra.services;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Centralized service for distributed Redis locking.
 */
@Service
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public RedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Attempts to acquire a lock in Redis using SETNX.
     * @param key The unique lock key (e.g. lock:show:1:seat:5)
     * @param timeout The duration before the lock auto-expires
     * @return true if lock was acquired, false if already locked
     */
    public boolean acquireLock(String key, Duration timeout) {
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "LOCKED", timeout);
        return Boolean.TRUE.equals(success);
    }

    public void releaseLock(String key) {
        redisTemplate.delete(key);
    }
}
