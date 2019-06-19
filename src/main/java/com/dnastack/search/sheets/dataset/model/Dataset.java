package com.dnastack.search.sheets.dataset.model;

import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
public class Dataset {
    private Schema schema;
    private List<Map<String, Object>> objects;
    private Pagination pagination;
}
