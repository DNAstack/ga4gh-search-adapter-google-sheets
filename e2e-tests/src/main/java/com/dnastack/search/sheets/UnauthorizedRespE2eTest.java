package com.dnastack.search.sheets;

import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Ensure that all secured endpoints require a valid bearer token.
 */
public class UnauthorizedRespE2eTest extends BaseE2eTest {

    private final String sheetIDEndpoint = "/table/17LzrJJXKraCCsfCZJ1FqCsMAAoYTcntkpS5gSvOsx1w:Fruits";

    @Test
    public void getShouldGive400WhenAuthHeaderIsMissing() {
        getWithoutAuthHeaderAndCheckFor400("/tables");
        getWithoutAuthHeaderAndCheckFor400(sheetIDEndpoint + "/info");
        getWithoutAuthHeaderAndCheckFor400(sheetIDEndpoint + "/data-model");
        getWithoutAuthHeaderAndCheckFor400(sheetIDEndpoint + "/data");
    }

    @Test
    public void getShouldGive401WhenInvalidTokenUsed() {
        getWithInvalidTokenAndCheckFor401("/tables");
        getWithInvalidTokenAndCheckFor401(sheetIDEndpoint + "/info");
        getWithInvalidTokenAndCheckFor401(sheetIDEndpoint + "/data-model");
        getWithInvalidTokenAndCheckFor401(sheetIDEndpoint + "/data");
    }

    private void getWithoutAuthHeaderAndCheckFor400(String endpoint) {
        given()
            .log().uri()
        .when()
            .get(endpoint)
        .then()
            .statusCode(400)
            .body("message", equalTo("Missing request header 'authorization' for method parameter of type String"));
    }

    private void getWithInvalidTokenAndCheckFor401(String endpoint) {
        given()
            .log().uri()
            .header("Authorization", "Bearer " + "A_malformed_bearer_token")
        .when()
            .get(endpoint)
        .then()
            .statusCode(401)
            // Attempt at future-proofing since the header is from Google. Avoid exact match of full string.
            .header("WWW-Authenticate", containsStringIgnoringCase("Bearer"))
            .header("WWW-Authenticate", containsStringIgnoringCase("invalid_token"));
    }
}
