package com.example.signingoogle2.Config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import com.google.api.services.people.v1.PeopleServiceScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
public class GoogleDriveConfig {

    @Value("classpath:keys/client_secret_signingoogle.json")
    private Resource secretKeyResource;

    @Value("${google.credentials.folder.path}")
    private Resource credentialsFolder;

    public static HttpTransport httpTransport = new NetHttpTransport();
    public static JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    private static final List<String> SCOPES = Arrays.asList(
            DriveScopes.DRIVE,
            PeopleServiceScopes.USERINFO_PROFILE,
            PeopleServiceScopes.USERINFO_EMAIL,
            PeopleServiceScopes.CONTACTS_READONLY
    );

    @Bean
    public GoogleAuthorizationCodeFlow codeFlow() throws IOException {
        // Load client secrets.
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory,
                new InputStreamReader(secretKeyResource.getInputStream()));

        // Set up authorization code flow for OAuth 2.0.
        return new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets,
                SCOPES).setDataStoreFactory(new FileDataStoreFactory(credentialsFolder.getFile())).build();
    }

}

