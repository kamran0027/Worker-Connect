package com.workerconnect.ratelimiter.service;

import java.util.Collections;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.workerconnect.ratelimiter.TokenBucketScript;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    private final TokenBucketScript tokenBucketScript;

    public boolean allowRequest(String key,
                                int capacity,
                                int refillRate) {

        Long result =
                redisTemplate.execute(
                        tokenBucketScript,
                        Collections.singletonList(key),
                        String.valueOf(capacity),
                        String.valueOf(refillRate));

        return result != null && result == 1;

    }

}
