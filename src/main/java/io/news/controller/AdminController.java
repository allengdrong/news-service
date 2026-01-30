package io.news.controller;

import io.news.dto.FetchResult;
import io.news.repository.NewsRepository;
import io.news.repository.NewsRepositoryRouter;
import io.news.service.RssFetchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final RssFetchService rssFetchService;
    private final NewsRepository newsRepository;

    @PostMapping("/fetch")
    public ResponseEntity<Map<String, Object>> fetchNews() {
        log.info("RSS 뉴스 수집 요청 (수동)");
        FetchResult result = rssFetchService.fetchAll();
        return ResponseEntity.ok(Map.of(
                "success", !result.hasFailures() || result.getSuccessCount() > 0,
                "message", result.getSummary(),
                "totalNews", result.getTotalCount(),
                "feedsSuccess", result.getSuccessCount(),
                "feedsFail", result.getFailCount(),
                "details", result.getFeedResults()
        ));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        boolean usingEs = false;
        if (newsRepository instanceof NewsRepositoryRouter router) {
            usingEs = router.isUsingElasticsearch();
        }
        return ResponseEntity.ok(Map.of(
                "storage", usingEs ? "elasticsearch" : "in-memory",
                "newsCount", newsRepository.count()
        ));
    }

    @PostMapping("/reset-es")
    public ResponseEntity<Map<String, Object>> resetElasticsearch() {
        if (newsRepository instanceof NewsRepositoryRouter router) {
            router.resetToElasticsearch();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Elasticsearch로 재전환 시도",
                    "usingEs", router.isUsingElasticsearch()
            ));
        }
        return ResponseEntity.ok(Map.of("success", false, "message", "Router 사용 안함"));
    }
}
