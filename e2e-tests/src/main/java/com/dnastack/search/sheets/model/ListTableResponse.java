package com.dnastack.search.sheets.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ListTableResponse {

    @JsonProperty("tables")
    private List<Table> tables;

    @JsonProperty("pagination")
    private Pagination pagination;

}
