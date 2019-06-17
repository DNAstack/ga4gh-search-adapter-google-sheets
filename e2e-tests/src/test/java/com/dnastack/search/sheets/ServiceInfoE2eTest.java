package com.dnastack.search.sheets;

import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

public class ServiceInfoE2eTest extends BaseE2eTest {

    @Test
    public void serviceInfoShouldBeExposed() {
        //@formatter:off
        given()
            .log().method()
            .log().uri()
        .when()
            .get("/ga4gh/search/v1/service-info")
        .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("description", equalTo("search-adapter-google-sheets.service-info.description"));
        //@formatter:on
    }

    /**
     * This test requires the service to respond with HTTP 200 at /, which is a requirement of the Google
     * K8s Ingress Default Health Check.
     */
    @Test
    public void serviceInfoShouldBeExposedAtRootWithoutAuth() {
        //@formatter:off
        given()
            .log().method()
            .log().uri()
        .when()
            .get("/")
        .then()
            .log().ifValidationFails()
            .statusCode(200);
        //@formatter:on
    }

}