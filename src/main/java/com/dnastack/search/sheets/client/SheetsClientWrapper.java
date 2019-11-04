package com.dnastack.search.sheets.client;

import static java.util.stream.Collectors.toList;

import com.dnastack.search.sheets.shared.NotFoundException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.GridData;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

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

    SheetInfoListResponse fetchSheetList(Integer pageSize) throws IOException {
        FileList fileList = driveService.files().list()
            .setQ("mimeType='application/vnd.google-apps.spreadsheet'")
            .setPageSize(pageSize)
            .execute();

        return extractSheetInfoFromFileList(fileList);
    }

    SheetInfoListResponse fetchNextPageSheetList(String nextPageToken) throws IOException {
        FileList fileList = driveService.files()
            .list().setPageToken(nextPageToken)
            .setQ("mimeType='application/vnd.google-apps.spreadsheet'")
            .execute();
        return extractSheetInfoFromFileList(fileList);
    }

    private SheetInfoListResponse extractSheetInfoFromFileList(FileList fileList) {
        List<SheetInfo> sheetInfos = fileList.getFiles().stream()
            .map(file -> new SheetInfo(file.getId(), file.getName(), fetchWorksheetNames(file.getId())))
            .collect(toList());
        return new SheetInfoListResponse(fileList.getNextPageToken(), sheetInfos);
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
        List<GridData> dataRanges = spreadsheet.getSheets().stream()
            .filter(sheet -> sheet.getProperties().getTitle().equalsIgnoreCase(worksheetTitle))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Worksheet not found: " + worksheetTitle))
            .getData();

        if (dataRanges.size() != 1) {
            log.warn("Unexpected Sheets API behaviour: got {} data ranges from sheet {}", dataRanges
                .size(), worksheetTitle);
        }

        return dataRanges.get(0).getRowData().stream()
            .map(RowData::getValues)
            .map(values -> values == null ? List.<CellData>of() : values)
            .collect(toList());
    }

    SheetInfo fetchWorksheetInfo(String spreadsheetId) throws GoogleJsonResponseException, NotFoundException {
        Spreadsheet spreadsheet;
        try {
            spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).setIncludeGridData(true).execute();
        } catch (GoogleJsonResponseException e) {
            throw e;
        } catch (IOException e) {
            throw new NotFoundException("Couldn't fetch spreadsheet " + spreadsheetId + ": " + e.getMessage());
        }

        List<String> sheetNames = spreadsheet.getSheets().stream()
            .map(sheet -> sheet.getProperties().getTitle())
            .collect(Collectors.toList());
        return new SheetInfo(spreadsheetId, spreadsheet.getProperties().getTitle(), sheetNames);
    }
}
