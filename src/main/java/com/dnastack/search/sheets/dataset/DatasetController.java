package com.dnastack.search.sheets.dataset;

import com.dnastack.search.sheets.client.SheetsServiceFactory;
import com.dnastack.search.sheets.shared.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.ga4gh.dataset.model.Dataset;
import org.ga4gh.dataset.model.ListDatasetsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
public class DatasetController {

    @Autowired
    private SheetsServiceFactory sheetsServiceFactory;

    @GetMapping("/datasets")
    public ListDatasetsResponse list(@RequestHeader String authorization) {

        var datasets = sheetsServiceFactory.create(extractBearerToken(authorization)).getDatasets();
        return new ListDatasetsResponse(datasets);
    }

    @GetMapping("/dataset/{spreadsheetId}:{sheetTitle}")
    public Dataset get(@RequestHeader String authorization, @PathVariable String spreadsheetId, @PathVariable String sheetTitle) throws IOException {
        log.info("Fetching {} - {}", spreadsheetId, sheetTitle);
        var sheetsClient = sheetsServiceFactory.create(extractBearerToken(authorization));
        return sheetsClient.getDataset(spreadsheetId, sheetTitle);
    }


    private String extractBearerToken(String authorization) {
        String[] parts = authorization.split(" ");
        if (parts.length != 2) {
            throw new BadRequestException("Incorrect authorization type (expected a bearer token)");
        }
        if (!parts[0].equalsIgnoreCase("bearer")) {
            throw new BadRequestException("Incorrect authorization type (expected \"Bearer\", got \"" + parts[0] + "\")");
        }
        return parts[1];
    }

}