package io.news.service;

import io.news.dto.NewsItem;
import io.news.dto.Page;
import io.news.dto.SearchResult;
import io.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsService {

    private static final int DEFAULT_PAGE_SIZE = 30;

    private final NewsRepository newsRepository;

    public Page<SearchResult> search(String keyword, int page) {
        int totalCount;
        List<NewsItem> source;

        if (newsRepository.count() > 0) {
            totalCount = newsRepository.countByKeyword(keyword);
            source = newsRepository.findByKeyword(keyword, page, DEFAULT_PAGE_SIZE);
        } else {
            List<NewsItem> filtered = getDummyNews().stream()
                    .filter(news -> news.getTitle().toLowerCase().contains(keyword.toLowerCase())
                            || news.getSummary().toLowerCase().contains(keyword.toLowerCase()))
                    .toList();
            totalCount = filtered.size();
            source = filtered.stream()
                    .skip((long) page * DEFAULT_PAGE_SIZE)
                    .limit(DEFAULT_PAGE_SIZE)
                    .toList();
        }

        List<SearchResult> results = source.stream()
                .map(news -> new SearchResult(news, keyword))
                .toList();

        return new Page<>(results, page, DEFAULT_PAGE_SIZE, totalCount);
    }

    public Page<NewsItem> getLatestNews(int page) {
        int totalCount;
        List<NewsItem> content;

        if (newsRepository.count() > 0) {
            totalCount = newsRepository.count();
            content = newsRepository.findAll(page, DEFAULT_PAGE_SIZE);
        } else {
            List<NewsItem> dummy = getDummyNews();
            totalCount = dummy.size();
            content = dummy.stream()
                    .skip((long) page * DEFAULT_PAGE_SIZE)
                    .limit(DEFAULT_PAGE_SIZE)
                    .toList();
        }

        return new Page<>(content, page, DEFAULT_PAGE_SIZE, totalCount);
    }

    private List<NewsItem> getDummyNews() {
        LocalDateTime now = LocalDateTime.now();

        return List.of(
                new NewsItem(1L,
                        "2026년 AI 기술 트렌드 전망 발표",
                        "올해 주목해야 할 인공지능 기술 동향과 산업별 적용 사례를 분석한 보고서가 공개되었다.",
                        "테크뉴스",
                        now.minusMinutes(5),
                        "https://example.com/news/1",
                        null),
                new NewsItem(2L,
                        "국내 스타트업 해외 투자 유치 급증",
                        "올해 1분기 국내 스타트업의 해외 투자 유치 금액이 전년 대비 150% 증가한 것으로 나타났다.",
                        "경제일보",
                        now.minusMinutes(23),
                        "https://example.com/news/2",
                        null),
                new NewsItem(3L,
                        "새로운 반도체 공정 기술 개발 성공",
                        "국내 연구진이 차세대 2나노 반도체 공정 기술 개발에 성공하며 글로벌 경쟁력 확보에 나섰다.",
                        "과학기술신문",
                        now.minusMinutes(47),
                        "https://example.com/news/3",
                        null),
                new NewsItem(4L,
                        "전기차 배터리 수명 2배 늘리는 기술 등장",
                        "새로운 양극재 소재를 적용해 전기차 배터리 수명을 획기적으로 연장하는 기술이 상용화 단계에 접어들었다.",
                        "자동차매거진",
                        now.minusHours(1),
                        "https://example.com/news/4",
                        null),
                new NewsItem(5L,
                        "메타버스 플랫폼 MAU 1억 돌파",
                        "국내 대표 메타버스 플랫폼이 월간 활성 사용자 1억 명을 돌파하며 새로운 이정표를 세웠다.",
                        "디지털타임스",
                        now.minusHours(2),
                        "https://example.com/news/5",
                        null),
                new NewsItem(6L,
                        "정부, 디지털 인재 양성에 5조 투자 계획",
                        "정부가 향후 5년간 디지털 전환 인재 양성을 위해 대규모 투자 계획을 발표했다.",
                        "정책브리핑",
                        now.minusHours(3),
                        "https://example.com/news/6",
                        null),
                new NewsItem(7L,
                        "글로벌 빅테크 기업 한국 R&D 센터 확대",
                        "주요 글로벌 기술 기업들이 한국 내 연구개발 센터를 확대하며 인재 확보 경쟁에 나섰다.",
                        "IT조선",
                        now.minusHours(5),
                        "https://example.com/news/7",
                        null),
                new NewsItem(8L,
                        "사이버 보안 위협 대응 가이드라인 개정",
                        "증가하는 사이버 공격에 대응하기 위한 새로운 보안 가이드라인이 발표되었다.",
                        "보안뉴스",
                        now.minusHours(8),
                        "https://example.com/news/8",
                        null),
                new NewsItem(9L,
                        "클라우드 서비스 시장 점유율 변화",
                        "국내 클라우드 서비스 시장에서 토종 업체들의 점유율이 크게 상승한 것으로 조사됐다.",
                        "클라우드타임즈",
                        now.minusHours(12),
                        "https://example.com/news/9",
                        null),
                new NewsItem(10L,
                        "오픈소스 기반 개발 문화 확산",
                        "국내 기업들 사이에서 오픈소스 소프트웨어 활용과 기여 문화가 빠르게 확산되고 있다.",
                        "개발자뉴스",
                        now.minusHours(18),
                        "https://example.com/news/10",
                        null)
        );
    }
}
