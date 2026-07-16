package com.workerconnect.ratelimiter;


public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException() {
        super("Too many requests");
    }

}
