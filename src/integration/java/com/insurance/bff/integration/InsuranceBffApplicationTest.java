package com.insurance.bff.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test — verifies the full application context assembles without errors.
 */
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class InsuranceBffApplicationTest {

  @Test
  void contextLoads() {
    // passes if the application context starts without throwing
  }
}
