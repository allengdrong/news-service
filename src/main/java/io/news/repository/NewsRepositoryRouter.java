package io.news.repository;

import io.news.dto.NewsItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Primary
@Repository
public class NewsRepositoryRouter implements NewsRepository {

    private final NewsRepository elasticRepository;
    private final NewsRepository inMemoryRepository;
    private volatile boolean useElastic = true;

    @Autowired
    public NewsRepositoryRouter(
            @Autowired(required = false) ElasticNewsRepository elasticRepository,
            InMemoryNewsRepository inMemoryRepository) {
        this.elasticRepository = elasticRepository;
        this.inMemoryRepository = inMemoryRepository;

        if (elasticRepository == null) {
            this.useElastic = false;
            log.warn("Elasticsearch 사용 불가 - InMemory 저장소 사용");
        } else {
            log.info("Elasticsearch 저장소 활성화");
        }
    }

    private NewsRepository getActiveRepository() {
        return useElastic && elasticRepository != null ? elasticRepository : inMemoryRepository;
    }

    private <T> T executeWithFallback(RepositoryOperation<T> operation, String operationName) {
        if (useElastic && elasticRepository != null) {
            try {
                return operation.execute(elasticRepository);
            } catch (Exception e) {
                log.warn("ES {} 실패, InMemory로 fallback: {}", operationName, e.getMessage());
                useElastic = false;
            }
        }
        return operation.execute(inMemoryRepository);
    }

    private void executeWithFallbackVoid(RepositoryVoidOperation operation, String operationName) {
        if (useElastic && elasticRepository != null) {
            try {
                operation.execute(elasticRepository);
                return;
            } catch (Exception e) {
                log.warn("ES {} 실패, InMemory로 fallback: {}", operationName, e.getMessage());
                useElastic = false;
            }
        }
        operation.execute(inMemoryRepository);
    }

    @Override
    public void saveAll(List<NewsItem> newsItems) {
        executeWithFallbackVoid(repo -> repo.saveAll(newsItems), "saveAll");
    }

    @Override
    public void save(NewsItem newsItem) {
        executeWithFallbackVoid(repo -> repo.save(newsItem), "save");
    }

    @Override
    public List<NewsItem> findAll() {
        return executeWithFallback(NewsRepository::findAll, "findAll");
    }

    @Override
    public List<NewsItem> findAll(int page, int size) {
        return executeWithFallback(repo -> repo.findAll(page, size), "findAll(page)");
    }

    @Override
    public List<NewsItem> findByKeyword(String keyword) {
        return executeWithFallback(repo -> repo.findByKeyword(keyword), "findByKeyword");
    }

    @Override
    public List<NewsItem> findByKeyword(String keyword, int page, int size) {
        return executeWithFallback(repo -> repo.findByKeyword(keyword, page, size), "findByKeyword(page)");
    }

    @Override
    public int countByKeyword(String keyword) {
        return executeWithFallback(repo -> repo.countByKeyword(keyword), "countByKeyword");
    }

    @Override
    public Optional<NewsItem> findById(Long id) {
        return executeWithFallback(repo -> repo.findById(id), "findById");
    }

    @Override
    public void clear() {
        executeWithFallbackVoid(NewsRepository::clear, "clear");
    }

    @Override
    public int count() {
        return executeWithFallback(NewsRepository::count, "count");
    }

    public boolean isUsingElasticsearch() {
        return useElastic && elasticRepository != null;
    }

    public void resetToElasticsearch() {
        if (elasticRepository != null) {
            this.useElastic = true;
            log.info("Elasticsearch 저장소로 재전환");
        }
    }

    @FunctionalInterface
    private interface RepositoryOperation<T> {
        T execute(NewsRepository repository);
    }

    @FunctionalInterface
    private interface RepositoryVoidOperation {
        void execute(NewsRepository repository);
    }
}
