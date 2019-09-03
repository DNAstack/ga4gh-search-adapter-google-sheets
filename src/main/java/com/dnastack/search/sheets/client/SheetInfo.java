package com.dnastack.search.sheets.client;

import lombok.Value;

import java.util.List;

@Value
public class SheetInfo {
    private final String id;
    private final String description;
    private final List<String> worksheetTitles;
}
