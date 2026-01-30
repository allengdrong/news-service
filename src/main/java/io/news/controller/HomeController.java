package io.news.controller;

import io.news.dto.NewsItem;
import io.news.dto.Page;
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
public class HomeController {

    private final NewsService newsService;

    @GetMapping("/")
    public String home(@RequestParam(defaultValue = "0") int page, Model model) {
        log.info("홈 페이지 접속 - 페이지: {}", page);
        Page<NewsItem> newsPage = newsService.getLatestNews(page);
        model.addAttribute("pageTitle", "홈");
        model.addAttribute("newsPage", newsPage);
        return "index";
    }
}
