package com.hitster.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(
        scanBasePackages = "com.hitster",
        exclude = {UserDetailsServiceAutoConfiguration.class}
)
public class HitsterApplication {

    public static void main(String[] args) {
        SpringApplication.run(HitsterApplication.class, args);
    }
}