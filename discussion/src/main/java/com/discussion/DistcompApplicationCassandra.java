package com.discussion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


@SpringBootApplication
public class DistcompApplicationCassandra {
    public static void main(String[] args) {
        SpringApplication.run(DistcompApplicationCassandra.class, args);
    }
}
