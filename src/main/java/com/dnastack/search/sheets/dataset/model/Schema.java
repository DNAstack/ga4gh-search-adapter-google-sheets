package com.dnastack.search.sheets.dataset.model;

import lombok.Value;

import java.net.URI;
import java.util.Map;

@Value
public class Schema {
    private URI id;
    private String description;
    private Map<String, Object> properties;
}
