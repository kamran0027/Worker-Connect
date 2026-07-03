package com.workerconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class WorkerConnectApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkerConnectApplication.class, args);
    }
}
