package com.dnastack.search.sheets;

import io.restassured.RestAssured;
import org.junit.Before;

import java.util.Base64;

import static org.junit.Assert.fail;

public class BaseE2eTest {

    @Before
    public void setUp() throws Exception {
        RestAssured.baseURI = requiredEnv("E2E_BASE_URI");
    }

    protected static String requiredEnv(String name) {
        String val = System.getenv(name);
        if (val == null) {
            fail("Environnment variable `" + name + "` is required");
        }
        return val;
    }

    protected static byte[] requiredEnvBase64(String name) {
        return Base64.getDecoder().decode(requiredEnv(name));
    }
    protected static String optionalEnv(String name, String defaultValue) {
        String val = System.getenv(name);
        if (val == null) {
            return defaultValue;
        }
        return val;
    }
}
