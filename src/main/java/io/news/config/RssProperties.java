package io.news.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "news.rss")
public class RssProperties {

    private List<FeedConfig> feeds = new ArrayList<>();

    @Getter
    @Setter
    public static class FeedConfig {
        private String name;
        private String url;
    }
}
