package com.dnastack.search.sheets.client;

import com.dnastack.search.sheets.dataset.model.Dataset;
import com.dnastack.search.sheets.dataset.model.DatasetInfo;
import com.dnastack.search.sheets.dataset.model.Pagination;
import com.dnastack.search.sheets.dataset.model.Schema;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.sheets.v4.model.CellData;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Service interface for the Google Sheets API which performs Sheets-to-Dataset conversion and data
 * cleanup, such as trimming empty cells at the bottom and right-hand edges.
 */
@Slf4j
public class SheetsService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SheetsClientWrapper sheets;

    public SheetsService(SheetsClientWrapper sheets) {
        this.sheets = sheets;
    }

    public List<DatasetInfo> getDatasets() {
        // TODO request drive listing scope and list all spreadsheets visible to requesting user
        return Collections.emptyList();
    }

    public Dataset getDataset(String spreadsheetId, String worksheetName) throws IOException {
        // assume first row is the heading
        // in future, we oughtta check how many rows are frozen and use that for the headings
        final int headerSize = 1;

        var cellDataRows = sheets.fetchWorksheet(spreadsheetId, worksheetName);
        log.debug("Got worksheet data with {} rows and {} columns in first row",
                cellDataRows.size(), cellDataRows.size() > 0 ? cellDataRows.get(0).size() : "undefined number of");

        var stringRows = convertWorksheetToStringValues(cellDataRows);
        stringRows = trimTrailingEmptyRowsAndColumns(stringRows);

        // assemble schema
        var headerRow = stringRows.get(0);
        var sizeOfLongestRow = sizeOfLongestRow(stringRows);

        int actualHeadingCount = 0;
        int generatedHeadingCount = 0;
        Map<String, Object> schemaJson = new LinkedHashMap<>();
        for (int colNum = 0; colNum < sizeOfLongestRow; colNum++) {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("type", "string");
            props.put("x-ga4gh-position", colNum);

            String colName = colNum < headerRow.size() ? headerRow.get(colNum) : null;
            if (colName == null || colName.trim().length() == 0) {
                colName = "Column " + columnLetter(colNum);
                generatedHeadingCount++;
            } else {
                actualHeadingCount++;
                colName = colName.trim();
            }
            schemaJson.put(colName, props);
        }
        log.debug("Extracted {} actual column headings and {} generated values", actualHeadingCount, generatedHeadingCount);
        var schema = new Schema(URI.create("https://todo.example.com"), "Generated from header row of spreadsheet", schemaJson);

        // assemble row data
        List<Map<String, Object>> objects = new ArrayList<>();
        for (int i = headerSize; i < stringRows.size(); i++) {
            List<String> row = stringRows.get(i);

            Map<String, Object> rowObj = new LinkedHashMap<>();
            Iterator<String> colNameIter = schemaJson.keySet().iterator();
            Iterator<String> cellValueIter = row.iterator();
            while (colNameIter.hasNext() || cellValueIter.hasNext()) {
                String colName = colNameIter.next();
                String cellValue = null;
                if (cellValueIter.hasNext()) {
                    cellValue = cellValueIter.next();
                }
                rowObj.put(colName, cellValue);
            }
            objects.add(rowObj);
        }

        // assemble dataset
        return new Dataset(schema, objects, Pagination.ONLY_PAGE);
    }

    private static List<List<String>> convertWorksheetToStringValues(List<List<CellData>> cellDataRows) {
        return cellDataRows.stream()
                .map(SheetsService::convertRowToStringValues)
                .collect(toList());
    }

    private static List<String> convertRowToStringValues(List<CellData> cellDataRow) {
        return cellDataRow.stream()
                .map(cellData -> cellData == null ? null : cellData.getFormattedValue())
                .collect(toList());
    }

    private static <T> List<List<T>> trimTrailingEmptyRowsAndColumns(List<List<T>> rows) {
        // first trim nulls off end of each row (needed for empty row detection to work properly!)
        rows = rows.stream()
                .map(list -> {
                    int lastNonNull = indexOfLastNonNull(list);
                    log.trace("Last non-null at column {}", lastNonNull);
                    return list.subList(0, lastNonNull + 1);
                })
                .collect(toList());

        // now find last non-empty row among the trimmed rows
        int lastNonEmptyRow = -1;
        for (int row = 0; row < rows.size(); row++) {
            List<T> cells =  rows.get(row);
            if (cells.size() > 0) {
                lastNonEmptyRow = row;
            }
        }

        return rows.subList(0, lastNonEmptyRow + 1);
    }

    private static <T> int indexOfLastNonNull(List<T> row) {
        int lastNonNull = -1;
        for (int i = 0; i < row.size(); i++) {
            T t =  row.get(i);
            if (t != null) {
                lastNonNull = i;
            }
        }
        return lastNonNull;
    }

    private static <T> int sizeOfLongestRow(List<List<T>> rows) {
        return rows.stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);
    }

    private static String columnLetter(int colIndex) {
        StringBuilder colLetter = new StringBuilder();
        do {
            int remainder = colIndex % 26;
            colIndex /= 26;
            colLetter.insert(0, (char) ('A' + remainder));
        } while (colIndex > 26);
        return colLetter.toString();
    }
}
