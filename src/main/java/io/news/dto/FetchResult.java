package io.news.dto;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class FetchResult {
    private final List<FeedResult> feedResults = new ArrayList<>();
    private int totalCount = 0;
    private int successCount = 0;
    private int failCount = 0;

    public void addSuccess(String feedName, int newsCount) {
        feedResults.add(new FeedResult(feedName, true, newsCount, null));
        totalCount += newsCount;
        successCount++;
    }

    public void addFailure(String feedName, String errorMessage) {
        feedResults.add(new FeedResult(feedName, false, 0, errorMessage));
        failCount++;
    }

    public boolean hasFailures() {
        return failCount > 0;
    }

    public String getSummary() {
        return String.format("총 %d건 수집 (피드 %d개 성공, %d개 실패)",
                totalCount, successCount, failCount);
    }

    @Getter
    public static class FeedResult {
        private final String feedName;
        private final boolean success;
        private final int newsCount;
        private final String errorMessage;

        public FeedResult(String feedName, boolean success, int newsCount, String errorMessage) {
            this.feedName = feedName;
            this.success = success;
            this.newsCount = newsCount;
            this.errorMessage = errorMessage;
        }
    }
}
