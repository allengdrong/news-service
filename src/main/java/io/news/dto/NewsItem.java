package io.news.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NewsItem {
    private Long id;
    private String title;
    private String summary;
    private String publisher;
    private LocalDateTime publishedAt;
    private String url;
    private String thumbnailUrl;

    public String getRelativeTime() {
        Duration duration = Duration.between(publishedAt, LocalDateTime.now());
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (minutes < 1) {
            return "방금 전";
        } else if (minutes < 60) {
            return minutes + "분 전";
        } else if (hours < 24) {
            return hours + "시간 전";
        } else {
            return days + "일 전";
        }
    }
}
