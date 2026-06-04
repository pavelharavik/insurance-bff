package com.insurance.bff.infrastructure.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.netty.channel.ChannelOption;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * Configures a {@link WebClient} for each upstream system with dedicated
 * base URLs and timeouts sourced from {@link UpstreamProperties}.
 */
@Configuration
@EnableConfigurationProperties(UpstreamProperties.class)
public class WebClientConfig {

    @Bean
    WebClient systemAWebClient(WebClient.Builder builder, UpstreamProperties props) {
        return buildWebClient(builder, props.systemA());
    }

    @Bean
    WebClient systemBWebClient(WebClient.Builder builder, UpstreamProperties props) {
        return buildWebClient(builder, props.systemB());
    }

    private WebClient buildWebClient(WebClient.Builder builder, UpstreamProperties.SystemProperties props) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.connectTimeoutMs())
                .responseTimeout(Duration.ofMillis(props.readTimeoutMs()));
        return builder
                .baseUrl(props.url())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(c -> c.customCodecs().register(
                        new Jackson2JsonDecoder(new XmlMapper(), MediaType.APPLICATION_XML, MediaType.TEXT_XML)))
                .build();
    }
}
