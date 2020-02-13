package com.dnastack.search.sheets;

import io.restassured.response.Response;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringStartsWith.startsWith;

public class OAuthE2eTest extends BaseE2eTest {

    /**
     * We can't test a real login because Google has thorough anti-bot measures in their sign-in process, and there
     * is no sandbox environment we can use for testing. However, Google performs a number of pre-flight checks before
     * displaying the login page, so there is high value in at least visiting the login page and ensuring it's not
     * showing an error message.
     */
    @Test
    public void oauthTokenEndpointShouldRedirectToValidGoogleSignInPage() throws Exception {

        //@formatter:off
        given()
            .log().method()
            .log().uri()
            .redirects().follow(true)
        .get("/oauth/token")
            .then()
            .log().all()
            .assertThat()
            .statusCode(200)
            .contentType("text/html");
        //@formatter:on

    }
}