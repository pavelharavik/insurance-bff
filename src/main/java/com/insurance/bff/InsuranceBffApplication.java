package com.insurance.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * Entry point for the Insurance BFF (Backend for Frontend) service.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Accept doctor requests for patient insurance data by ID.</li>
 *   <li>Fan out to System A and System B concurrently via Reactor operators.</li>
 *   <li>Return the first HTTP 200 response, normalised to a unified format.</li>
 * </ul>
 */
@SpringBootApplication
public class InsuranceBffApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsuranceBffApplication.class, args);
    }
}
