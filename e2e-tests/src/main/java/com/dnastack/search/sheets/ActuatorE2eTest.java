package com.dnastack.search.sheets;

import io.restassured.RestAssured;
import org.junit.Assume;
import org.junit.Test;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

public class ActuatorE2eTest extends BaseE2eTest {

    @Test
    public void appNameAndVersionShouldBeExposed() {
//        Assume.assumeFalse("Service info isn't set on local dev builds", RestAssured.baseURI.startsWith("http://localhost:"));
        //@formatter:off
        given()
            .log().method()
            .log().uri()
        .when()
            .get("/actuator/info")
        .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("build.name", equalTo("ga4gh-search-adapter-google-sheets"))
            .body("build.version", notNullValue());
        //@formatter:on
    }

    @Test
    public void appHealthStatusShouldBeExposed() {
        given()
            .log().method()
            .log().uri()
            .when()
            .get("/actuator/health")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("status", equalTo("UP"));
    }

    @Test
    public void sensitiveInfoShouldNotBeExposed() {
        //@formatter:off
        Stream.of("auditevents", "beans", "conditions", "configprops", "env", "flyway", "httptrace", "logfile", "loggers",
                "liquibase", "metrics", "mappings", "scheduledtasks", "sessions", "shutdown", "threaddump")
                //@formatter:off
                .forEach(endpoint -> {
                    given()
                        .log().method()
                        .log().uri()
                    .when()
                        .get("/actuator/" + endpoint)
                    .then()
                        .log().ifValidationFails()
                        .statusCode(anyOf(equalTo(401), equalTo(404)));
                    });
        //@formatter:on
    }

}
