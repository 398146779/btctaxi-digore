package com.btctaxi.gate.model;

import genesis.common.DataMap;

import java.util.List;

public class Pageable {

    public static final String PAGE = "page";
    public static final String SIZE = "size";
    public static final String TOTAL = "total";
    public static final String VALUES = "values";

    private Integer page;
    private Integer size;
    private Integer total;
    private List<DataMap> values;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<DataMap> getValues() {
        return values;
    }

    public void setValues(List<DataMap> values) {
        this.values = values;
    }
}
