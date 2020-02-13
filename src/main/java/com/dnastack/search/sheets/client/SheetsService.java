package com.dnastack.search.sheets.client;

import static java.util.stream.Collectors.toList;

import com.dnastack.search.sheets.shared.BadRequestException;
import com.dnastack.search.sheets.table.model.DataModel;
import com.dnastack.search.sheets.table.model.ListTableResponse;
import com.dnastack.search.sheets.table.model.Pagination;
import com.dnastack.search.sheets.table.model.Table;
import com.dnastack.search.sheets.table.model.TableData;
import com.google.api.services.sheets.v4.model.CellData;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Service interface for the Google Sheets API which performs Sheets-to-Dataset conversion and data cleanup, such as
 * trimming empty cells at the bottom and right-hand edges.
 */
@Slf4j
public class SheetsService {

    private final SheetsClientWrapper sheets;
    private final static int DEFAULT_PAGE_SIZE = 10;
    private final static int DEFAULT_HEADER_ROW = 1;

    public SheetsService(SheetsClientWrapper sheets) {
        this.sheets = sheets;
    }


    public ListTableResponse getTables(Integer pageSize, String nextPageToken) throws IOException {
        SheetInfoListResponse listResponse;
        if (nextPageToken != null) {
            listResponse = sheets.fetchNextPageSheetList(nextPageToken);
        } else {
            int finalPageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
            listResponse = sheets.fetchSheetList(finalPageSize);
        }

        List<Table> tables = listResponse.getSheetInfo().stream()
            .flatMap(sheetInfo ->
                sheetInfo.getWorksheetTitles().stream().map(worksheetTitle ->
                    new Table(
                        getIdString(sheetInfo.getId(), worksheetTitle),
                        sheetInfo.getDescription() + " - " + worksheetTitle,
                        Map.of("$ref", "table/" + getIdString(sheetInfo.getId(), worksheetTitle) + "/data-model"))
                )
            )
            .collect(toList());

        URI nextPage = null;
        if (listResponse.getNextPageToken() != null) {
            nextPage = UriComponentsBuilder.fromPath("tables")
                .queryParam("nextPageToken", listResponse.getNextPageToken())
                .build().toUri();
        }

        return new ListTableResponse(tables, new Pagination(nextPage, null));
    }


    private String getIdString(String sheetId, String worksheetTitle) {
        return sheetId + ":" + URLEncoder.encode(worksheetTitle, Charset.defaultCharset());
    }


    private String decodeWorksheetName(String worksheetName) {
        return URLDecoder.decode(worksheetName, StandardCharsets.UTF_8);
    }

    public DataModel getDataModel(String sheetId, String worksheetName, Integer headerRow) throws IOException {
        worksheetName = decodeWorksheetName(worksheetName);
        List<List<String>> sheetData = getSheetData(sheetId, worksheetName);
        if (headerRow == null) {
            headerRow = DEFAULT_HEADER_ROW;
        }

        if (sheetData.size() < headerRow) {
            throw new BadRequestException("Could not extract header row, not enough rows");
        }

        List<String> header = sheetData.get(headerRow - 1);
        int sizeOfLongestRow = sizeOfLongestRow(sheetData);
        return getDataModel(header, sizeOfLongestRow);
    }


    /**
     * Extract thed data model from a header row. For now this strictly returns a data model consisting of strings.
     */
    private DataModel getDataModel(List<String> headerRow, int sizeOfLongestRow) {

        int actualHeadingCount = 0;
        int generatedHeadingCount = 0;
        Map<String, Object> properties = new LinkedHashMap<>();
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
            properties.put(colName, props);
        }

        log.debug("Extracted {} actual column headings and {} generated values", actualHeadingCount, generatedHeadingCount);
        return DataModel.fromProperties(properties);

    }

    public Table getTable(String spreadsheetId, String worksheetName) throws IOException {
        worksheetName = decodeWorksheetName(worksheetName);
        SheetInfo sheetInfo = sheets.fetchWorksheetInfo(spreadsheetId);
        return new Table(
            spreadsheetId + ":" + worksheetName,
            sheetInfo.getDescription() + " - " + worksheetName,
            Map.of("$ref", "data-model"));
    }

    public TableData getTableData(String spreadsheetId, String worksheetName, Integer headerRow) throws IOException {
        // assume first row is the heading
        // in future, we oughtta check how many rows are frozen and use that for the headings
        if (headerRow == null) {
            headerRow = DEFAULT_HEADER_ROW;
        }
        worksheetName = decodeWorksheetName(worksheetName);
        List<List<String>> sheetData = getSheetData(spreadsheetId, worksheetName);

        if (sheetData.size() < headerRow) {
            throw new BadRequestException("Could not extract header row, not enough rows");
        }

        List<String> header = sheetData.get(headerRow - 1);
        int sizeOfLongestRow = sizeOfLongestRow(sheetData);
        DataModel dataModel = getDataModel(header, sizeOfLongestRow);

        TableData table = new TableData();
        table.setDataModel(dataModel.toMap());
        List<Map<String, Object>> data = new ArrayList<>();

        if (sheetData.size() > 1) {
            List<List<String>> dataRows = sheetData.subList(headerRow, sheetData.size());
            for (int i = 0; i < dataRows.size(); i++) {
                List<String> row = dataRows.get(i);
                Map<String, Object> rowObj = new LinkedHashMap<>();
                Iterator<String> colNameIter = dataModel.getProperties().keySet().iterator();
                Iterator<String> cellValueIter = row.iterator();
                while (colNameIter.hasNext() || cellValueIter.hasNext()) {
                    String colName = colNameIter.next();
                    String cellValue = null;
                    if (cellValueIter.hasNext()) {
                        cellValue = cellValueIter.next();
                    }
                    rowObj.put(colName, cellValue);
                }
                data.add(rowObj);
            }

        }
        table.setData(data);
        return table;
    }

    private List<List<String>> getSheetData(String sheetId, String worksheetName) throws IOException {

        var cellDataRows = sheets.fetchWorksheet(sheetId, worksheetName);
        log.debug("Got worksheet data with {} rows and {} columns in first row",
            cellDataRows.size(), cellDataRows.size() > 0 ? cellDataRows.get(0).size() : "undefined number of");

        var stringRows = convertWorksheetToStringValues(cellDataRows);
        return trimTrailingEmptyRowsAndColumns(stringRows);
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
            List<T> cells = rows.get(row);
            if (cells.size() > 0) {
                lastNonEmptyRow = row;
            }
        }

        return rows.subList(0, lastNonEmptyRow + 1);
    }

    private static <T> int indexOfLastNonNull(List<T> row) {
        int lastNonNull = -1;
        for (int i = 0; i < row.size(); i++) {
            T t = row.get(i);
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
