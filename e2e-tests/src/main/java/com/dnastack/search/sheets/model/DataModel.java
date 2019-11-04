package com.dnastack.search.sheets.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataModel {

    public static final String DEFAULT_SCHEMA_ID = "GENERATED_SCHEMA";
    public static final String DEFAULT_DESCRIPTION = "Automatically generated schema";
    public static final String DEFAULT_JSON_SCHEMA_REFERENCE = "http://json-schema.org/draft-07/schema#";

    @JsonProperty("id")
    private String id;

    @JsonProperty("description")
    private String description;

    @JsonProperty("$schema")
    private String schema;

    @JsonProperty("properties")
    private Map<String, Object> properties;


    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("description", description);
        map.put("$schema", schema);
        map.put("properties", properties);
        return map;
    }

    public static DataModel fromProperties(Map<String, Object> properties) {
        DataModel model = new DataModel();
        model.setId(DEFAULT_SCHEMA_ID);
        model.setDescription(DEFAULT_DESCRIPTION);
        model.setSchema(DEFAULT_JSON_SCHEMA_REFERENCE);
        model.setProperties(properties);

        return model;
    }
}
