package com.publisher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class DistcompApplication {
    public static void main(String[] args) {
        SpringApplication.run(DistcompApplication.class, args);
    }
}
