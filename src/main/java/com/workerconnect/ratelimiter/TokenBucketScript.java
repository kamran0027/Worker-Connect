package com.workerconnect.ratelimiter;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
public class TokenBucketScript extends DefaultRedisScript<Long> {

    public TokenBucketScript() {

        setLocation(
                new ClassPathResource("token_bucket.lua"));

        setResultType(Long.class);

    }

}
