package com.dnastack.search.sheets.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.sheets.v4.model.CellData;
import org.assertj.core.api.Assertions;
import org.ga4gh.dataset.model.Dataset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SheetsClientTest {

    @Mock
    SheetsApiWrapper sheetsBackend;

    @InjectMocks
    SheetsClient sheetsClient;

    @Test
    public void getDataset_should_trimTrailingNullsFromHeader_when_everyValueIsUnderAHeading() throws Exception {
        var rawSheetsData = List.of(
                List.of(cd("title A1"), cd("title B1"), cd(null), cd(null), cd(null), cd(null)),
                List.of(cd("value A2"), cd("value B2")),
                List.of(cd("value A3"), cd("value B3"))
        );
        when(sheetsBackend.fetchWorksheet("ss1", "ws1")).thenReturn(rawSheetsData);

        Dataset dataset = sheetsClient.getDataset("ss1", "ws1");
        Map<String, Object> schema = extractSchema(dataset);
        Assertions.assertThat(schema).containsOnlyKeys("title A1", "title B1");
    }

    @Test
    public void getDataset_should_trimTrailingEmptyRows() throws Exception {
        List<List<CellData>> rawSheetsData = List.of(
                List.of(cd("title A1"), cd("title B1")),
                List.of(cd("value A2"), cd("value B2")),
                List.of(cd("value A3"), cd("value B3")),
                List.of(),
                List.of(),
                List.of()
        );
        when(sheetsBackend.fetchWorksheet("ss1", "ws1")).thenReturn(rawSheetsData);

        Dataset dataset = sheetsClient.getDataset("ss1", "ws1");
        List<Map<String, Object>> objects = extractRows(dataset);
        Assertions.assertThat(objects).containsExactly(
                Map.of(
                        "title A1", "value A2",
                        "title B1", "value B2"),
                Map.of(
                        "title A1", "value A3",
                        "title B1", "value B3")
        );
    }

    @Test
    public void getDataset_should_generateTrailingColumnNames_when_someRowsAreLongerThanHeaderRow() throws Exception {
        var rawSheetsData = List.of(
                List.of(cd("title A1"), cd("title B1")),
                List.of(cd("value A2"), cd("value B2")),
                List.of(cd("value A3"), cd("value B3"), cd("value C3"))
        );
        when(sheetsBackend.fetchWorksheet("ss1", "ws1")).thenReturn(rawSheetsData);

        Dataset dataset = sheetsClient.getDataset("ss1", "ws1");
        Map<String, Object> schema = extractSchema(dataset);
        Assertions.assertThat(schema).containsOnlyKeys("title A1", "title B1", "Column C");
    }

    private static Map<String, Object> extractSchema(Dataset dataset) {
        JsonNode schemaJson = dataset.getSchema().getSchemaJson();
        return new ObjectMapper().convertValue(schemaJson, new TypeReference<Map<String, Object>>() {
        });
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> extractRows(Dataset dataset) {
        return dataset.getObjects().stream()
                .map(row -> (Map<String, Object>) row)
                .collect(toList());
    }

    private static CellData cd(Object value) {
        return new CellData().setFormattedValue(value == null ? null : String.valueOf(value));
    }
}