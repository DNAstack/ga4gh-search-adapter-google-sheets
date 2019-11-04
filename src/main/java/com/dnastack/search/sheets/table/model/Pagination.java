package com.dnastack.search.sheets.table.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Pagination {


    @JsonProperty("next_page_url")
    private URI nextPageUrl;

    @JsonProperty("previous_page_url")
    private URI previousPageUrl;

}
