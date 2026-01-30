package io.news.repository;

import io.news.dto.NewsItem;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryNewsRepository implements NewsRepository {

    private final Map<Long, NewsItem> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public void saveAll(List<NewsItem> newsItems) {
        newsItems.forEach(this::save);
    }

    @Override
    public void save(NewsItem newsItem) {
        Long id = newsItem.getId();
        if (id == null) {
            id = idGenerator.getAndIncrement();
            newsItem = new NewsItem(
                    id,
                    newsItem.getTitle(),
                    newsItem.getSummary(),
                    newsItem.getPublisher(),
                    newsItem.getPublishedAt(),
                    newsItem.getUrl(),
                    newsItem.getThumbnailUrl()
            );
        }
        storage.put(id, newsItem);
    }

    @Override
    public List<NewsItem> findAll() {
        return storage.values().stream()
                .sorted(Comparator.comparing(NewsItem::getPublishedAt).reversed())
                .toList();
    }

    @Override
    public List<NewsItem> findAll(int page, int size) {
        return findAll().stream()
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    @Override
    public List<NewsItem> findByKeyword(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return findAll().stream()
                .filter(news -> news.getTitle().toLowerCase().contains(lowerKeyword)
                        || news.getSummary().toLowerCase().contains(lowerKeyword))
                .toList();
    }

    @Override
    public List<NewsItem> findByKeyword(String keyword, int page, int size) {
        return findByKeyword(keyword).stream()
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    @Override
    public int countByKeyword(String keyword) {
        return findByKeyword(keyword).size();
    }

    @Override
    public Optional<NewsItem> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public void clear() {
        storage.clear();
        idGenerator.set(1);
    }

    @Override
    public int count() {
        return storage.size();
    }
}
