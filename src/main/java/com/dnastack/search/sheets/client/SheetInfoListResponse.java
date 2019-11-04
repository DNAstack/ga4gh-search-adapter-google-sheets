package com.dnastack.search.sheets.client;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SheetInfoListResponse {

    String nextPageToken;
    List<SheetInfo> sheetInfo;

}
