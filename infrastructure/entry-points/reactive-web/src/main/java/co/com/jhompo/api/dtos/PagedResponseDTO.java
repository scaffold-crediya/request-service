package co.com.jhompo.api.dtos;

import java.util.List;

public class PagedResponseDTO<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    // constructors, getters, setters
}
