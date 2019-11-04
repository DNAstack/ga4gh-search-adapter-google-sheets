package com.dnastack.search.sheets.table;


import com.dnastack.search.sheets.client.SheetsServiceFactory;
import com.dnastack.search.sheets.shared.BadRequestException;
import com.dnastack.search.sheets.table.model.DataModel;
import com.dnastack.search.sheets.table.model.ListTableResponse;
import com.dnastack.search.sheets.table.model.Table;
import com.dnastack.search.sheets.table.model.TableData;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TablesController {


    @Autowired
    private SheetsServiceFactory sheetsServiceFactory;

    @RequestMapping(value = "/api/tables", method = RequestMethod.GET)
    public ListTableResponse getTables(@RequestHeader String authorization, @RequestParam(value = "pageSize", required = false) Integer pageSize, @RequestParam(value = "nextPageToken", required = false) String nextPageToken) throws IOException {
        return sheetsServiceFactory.create(extractBearerToken(authorization)).getTables(pageSize, nextPageToken);
    }


    @RequestMapping(value = "/api/table/{sheetId}:{sheetTitle}/info", method = RequestMethod.GET)
    public Table getTableInfo(@RequestHeader String authorization, @PathVariable("sheetId") String sheetId, @PathVariable("sheetTitle") String sheetTitle) throws IOException {
        return sheetsServiceFactory.create(extractBearerToken(authorization)).getTable(sheetId, sheetTitle);
    }


    @RequestMapping(value = "/api/table/{sheetId}:{sheetTitle}/data", method = RequestMethod.GET)
    public TableData getTableData(@RequestHeader String authorization, @PathVariable("sheetId") String sheetId, @PathVariable("sheetTitle") String sheetTitle, @RequestParam(value = "headerRow", required = false) Integer headerRow) throws IOException {
        return sheetsServiceFactory.create(extractBearerToken(authorization))
            .getTableData(sheetId, sheetTitle, headerRow);
    }

    @RequestMapping(value = "/api/table/{sheetId}:{sheetTitle}/data-model", method = RequestMethod.GET)
    public DataModel getDataModel(@RequestHeader String authorization, @PathVariable("sheetId") String sheetId, @PathVariable("sheetTitle") String sheetTitle, @RequestParam(value = "headerRow", required = false) Integer headerRow) throws IOException {
        return sheetsServiceFactory.create(extractBearerToken(authorization))
            .getDataModel(sheetId, sheetTitle, headerRow);
    }

    private String extractBearerToken(String authorization) {
        String[] parts = authorization.split(" ");
        if (parts.length != 2) {
            throw new BadRequestException("Incorrect authorization type (expected a bearer token)");
        }
        if (!parts[0].equalsIgnoreCase("bearer")) {
            throw new BadRequestException(
                "Incorrect authorization type (expected \"Bearer\", got \"" + parts[0] + "\")");
        }
        return parts[1];
    }
}
