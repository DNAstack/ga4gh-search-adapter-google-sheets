package com.dnastack.search.sheets.client;

import com.dnastack.search.sheets.shared.NotFoundException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Easy-to-mock interface that encapsulates the less-mockable Sheets API.
 */
@Slf4j
class SheetsClientWrapper {

    private final Drive driveService;
    private final Sheets sheetsService;

    public SheetsClientWrapper(Drive driveService, Sheets sheetsService) {
        this.driveService = driveService;
        this.sheetsService = sheetsService;
    }

    List<SheetInfo> fetchSheetList() throws IOException {
        // drive.files.list({
        //    q: "mimeType='application/vnd.google-apps.spreadsheet'",
        //    fields: 'nextPageToken, files(id, name)'
        //}
        FileList fileList = driveService.files().list()
                .setQ("mimeType='application/vnd.google-apps.spreadsheet'")
                .execute();

        return fileList.getFiles().stream()
                .map(file -> new SheetInfo(file.getId(), file.getName(), fetchWorksheetNames(file.getId())))
                .collect(toList());
    }

    private List<String> fetchWorksheetNames(String spreadsheetId) {
        Spreadsheet spreadsheet = null;
        try {
            spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).setIncludeGridData(false).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return spreadsheet.getSheets().stream().map(sheet -> sheet.getProperties().getTitle()).collect(toList());
    }

    List<List<CellData>> fetchWorksheet(String spreadsheetId, String worksheetTitle) throws GoogleJsonResponseException, NotFoundException {

        Spreadsheet spreadsheet;
        try {
            spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).setIncludeGridData(true).execute();
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
                .map(values -> values == null ? List.<CellData>of() : values)
                .collect(toList());
    }
}
