package com.insurance.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Entry point for the Insurance BFF (Backend for Frontend) service.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Accept doctor requests for patient insurance data by ID.</li>
 *   <li>Fan out to System A and System B concurrently via virtual threads.</li>
 *   <li>Return the first HTTP 200 response, normalised to a unified format.</li>
 * </ul>
 *
 * <p>Virtual threads are activated globally via {@code spring.threads.virtual.enabled=true}
 * in {@code application.yml}, so no per-request configuration is required here.
 */
@SpringBootApplication
@EnableCaching
public class InsuranceBffApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsuranceBffApplication.class, args);
    }
}
