package com.dnastack.search.sheets;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static io.restassured.RestAssured.given;

public class UnauthorizedRespE2eTest extends BaseE2eTest {

    // TODO: Probably don't need to test against the actual sheet

    /**
     * Ensure that all secured endpoints require a valid bearer token.
     */
    @Test
    public void getWithMalformedTokenShouldGive400() {
        getEndpointWithoutTokenAndEnsure400Response("/tables");
        getEndpointWithoutTokenAndEnsure400Response("/table/17LzrJJXKraCCsfCZJ1FqCsMAAoYTcntkpS5gSvOsx1w:Fruits/info");
        getEndpointWithoutTokenAndEnsure400Response("/table/17LzrJJXKraCCsfCZJ1FqCsMAAoYTcntkpS5gSvOsx1w:Fruits/data-model");
        getEndpointWithoutTokenAndEnsure400Response("/table/17LzrJJXKraCCsfCZJ1FqCsMAAoYTcntkpS5gSvOsx1w:Fruits/data");
    }

    @Test
    public void getWithoutTokenShouldGive401() {
        getEndpointWithMalformedTokenAndEnsure401Response("/tables");
        getEndpointWithMalformedTokenAndEnsure401Response("/table/17LzrJJXKraCCsfCZJ1FqCsMAAoYTcntkpS5gSvOsx1w:Fruits/info");
        getEndpointWithMalformedTokenAndEnsure401Response("/table/17LzrJJXKraCCsfCZJ1FqCsMAAoYTcntkpS5gSvOsx1w:Fruits/data-model");
        getEndpointWithMalformedTokenAndEnsure401Response("/table/17LzrJJXKraCCsfCZJ1FqCsMAAoYTcntkpS5gSvOsx1w:Fruits/data");
    }

    private void getEndpointWithoutTokenAndEnsure400Response(String endpoint) {
        given()
            .log().uri()
            .when()
        .get(endpoint)
            .then()
            .statusCode(400)
            .body("message", CoreMatchers.equalTo("Missing request header 'authorization' for method parameter of type String"));
    }

    private void getEndpointWithMalformedTokenAndEnsure401Response(String endpoint) {
        given()
            .log().uri()
            .header("Authorization", "Bearer " + "Malformed_bearer")
            .when()
            .get(endpoint)
            .then()
            .statusCode(401)
            .header("WWW-Authenticate", "[Bearer realm=\"https://accounts.google.com/\", error=invalid_token]");
    }
}
