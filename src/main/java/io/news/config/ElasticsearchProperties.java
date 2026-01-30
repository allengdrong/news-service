package io.news.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticsearchProperties {
    private boolean enabled = true;
    private String url;
    private String username;
    private String password;
    private String indexName = "news";
}
