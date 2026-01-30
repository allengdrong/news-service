package io.news.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import io.news.config.ElasticsearchProperties;
import io.news.document.NewsDocument;
import io.news.dto.NewsItem;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
@ConditionalOnBean(ElasticsearchClient.class)
public class ElasticNewsRepository implements NewsRepository {

    private final ElasticsearchClient client;
    private final ElasticsearchProperties properties;

    @PostConstruct
    public void init() {
        createIndexIfNotExists();
    }

    private void createIndexIfNotExists() {
        try {
            String indexName = properties.getIndexName();
            boolean exists = client.indices().exists(ExistsRequest.of(e -> e.index(indexName))).value();

            if (!exists) {
                client.indices().create(CreateIndexRequest.of(c -> c
                        .index(indexName)
                        .mappings(m -> m
                                .properties("id", p -> p.keyword(k -> k))
                                .properties("title", p -> p.text(t -> t.analyzer("standard")))
                                .properties("summary", p -> p.text(t -> t.analyzer("standard")))
                                .properties("publisher", p -> p.keyword(k -> k))
                                .properties("publishedAt", p -> p.date(d -> d.format("yyyy-MM-dd'T'HH:mm:ss")))
                                .properties("url", p -> p.keyword(k -> k))
                                .properties("imageUrl", p -> p.keyword(k -> k))
                        )
                ));
                log.info("Elasticsearch 인덱스 생성 완료: {}", indexName);
            } else {
                log.info("Elasticsearch 인덱스 이미 존재: {}", indexName);
            }
        } catch (Exception e) {
            log.error("인덱스 생성 실패: {}", e.getMessage());
        }
    }

    @Override
    public void saveAll(List<NewsItem> newsItems) {
        if (newsItems.isEmpty()) return;

        try {
            List<BulkOperation> operations = newsItems.stream()
                    .map(item -> {
                        NewsDocument doc = NewsDocument.from(item);
                        return BulkOperation.of(op -> op
                                .index(idx -> idx
                                        .index(properties.getIndexName())
                                        .id(doc.getId())
                                        .document(doc)
                                )
                        );
                    })
                    .toList();

            BulkResponse response = client.bulk(BulkRequest.of(b -> b.operations(operations)));

            if (response.errors()) {
                log.error("Bulk 저장 중 일부 오류 발생");
            } else {
                log.info("ES 저장 완료: {}건", newsItems.size());
            }
        } catch (Exception e) {
            log.error("ES 저장 실패: {}", e.getMessage());
            throw new RuntimeException("ES 저장 실패", e);
        }
    }

    @Override
    public void save(NewsItem newsItem) {
        try {
            NewsDocument doc = NewsDocument.from(newsItem);
            client.index(IndexRequest.of(i -> i
                    .index(properties.getIndexName())
                    .id(doc.getId())
                    .document(doc)
            ));
        } catch (Exception e) {
            log.error("ES 단건 저장 실패: {}", e.getMessage());
            throw new RuntimeException("ES 저장 실패", e);
        }
    }

    @Override
    public List<NewsItem> findAll() {
        return findAll(0, 1000);
    }

    @Override
    public List<NewsItem> findAll(int page, int size) {
        try {
            log.info("[ES Query] findAll - index: {}, from: {}, size: {}, sort: publishedAt DESC",
                    properties.getIndexName(), page * size, size);

            SearchResponse<NewsDocument> response = client.search(s -> s
                            .index(properties.getIndexName())
                            .from(page * size)
                            .size(size)
                            .sort(sort -> sort.field(f -> f.field("publishedAt").order(SortOrder.Desc))),
                    NewsDocument.class
            );

            log.info("[ES Result] findAll - hits: {}, total: {}",
                    response.hits().hits().size(),
                    response.hits().total() != null ? response.hits().total().value() : "unknown");

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(doc -> doc != null)
                    .map(NewsDocument::toNewsItem)
                    .toList();
        } catch (Exception e) {
            log.error("ES 조회 실패: {}", e.getMessage());
            throw new RuntimeException("ES 조회 실패", e);
        }
    }

    @Override
    public List<NewsItem> findByKeyword(String keyword) {
        return findByKeyword(keyword, 0, 1000);
    }

    @Override
    public List<NewsItem> findByKeyword(String keyword, int page, int size) {
        try {
            log.info("[ES Query] search - index: {}, query: multi_match(title^2, summary) = '{}', from: {}, size: {}, sort: [_score DESC, publishedAt DESC]",
                    properties.getIndexName(), keyword, page * size, size);

            Query multiMatch = MultiMatchQuery.of(m -> m
                    .query(keyword)
                    .fields("title^2", "summary")
            )._toQuery();

            SearchResponse<NewsDocument> response = client.search(s -> s
                            .index(properties.getIndexName())
                            .query(multiMatch)
                            .from(page * size)
                            .size(size)
                            .sort(sort -> sort.score(sc -> sc.order(SortOrder.Desc)))
                            .sort(sort -> sort.field(f -> f.field("publishedAt").order(SortOrder.Desc))),
                    NewsDocument.class
            );

            log.info("[ES Result] search - keyword: '{}', hits: {}, total: {}",
                    keyword, response.hits().hits().size(),
                    response.hits().total() != null ? response.hits().total().value() : "unknown");

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(doc -> doc != null)
                    .map(NewsDocument::toNewsItem)
                    .toList();
        } catch (Exception e) {
            log.error("ES 검색 실패: {}", e.getMessage());
            throw new RuntimeException("ES 검색 실패", e);
        }
    }

    @Override
    public int countByKeyword(String keyword) {
        try {
            Query multiMatch = MultiMatchQuery.of(m -> m
                    .query(keyword)
                    .fields("title^2", "summary")
            )._toQuery();

            CountResponse response = client.count(c -> c
                    .index(properties.getIndexName())
                    .query(multiMatch)
            );

            return (int) response.count();
        } catch (Exception e) {
            log.error("ES 카운트 실패: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public Optional<NewsItem> findById(Long id) {
        try {
            SearchResponse<NewsDocument> response = client.search(s -> s
                            .index(properties.getIndexName())
                            .query(q -> q.term(t -> t.field("id").value(String.valueOf(id))))
                            .size(1),
                    NewsDocument.class
            );

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(doc -> doc != null)
                    .map(NewsDocument::toNewsItem)
                    .findFirst();
        } catch (Exception e) {
            log.error("ES findById 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void clear() {
        try {
            client.deleteByQuery(d -> d
                    .index(properties.getIndexName())
                    .query(q -> q.matchAll(m -> m))
            );
            log.info("ES 인덱스 데이터 삭제 완료");
        } catch (Exception e) {
            log.error("ES clear 실패: {}", e.getMessage());
        }
    }

    @Override
    public int count() {
        try {
            CountResponse response = client.count(c -> c.index(properties.getIndexName()));
            return (int) response.count();
        } catch (Exception e) {
            log.error("ES count 실패: {}", e.getMessage());
            return 0;
        }
    }
}
