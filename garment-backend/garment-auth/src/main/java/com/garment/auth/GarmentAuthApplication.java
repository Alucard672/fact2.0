package com.garment.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.garment")
public class GarmentAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(GarmentAuthApplication.class, args);
    }
}