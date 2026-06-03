package com.insurance.bff.service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.insurance.bff.client.InsuranceClient;
import com.insurance.bff.exception.InsuranceNotFoundException;
import com.insurance.bff.model.InsuranceData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies that {@link InsuranceService#getInsuranceData} caches results by patient ID.
 *
 * <p>Client B always returns an error, making client A the only possible success path.
 * This removes non-determinism from the race between the two reactive subscriptions,
 * so {@code verify(clientA, times(1))} is a reliable assertion.
 */
@ExtendWith(SpringExtension.class)
@Import({InsuranceService.class, InsuranceCacheTest.TestConfig.class})
class InsuranceCacheTest {

    private static final String        PATIENT_ID = "123";
    private static final InsuranceData DATA       = new InsuranceData("123", "Alice", true);

    @TestConfiguration
    @EnableCaching
    static class TestConfig {

        @Bean("systemAClient")
        InsuranceClient clientA() {
            return Mockito.mock(InsuranceClient.class);
        }

        @Bean("systemBClient")
        InsuranceClient clientB() {
            return id -> Mono.error(new InsuranceNotFoundException(id));
        }

        @Bean
        CacheManager cacheManager() {
            CaffeineCacheManager manager = new CaffeineCacheManager("insurance");
            manager.setCaffeine(Caffeine.newBuilder().maximumSize(100));
            manager.setAsyncCacheMode(true);
            return manager;
        }
    }

    @Autowired private InsuranceService insuranceService;
    @Autowired private CacheManager     cacheManager;
    @Autowired @Qualifier("systemAClient") private InsuranceClient clientA;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("insurance").clear();
        Mockito.reset(clientA);
    }

    @Test
    void getInsuranceData_doesNotHitUpstream_onCacheHit() {
        when(clientA.fetchById(PATIENT_ID)).thenReturn(Mono.just(DATA));

        insuranceService.getInsuranceData(PATIENT_ID).block();
        insuranceService.getInsuranceData(PATIENT_ID).block();

        verify(clientA, times(1)).fetchById(PATIENT_ID);
    }

    @Test
    void getInsuranceData_hitsUpstreamForEachDistinctPatientId() {
        when(clientA.fetchById("111")).thenReturn(Mono.just(new InsuranceData("111", "Alice", true)));
        when(clientA.fetchById("222")).thenReturn(Mono.just(new InsuranceData("222", "Bob", false)));

        insuranceService.getInsuranceData("111").block();
        insuranceService.getInsuranceData("222").block();

        verify(clientA, times(1)).fetchById("111");
        verify(clientA, times(1)).fetchById("222");
    }
}
