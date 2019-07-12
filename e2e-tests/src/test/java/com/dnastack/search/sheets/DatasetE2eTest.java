package com.dnastack.search.sheets;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.hasEntry;

public class DatasetE2eTest extends BaseE2eTest {

    static GoogleCredential credential;

    @BeforeClass
    public static void oneTimeSetup() throws Exception {
        credential =
                GoogleCredential
                        .fromStream(new ByteArrayInputStream(requiredEnvBase64("E2E_GOOGLE_CREDENTIALS_JSON_BASE64")))
                        .createScoped(List.of("https://www.googleapis.com/auth/spreadsheets.readonly"));
        credential.refreshToken();
    }

    @Test
    public void getDatasetShouldWorkForFruitsWorksheet() throws Exception {
        //@formatter:off
        given()
            .log().method()
            .log().uri()
            .header("Authorization", "Bearer " + credential.getAccessToken())
        .when()
            .get("/dataset/17LzrJJXKraCCsfCZJ1FqCsMAAoYTcntkpS5gSvOsx1w:Fruits")
        .then()
            .log().all()
            .log().ifValidationFails()
            .statusCode(200)
            .body("schema.description", equalTo("Generated from header row of spreadsheet"))
            .body("schema.properties.keySet()", hasItems("Bananas", "Apples", "Grapes", "Column D", "Durian"))

            .body("objects[0]", hasEntry("Bananas", "4"))
            .body("objects[0]", hasEntry("Apples", "5"))
            .body("objects[0]", hasEntry("Grapes", "7"))
            .body("objects[0]", hasEntry("Column D", "?"))
            .body("objects[0]", hasEntry("Durian", "Not permitted"))

            .body("objects[1]", hasEntry("Bananas", "12"))
            .body("objects[1]", hasEntry("Apples", "7"))
            .body("objects[1]", hasEntry("Grapes", "6"))
            .body("objects[1]", hasEntry("Column D", "llama"))
            .body("objects[1]", hasEntry("Durian", "hello"));
        //@formatter:on
    }

    @Test
    public void getDatasetShouldWorkForVegetablesWorksheet() throws Exception {
        //@formatter:off
        given()
            .log().method()
            .log().uri()
            .header("Authorization", "Bearer " + credential.getAccessToken())
        .when()
            .get("/dataset/17LzrJJXKraCCsfCZJ1FqCsMAAoYTcntkpS5gSvOsx1w:Vegetables")
        .then()
            .log().all()
            .log().ifValidationFails()
            .statusCode(200)
            .body("schema.description", equalTo("Generated from header row of spreadsheet"))
            .body("schema.properties.keySet()", hasItems("Broccoli", "Kale", "Carrots", "Potatoes"))

            .body("objects[0]", hasEntry("Broccoli", "7"))
            .body("objects[0]", hasEntry("Kale", "8"))
            .body("objects[0]", hasEntry("Carrots", "9"))
            .body("objects[0]", hasEntry("Potatoes", "6"))

            .body("objects[1]", hasEntry("Broccoli", "9"))
            .body("objects[1]", hasEntry("Kale", "8"))
            .body("objects[1]", hasEntry("Carrots", "7"))
            .body("objects[1]", hasEntry("Potatoes", "5"))

            .body("objects[2]", hasEntry("Broccoli", "123"))
            .body("objects[2]", hasEntry("Kale", "456"))
            .body("objects[2]", hasEntry("Carrots", "Jennifer"))
            .body("objects[2]", hasEntry("Potatoes", "12"));
        //@formatter:on
    }
}