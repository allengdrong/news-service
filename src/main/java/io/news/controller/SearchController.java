package io.news.controller;

import io.news.dto.Page;
import io.news.dto.SearchResult;
import io.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SearchController {

    private final NewsService newsService;

    @GetMapping("/search")
    public String search(@RequestParam(name = "q", required = false) String query,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        if (query == null || query.isBlank()) {
            log.info("검색어 없음 - 홈으로 리다이렉트");
            return "redirect:/";
        }

        Page<SearchResult> resultPage = newsService.search(query, page);
        log.info("검색 요청: '{}' - 페이지: {} - 결과 {}건", query, page, resultPage.getTotalElements());
        model.addAttribute("query", query);
        model.addAttribute("resultPage", resultPage);
        model.addAttribute("pageTitle", "검색: " + query);
        return "search";
    }
}
