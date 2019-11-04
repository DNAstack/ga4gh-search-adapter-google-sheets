package com.dnastack.search.sheets;

import static io.restassured.RestAssured.given;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.dnastack.search.sheets.model.ListTableResponse;
import com.dnastack.search.sheets.model.Table;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.Test;

public class TableE2eTest extends BaseE2eTest {

    static GoogleCredentials credential;

    @BeforeClass
    public static void oneTimeSetup() throws Exception {
        try {
            credential =
                ServiceAccountCredentials
                    .fromStream(new ByteArrayInputStream(requiredEnv("E2E_GOOGLE_CREDENTIALS_JSON_BASE64").getBytes()))
                    .createScoped(List
                        .of("https://www.googleapis.com/auth/spreadsheets.readonly", "https://www.googleapis.com/auth/drive.metadata.readonly"));
            credential.refresh();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void getTablesShouldListAllAvailableWorksheets() throws Exception {
        //@formatter:off
        ListTableResponse response = given()
            .log().method()
            .log().uri()
            .header("Authorization", "Bearer " + credential.getAccessToken().getTokenValue())
        .when()
            .get("/api/tables")
        .then()
            .log().all()
            .log().ifValidationFails()
            .statusCode(200)
            .extract().body().as(ListTableResponse.class);
        //@formatter:on

        assertThat(response.getTables(), notNullValue());

        Set<String> tableIds = response.getTables().stream().map(Table::getName).collect(toSet());
        assertThat(tableIds, containsInAnyOrder(
            "17LzrJJXKraCCsfCZJ1FqCsMAAoYTcntkpS5gSvOsx1w:Fruits",
            "17LzrJJXKraCCsfCZJ1FqCsMAAoYTcntkpS5gSvOsx1w:Vegetables"));

        Set<String> datasetDescriptions = response.getTables().stream().map(Table::getDescription)
            .collect(toSet());
        assertThat(datasetDescriptions, containsInAnyOrder(
            "Fruits and Vegetables - Fruits",
            "Fruits and Vegetables - Vegetables"));

        for (Table ds : response.getTables()) {
            assertThat(ds.getDataModel().get("$ref"), is("table/" + ds.getName() + "/data-model"));
        }
    }

    @Test
    public void getTableInfoShouldWorkForFruitSheet() {
        //@formatter:off
        given()
            .log().method()
            .log().uri()
            .header("Authorization", "Bearer " + credential.getAccessToken().getTokenValue())
        .when()
            .get("api/table/17LzrJJXKraCCsfCZJ1FqCsMAAoYTcntkpS5gSvOsx1w:Fruits/info")
        .then()
            .log().all()
            .log().ifValidationFails()
            .statusCode(200)
            .body("name",equalTo("17LzrJJXKraCCsfCZJ1FqCsMAAoYTcntkpS5gSvOsx1w:Fruits"))
            .body("description",equalTo("Fruits and Vegetables - Fruits"))
            .body("data_model",hasEntry("$ref","table/17LzrJJXKraCCsfCZJ1FqCsMAAoYTcntkpS5gSvOsx1w:Fruits/data-model"));
        //@formatter:on
    }

    @Test
    public void getTableModelShouldWorkForFruitSheet() {
        //@formatter:off
        given()
            .log().method()
            .log().uri()
            .header("Authorization", "Bearer " + credential.getAccessToken().getTokenValue())
        .when()
            .get("api/table/17LzrJJXKraCCsfCZJ1FqCsMAAoYTcntkpS5gSvOsx1w:Fruits/data-model")
        .then()
            .log().all()
            .log().ifValidationFails()
            .statusCode(200)
            .body("description", equalTo("Generated from header row of spreadsheet"))
            .body("properties.keySet()", hasItems("Bananas", "Apples", "Grapes", "Column D", "Durian"));
    }

        //@formatter:on
        @Test
        public void getTableDataShouldWorkForFruitSheet() {

            //@formatter:off
        given()
            .log().method()
            .log().uri()
            .header("Authorization", "Bearer " + credential.getAccessToken().getTokenValue())
        .when()
            .get("api/table/17LzrJJXKraCCsfCZJ1FqCsMAAoYTcntkpS5gSvOsx1w:Fruits/data")
        .then()
            .log().all()
            .log().ifValidationFails()
            .statusCode(200)
            .body("data[0]", hasEntry("Bananas", "4"))
            .body("data[0]", hasEntry("Apples", "5"))
            .body("data[0]", hasEntry("Grapes", "7"))
            .body("data[0]", hasEntry("Column D", "?"))
            .body("data[0]", hasEntry("Durian", "Not permitted"))

            .body("data[1]", hasEntry("Bananas", "12"))
            .body("data[1]", hasEntry("Apples", "7"))
            .body("data[1]", hasEntry("Grapes", "6"))
            .body("data[1]", hasEntry("Column D", "llama"))
            .body("data[1]", hasEntry("Durian", "hello"));
    }
}