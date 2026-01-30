package io.news.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.news.dto.NewsItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsDocument {
    private String id;
    private String title;
    private String summary;
    private String publisher;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime publishedAt;

    private String url;
    private String imageUrl;

    public static NewsDocument from(NewsItem item) {
        return NewsDocument.builder()
                .id(generateId(item.getUrl()))
                .title(item.getTitle())
                .summary(item.getSummary())
                .publisher(item.getPublisher())
                .publishedAt(item.getPublishedAt())
                .url(item.getUrl())
                .imageUrl(item.getThumbnailUrl())
                .build();
    }

    public NewsItem toNewsItem() {
        return new NewsItem(
                hashToLong(id),
                title,
                summary,
                publisher,
                publishedAt,
                url,
                imageUrl
        );
    }

    public static String generateId(String url) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(url.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return String.valueOf(url.hashCode());
        }
    }

    private Long hashToLong(String hash) {
        try {
            return Long.parseLong(hash.substring(0, 15), 16);
        } catch (Exception e) {
            return (long) hash.hashCode();
        }
    }
}
