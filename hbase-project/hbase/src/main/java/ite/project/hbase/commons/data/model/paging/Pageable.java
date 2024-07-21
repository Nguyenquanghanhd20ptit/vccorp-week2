package ite.project.hbase.commons.data.model.paging;

import lombok.Data;

import java.util.List;

@Data
public class Pageable {
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 5;
    private int page;
    private int pageSize;
    private Integer offset;
    private long total;
    private List<Order> sort;

    public Pageable() {
        this.page = DEFAULT_PAGE;
        this.pageSize = DEFAULT_PAGE_SIZE;
        this.offset = Math.max((this.page - 1) * this.pageSize, 0);
    }

    public Pageable(int page, int pageSize) {
        this.page = page;
        this.pageSize = pageSize;
        this.offset = Math.max((page - 1) * pageSize, 0);
    }
    public Pageable(int page, int pageSize,boolean random) {
        this.page = page;
        this.pageSize = pageSize;
        this.offset = Math.max((page - 1) * pageSize, 0);
    }

    public int getOffset() {
        if (offset == null || offset <= 0) {
            return Math.max((this.page - 1) * this.pageSize, 0);
        }
        return offset;
    }
}
