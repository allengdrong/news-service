package io.news.dto;

import lombok.Getter;

@Getter
public class SearchResult {
    private final NewsItem news;
    private final String highlightedTitle;
    private final String highlightedSummary;

    public SearchResult(NewsItem news, String keyword) {
        this.news = news;
        this.highlightedTitle = highlight(news.getTitle(), keyword);
        this.highlightedSummary = highlight(news.getSummary(), keyword);
    }

    private String highlight(String text, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return text;
        }
        // 대소문자 무시하면서 원본 텍스트의 케이스 유지
        return text.replaceAll("(?i)(" + escapeRegex(keyword) + ")", "<mark>$1</mark>");
    }

    private String escapeRegex(String str) {
        return str.replaceAll("([\\\\\\[\\](){}.*+?^$|])", "\\\\$1");
    }
}
