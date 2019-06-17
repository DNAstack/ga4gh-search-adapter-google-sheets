package com.dnastack.search.sheets.client;

import com.dnastack.search.sheets.shared.NotFoundException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.GridData;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Easy-to-mock interface that encapsulates the horrors of the Sheets API.
 */
@Slf4j
class SheetsApiWrapper {

    private final Sheets service;

    public SheetsApiWrapper(Sheets service) {
        this.service = service;
    }

    List<List<CellData>> fetchWorksheet(String spreadsheetId, String worksheetTitle) throws GoogleJsonResponseException, NotFoundException {

        Spreadsheet spreadsheet;
        try {
            spreadsheet = service.spreadsheets().get(spreadsheetId).setIncludeGridData(true).execute();
        } catch (GoogleJsonResponseException e) {
            throw e;
        } catch (IOException e) {
            throw new NotFoundException("Couldn't fetch spreadsheet " + spreadsheetId + ": " + e.getMessage());
        }

//        ValueRange response = service.spreadsheets().values()
//                .get(id, range)
//                .execute();
//        List<List<Object>> values = response.getValues();
//        if (values == null || values.isEmpty()) {
//            throw new NotFoundException(id);
//        }

        List<GridData> dataRanges = spreadsheet.getSheets().stream()
                .filter(sheet -> sheet.getProperties().getTitle().equalsIgnoreCase(worksheetTitle))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Worksheet not found: " + worksheetTitle))
                .getData();

        if (dataRanges.size() != 1) {
            log.warn("Unexpected Sheets API behaviour: got {} data ranges from sheet {}", dataRanges.size(), worksheetTitle);
        }

        return dataRanges.get(0).getRowData().stream()
                .map(RowData::getValues)
                .collect(toList());
    }
}
