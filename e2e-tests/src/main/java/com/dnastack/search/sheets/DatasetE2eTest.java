package com.dnastack.search.sheets;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.hasEntry;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import java.io.ByteArrayInputStream;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

public class DatasetE2eTest extends BaseE2eTest {

    static GoogleCredentials credential;

    @BeforeClass
    public static void oneTimeSetup() throws Exception {
        credential =
            ServiceAccountCredentials
                .fromStream(new ByteArrayInputStream(requiredEnvBase64("E2E_GOOGLE_CREDENTIALS_JSON_BASE64")))
                .createScoped(List.of("https://www.googleapis.com/auth/spreadsheets.readonly"));
        credential.refresh();
    }


}