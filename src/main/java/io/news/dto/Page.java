package io.news.dto;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Page<T> {
    private static final int PAGE_GROUP_SIZE = 5;

    private final List<T> content;
    private final int page;
    private final int size;
    private final int totalElements;
    private final int totalPages;

    public Page(List<T> content, int page, int size, int totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
    }

    public boolean isFirst() {
        return page == 0;
    }

    public boolean isLast() {
        return page >= totalPages - 1;
    }

    public boolean hasPrevious() {
        return page > 0;
    }

    public boolean hasNext() {
        return page < totalPages - 1;
    }

    public int getFirstPage() {
        return 0;
    }

    public int getLastPage() {
        return Math.max(0, totalPages - 1);
    }

    public int getPreviousPage() {
        return Math.max(0, page - 1);
    }

    public int getNextPage() {
        return Math.min(totalPages - 1, page + 1);
    }

    public List<Integer> getPageNumbers() {
        List<Integer> pages = new ArrayList<>();
        int startPage = (page / PAGE_GROUP_SIZE) * PAGE_GROUP_SIZE;
        int endPage = Math.min(startPage + PAGE_GROUP_SIZE, totalPages);

        for (int i = startPage; i < endPage; i++) {
            pages.add(i);
        }
        return pages;
    }
}
