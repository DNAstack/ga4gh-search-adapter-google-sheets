package com.dnastack.search.sheets.client;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SheetsServiceFactory {

    @Value("{spring.application.name}")
    private String appName;

    @Autowired
    private JsonFactory jsonFactory;

    @Autowired
    private NetHttpTransport httpTransport;

    public SheetsService create(String accessToken) {
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        Sheets service = new Sheets.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(appName)
                .build();

        return new SheetsService(new SheetsClientWrapper(service));
    }
}
