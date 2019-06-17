package com.dnastack.search.sheets.client;

import com.google.api.services.sheets.v4.model.CellData;
import lombok.Value;

import java.util.List;

/**
 * A less horrible data structure than the one that comes with the Google Sheets API client. Easier to work with
 * in tests, and in general.
 */
@Value
class SheetData {
    private List<List<CellData>> data;
}
