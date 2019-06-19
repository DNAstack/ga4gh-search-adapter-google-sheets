package com.dnastack.search.sheets.dataset.model;

import lombok.Value;

import java.util.List;

@Value
public class ListDatasetsResponse {
    private List<DatasetInfo> datasets;
}
