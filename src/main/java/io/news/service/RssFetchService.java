package io.news.service;

import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.rometools.modules.mediarss.MediaEntryModule;
import com.rometools.modules.mediarss.types.MediaContent;
import com.rometools.modules.mediarss.types.Thumbnail;
import io.news.config.RssProperties;
import io.news.dto.FetchResult;
import io.news.dto.NewsItem;
import io.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class RssFetchService {

    private final RssProperties rssProperties;
    private final NewsRepository newsRepository;

    public FetchResult fetchAll() {
        FetchResult result = new FetchResult();
        List<NewsItem> allNews = new ArrayList<>();

        for (RssProperties.FeedConfig feed : rssProperties.getFeeds()) {
            try {
                List<NewsItem> news = fetchFromFeed(feed);
                allNews.addAll(news);
                result.addSuccess(feed.getName(), news.size());
                log.info("[성공] {} - {}건 수집", feed.getName(), news.size());
            } catch (Exception e) {
                result.addFailure(feed.getName(), e.getMessage());
                log.error("[실패] {} - {}", feed.getName(), e.getMessage());
            }
        }

        // ES는 동일 ID면 upsert, InMemory는 덮어쓰기
        newsRepository.saveAll(allNews);
        log.info("수집 완료: {}", result.getSummary());

        return result;
    }

    private List<NewsItem> fetchFromFeed(RssProperties.FeedConfig feedConfig) throws Exception {
        List<NewsItem> newsItems = new ArrayList<>();

        try (XmlReader reader = new XmlReader(URI.create(feedConfig.getUrl()).toURL())) {
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(reader);

            for (SyndEntry entry : feed.getEntries()) {
                NewsItem newsItem = new NewsItem(
                        null,
                        cleanTitle(entry.getTitle()),
                        extractSummary(entry),
                        feedConfig.getName(),
                        convertToLocalDateTime(entry.getPublishedDate()),
                        entry.getLink(),
                        extractThumbnail(entry)
                );
                newsItems.add(newsItem);
            }
        }

        return newsItems;
    }

    private String cleanTitle(String title) {
        if (title == null) return "";
        // 구글 뉴스 등에서 제목 뒤에 붙는 출처 제거
        return title.replaceAll(" - [^-]+$", "").trim();
    }

    private String extractSummary(SyndEntry entry) {
        if (entry.getDescription() != null && entry.getDescription().getValue() != null) {
            // Jsoup으로 HTML 태그/엔티티 깔끔하게 제거
            String summary = Jsoup.parse(entry.getDescription().getValue()).text().trim();
            // 200자 제한
            if (summary.length() > 200) {
                summary = summary.substring(0, 200) + "...";
            }
            return summary;
        }
        return "";
    }

    private String extractThumbnail(SyndEntry entry) {
        // 1. Media RSS 모듈에서 썸네일 추출
        MediaEntryModule mediaModule = (MediaEntryModule) entry.getModule(MediaEntryModule.URI);
        if (mediaModule != null) {
            // 썸네일 확인
            Thumbnail[] thumbnails = mediaModule.getMetadata() != null
                    ? mediaModule.getMetadata().getThumbnail()
                    : null;
            if (thumbnails != null && thumbnails.length > 0 && thumbnails[0].getUrl() != null) {
                return thumbnails[0].getUrl().toString();
            }

            // 미디어 콘텐츠에서 URL 추출 (type 유무와 관계없이)
            MediaContent[] contents = mediaModule.getMediaContents();
            if (contents != null && contents.length > 0) {
                for (MediaContent content : contents) {
                    if (content.getReference() != null) {
                        String url = content.getReference().toString();
                        // type이 있으면 image인지 확인, 없으면 URL 확장자로 판단
                        if (content.getType() != null) {
                            if (content.getType().startsWith("image")) {
                                return url;
                            }
                        } else if (isImageUrl(url)) {
                            return url;
                        }
                    }
                }
                // 위에서 못 찾으면 첫 번째 URL이라도 반환
                if (contents[0].getReference() != null) {
                    return contents[0].getReference().toString();
                }
            }

            // MediaGroup 내의 콘텐츠도 확인
            if (mediaModule.getMediaGroups() != null) {
                for (var group : mediaModule.getMediaGroups()) {
                    if (group.getContents() != null) {
                        for (MediaContent content : group.getContents()) {
                            if (content.getReference() != null) {
                                return content.getReference().toString();
                            }
                        }
                    }
                }
            }
        }

        // 2. Enclosure에서 이미지 추출
        for (SyndEnclosure enclosure : entry.getEnclosures()) {
            if (enclosure.getUrl() != null) {
                if (enclosure.getType() != null && enclosure.getType().startsWith("image")) {
                    return enclosure.getUrl();
                } else if (isImageUrl(enclosure.getUrl())) {
                    return enclosure.getUrl();
                }
            }
        }

        // 3. Description에서 img 태그 추출
        if (entry.getDescription() != null && entry.getDescription().getValue() != null) {
            String desc = entry.getDescription().getValue();
            Pattern imgPattern = Pattern.compile("<img[^>]+src=[\"']([^\"']+)[\"']");
            Matcher matcher = imgPattern.matcher(desc);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        return null;
    }

    private boolean isImageUrl(String url) {
        if (url == null) return false;
        String lower = url.toLowerCase();
        return lower.contains(".jpg") || lower.contains(".jpeg") ||
               lower.contains(".png") || lower.contains(".gif") ||
               lower.contains(".webp") || lower.contains("image");
    }

    private LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) {
            return LocalDateTime.now();
        }
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
