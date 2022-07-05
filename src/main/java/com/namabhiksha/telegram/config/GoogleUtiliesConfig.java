package com.namabhiksha.telegram.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.namabhiksha.telegram.controllers.GoogleCalendarScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
public class GoogleUtiliesConfig {

    @Value("${service-account.private-key}")
    private String serviceAccountPrivateKey;

    @Value("${service-account.email}")
    private String serviceAccountEmail;

    /** Application name. */
    private static final String APPLICATION_NAME = "CalendarUtility";
    /** Global instance of the JSON factory. */

    private final List<String> scopes = Arrays.asList(CalendarScopes.CALENDAR, DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_PHOTOS_READONLY, DriveScopes.DRIVE);

    @Bean
    public NetHttpTransport getHttpTransport() throws GeneralSecurityException, IOException {
        return GoogleNetHttpTransport.newTrustedTransport();
    }

    @Bean
    public JsonFactory getJsonFactory() {
        return GsonFactory.getDefaultInstance();
    }

    private GoogleCredential getCredentials(@Autowired NetHttpTransport httpTransport,
                                            @Autowired JsonFactory jsonFactory,
                                            List<String> scopes) throws IOException, GeneralSecurityException {
        InputStream is = new ClassPathResource(serviceAccountPrivateKey).getInputStream();
       // File credentials = new File("config/godcanada-354600-18c327645dec.p12");
        return new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setServiceAccountId(serviceAccountEmail)
                .setServiceAccountScopes(scopes)
                .setServiceAccountPrivateKeyFromP12File(is)
                .build();
    }

    @Bean
    public Calendar googleCalendar(@Autowired NetHttpTransport httpTransport,
                                   @Autowired JsonFactory jsonFactory) throws GeneralSecurityException, IOException {
        GoogleCredential googleCredential = getCredentials(httpTransport, jsonFactory, Collections.singletonList(CalendarScopes.CALENDAR));
        return new Calendar.Builder(httpTransport, jsonFactory, googleCredential)
                .setApplicationName(APPLICATION_NAME)
                .setHttpRequestInitializer(googleCredential)
                .build();
    }

    @Bean
    public Drive googleDrive(@Autowired NetHttpTransport httpTransport,
                             @Autowired JsonFactory jsonFactory) throws GeneralSecurityException, IOException {
        GoogleCredential googleCredential = getCredentials(httpTransport, jsonFactory, Collections.singletonList(DriveScopes.DRIVE));
        return new Drive.Builder(httpTransport, jsonFactory, googleCredential)
                .setApplicationName(APPLICATION_NAME)
                .setHttpRequestInitializer(googleCredential)
                .build();
    }

    @Bean
    public GoogleCalendarScheduler googleCalendarScheduler(@Autowired Calendar calendar,
                                                           @Autowired Drive drive){
        return new GoogleCalendarScheduler(calendar, drive);
    }
}
