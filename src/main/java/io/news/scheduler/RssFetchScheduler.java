package io.news.scheduler;

import io.news.dto.FetchResult;
import io.news.service.RssFetchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RssFetchScheduler {

    private final RssFetchService rssFetchService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("애플리케이션 시작 - 초기 RSS 수집 실행");
        fetchNews();
    }

    @Scheduled(fixedRate = 600000, initialDelay = 600000) // 10분마다, 최초 실행은 10분 후
    public void scheduledFetch() {
        log.info("스케줄러 실행 - RSS 수집 시작");
        fetchNews();
    }

    private void fetchNews() {
        try {
            FetchResult result = rssFetchService.fetchAll();
            if (result.hasFailures()) {
                log.warn("RSS 수집 부분 완료: {}", result.getSummary());
            } else {
                log.info("RSS 수집 완료: {}", result.getSummary());
            }
        } catch (Exception e) {
            log.error("RSS 수집 중 예외 발생 - {}", e.getMessage(), e);
        }
    }
}
