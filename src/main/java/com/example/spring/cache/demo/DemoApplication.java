package com.example.spring.cache.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;

@SpringBootApplication
@Slf4j
public class DemoApplication implements CommandLineRunner {

    private final CacheManager cacheManager;

    public DemoApplication(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... strings) {
        log.info("\n\n" + "==============================================================================\n"
                + "Using cache manager: " + this.cacheManager.getClass().getName() + "\n"
                + "==============================================================================\n\n");
    }
}

