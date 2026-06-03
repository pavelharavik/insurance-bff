package com.insurance.bff;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test — verifies the full application context assembles without errors.
 *
 * <p>Uses a mock servlet environment (no real HTTP port) to keep startup fast.
 * Actual endpoint behaviour is covered by integration tests in later test classes.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class InsuranceBffApplicationTest {

    @Test
    void contextLoads() {
        // passes if the application context starts without throwing
    }
}
