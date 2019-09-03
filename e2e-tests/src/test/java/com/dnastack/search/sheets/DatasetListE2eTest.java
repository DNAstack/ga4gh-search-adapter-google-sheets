package com.dnastack.search.sheets;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import lombok.Data;
import org.hamcrest.MatcherAssert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DatasetListE2eTest extends BaseE2eTest {

    static GoogleCredential credential;

    @BeforeClass
    public static void oneTimeSetup() throws Exception {
        credential =
                GoogleCredential
                        .fromStream(new ByteArrayInputStream(requiredEnvBase64("E2E_GOOGLE_CREDENTIALS_JSON_BASE64")))
                        .createScoped(List.of("https://www.googleapis.com/auth/spreadsheets.readonly", "https://www.googleapis.com/auth/drive.metadata.readonly"));
        credential.refreshToken();
    }

    @Test
    public void getDatasetsShouldListAllAvailableWorksheets() throws Exception {
        //@formatter:off
        DatasetListResponse response = given()
            .log().method()
            .log().uri()
            .header("Authorization", "Bearer " + credential.getAccessToken())
        .when()
            .get("/datasets")
        .then()
            .log().all()
            .log().ifValidationFails()
            .statusCode(200)
            .extract().body().as(DatasetListResponse.class);
        //@formatter:on

        assertThat(response.getDatasets(), notNullValue());

        Set<String> datasetIds = response.getDatasets().stream().map(DatasetInfo::getId).collect(toSet());
        assertThat(datasetIds, containsInAnyOrder(
                "17LzrJJXKraCCsfCZJ1FqCsMAAoYTcntkpS5gSvOsx1w:Fruits",
                "17LzrJJXKraCCsfCZJ1FqCsMAAoYTcntkpS5gSvOsx1w:Vegetables"));

        Set<String> datasetDescriptions = response.getDatasets().stream().map(DatasetInfo::getDescription).collect(toSet());
        assertThat(datasetDescriptions, containsInAnyOrder(
                "Fruits and Vegetables - Fruits",
                "Fruits and Vegetables - Vegetables"));

        for (DatasetInfo ds : response.getDatasets()) {
            assertThat(ds.getSchema().getRef(), is("dataset/" + ds.getId() + "#schema"));
        }
    }

    @Data
    private static class DatasetListResponse {
        private List<DatasetInfo> datasets;
    }

    @Data
    private static class DatasetInfo {
        private String id;
        private SchemaRef schema;
        private String description;
    }

    @Data
    private static class SchemaRef {
        @JsonProperty("$ref")
        private String ref;
    }
}