package io.news.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ElasticsearchConfig {

    private final ElasticsearchProperties properties;

    @Bean
    @ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
    public ElasticsearchClient elasticsearchClient() {
        try {
            URI uri = URI.create(properties.getUrl());

            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(properties.getUsername(), properties.getPassword())
            );

            RestClient restClient = RestClient.builder(
                            new HttpHost(uri.getHost(), uri.getPort() == -1 ? 443 : uri.getPort(), uri.getScheme()))
                    .setHttpClientConfigCallback(httpClientBuilder ->
                            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                    .build();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper(objectMapper));

            log.info("Elasticsearch 클라이언트 초기화 완료: {}", properties.getUrl());
            return new ElasticsearchClient(transport);
        } catch (Exception e) {
            log.error("Elasticsearch 클라이언트 초기화 실패: {}", e.getMessage());
            return null;
        }
    }
}
