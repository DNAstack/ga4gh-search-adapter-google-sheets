package com.dnastack.search.sheets.dataset.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.net.URI;

/** Basic information about a dataset. Appears in listings of datasets. */
@Value
public class DatasetInfo {

    /**
     * Unique identifier of the dataset. Expected not to change; used in HTTP paths. Also a relative
     * location
     */
    private String id;

    private String description;

    @JsonProperty("schema-id")
    private String schemaId;

    @JsonProperty("schema-location")
    private URI schemaLocation;
}