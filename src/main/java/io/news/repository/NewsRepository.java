package io.news.repository;

import io.news.dto.NewsItem;

import java.util.List;
import java.util.Optional;

public interface NewsRepository {

    void saveAll(List<NewsItem> newsItems);

    void save(NewsItem newsItem);

    List<NewsItem> findAll();

    List<NewsItem> findAll(int page, int size);

    List<NewsItem> findByKeyword(String keyword);

    List<NewsItem> findByKeyword(String keyword, int page, int size);

    int countByKeyword(String keyword);

    Optional<NewsItem> findById(Long id);

    void clear();

    int count();
}
