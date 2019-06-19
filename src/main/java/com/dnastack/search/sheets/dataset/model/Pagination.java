package com.dnastack.search.sheets.dataset.model;

import lombok.Value;

import java.net.URI;

@Value
public class Pagination {
    public static final Pagination ONLY_PAGE = new Pagination(null, null);

    private URI nextPageUrl;
    private URI previousPageUrl;
}
