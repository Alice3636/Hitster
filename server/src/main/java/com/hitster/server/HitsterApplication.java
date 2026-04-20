package com.hitster.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.hitster")
public class HitsterApplication {

    public static void main(String[] args) {
        SpringApplication.run(HitsterApplication.class, args);
    }

}