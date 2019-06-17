package com.dnastack.search.sheets.client;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.security.SecureRandom;
import java.util.List;

@Slf4j
@RestController
public class GoogleUserAuthenticationController {

    @Value("${sheets.auth.client-id}")
    private String clientId;

    @Value("${sheets.auth.client-secret}")
    private String clientSecret;

    @Value("${sheets.auth.scopes}")
    private List<String> scopes;

    private SecureRandom secureRandom = new SecureRandom();

    @GetMapping("/oauth/token")
    public ResponseEntity<?> getAuthToken() {

        // TODO this is only useful if we remember it and validate on callback
        String oauthState = String.valueOf(secureRandom.nextLong());

        String googleAuthUrl = getGoogleAuthorizationCodeFlow()
                .newAuthorizationUrl()
                .setRedirectUri(getOAuthRedirectUri())
                .setState(oauthState)
                .build();

        return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).location(URI.create(googleAuthUrl)).build();
    }

    private String getOAuthRedirectUri() {
        String redirectUri = ServletUriComponentsBuilder.fromCurrentRequest()
                .replacePath("/oauth/code")
                .replaceQuery(null)
                .build()
                .toUriString();
        log.info("Using redirectUri {}", redirectUri);
        return redirectUri;
    }

    @GetMapping("/oauth/code")
    public String handleOauthCallback(@RequestParam String code) throws IOException {
        Credential credential = getCredentialForAuthCode(code);
        return credential.getAccessToken();
    }

    private GoogleAuthorizationCodeFlow getGoogleAuthorizationCodeFlow() {
        return new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                clientId,
                clientSecret,
                scopes)
//                .setAccessType("offline")
                .build();
    }

    private Credential getCredentialForAuthCode(String authCode) throws IOException {
        GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow = getGoogleAuthorizationCodeFlow();
        GoogleTokenResponse response = googleAuthorizationCodeFlow
                .newTokenRequest(authCode)
                .setRedirectUri(getOAuthRedirectUri())
                .execute();
        return googleAuthorizationCodeFlow.createAndStoreCredential(response, null);
    }
}