package com.insurance.bff.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Configures a {@link RestClient} for each upstream system with dedicated
 * base URLs and timeouts sourced from {@link UpstreamProperties}.
 */
@Configuration
@EnableConfigurationProperties(UpstreamProperties.class)
public class RestClientConfig {

    @Bean
    RestClient systemARestClient(UpstreamProperties props) {
        return RestClient.builder()
                .baseUrl(props.systemA().url())
                .requestFactory(factory(props.systemA()))
                .build();
    }

    @Bean
    RestClient systemBRestClient(UpstreamProperties props) {
        return RestClient.builder()
                .baseUrl(props.systemB().url())
                .requestFactory(factory(props.systemB()))
                .build();
    }

    private JdkClientHttpRequestFactory factory(UpstreamProperties.SystemProperties props) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(props.connectTimeoutMs()))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(props.readTimeoutMs()));
        return factory;
    }
}
