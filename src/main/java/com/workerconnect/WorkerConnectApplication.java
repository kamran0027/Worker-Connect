package com.workerconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
// @EnableRetry
public class WorkerConnectApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkerConnectApplication.class, args);
    }
}
