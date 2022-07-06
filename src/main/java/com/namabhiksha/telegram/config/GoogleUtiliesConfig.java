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
import com.namabhiksha.telegram.schedulers.AnnouncementsScheduler;
import com.namabhiksha.telegram.schedulers.NamaSlotsScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

@Configuration
public class GoogleUtiliesConfig {

    @Value("${service-account.json}")
    private String serviceAccountJson;

    @Value("${service-account.email}")
    private String serviceAccountEmail;

    /** Application name. */
    private static final String APPLICATION_NAME = "CalendarUtility";
    /** Global instance of the JSON factory. */


    private final List<String> scopes = Arrays.asList(DriveScopes.DRIVE, CalendarScopes.CALENDAR);

    @Bean
    public NetHttpTransport getHttpTransport() throws GeneralSecurityException, IOException {
        return GoogleNetHttpTransport.newTrustedTransport();
    }

    @Bean
    public JsonFactory getJsonFactory() {
        return GsonFactory.getDefaultInstance();
    }

    private GoogleCredential getCredentials(@Autowired NetHttpTransport httpTransport,
                                            @Autowired JsonFactory jsonFactory) throws IOException, GeneralSecurityException {

        return GoogleCredential.fromStream(this.getClass().getClassLoader().getResourceAsStream(serviceAccountJson))
                .createScoped(scopes);

     /*   InputStream is = new ClassPathResource(serviceAccountPrivateKey).getInputStream();
          return new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setServiceAccountId(serviceAccountEmail)
                .setServiceAccountScopes(scopes)
                .setServiceAccountPrivateKeyFromP12File(is)
                .build();*/
    }

    @Bean
    public Calendar googleCalendar(@Autowired NetHttpTransport httpTransport,
                                   @Autowired JsonFactory jsonFactory) throws GeneralSecurityException, IOException {
        GoogleCredential googleCredential = getCredentials(httpTransport, jsonFactory);
        return new Calendar.Builder(httpTransport, jsonFactory, googleCredential)
                .setApplicationName(APPLICATION_NAME)
                .setHttpRequestInitializer(googleCredential)
                .build();
    }

    @Bean
    public Drive googleDrive(@Autowired NetHttpTransport httpTransport,
                             @Autowired JsonFactory jsonFactory) throws GeneralSecurityException, IOException {
        GoogleCredential googleCredential = getCredentials(httpTransport, jsonFactory);
        return new Drive.Builder(httpTransport, jsonFactory, googleCredential)
                .setApplicationName(APPLICATION_NAME)
                .setHttpRequestInitializer(googleCredential)
                .build();
    }

    @Bean
    public NamaSlotsScheduler namaSlotsScheduler(@Autowired Calendar calendar,
                                                      @Autowired Drive drive){
        return new NamaSlotsScheduler(calendar, drive);
    }

    @Bean
    public AnnouncementsScheduler announcementsScheduler(@Autowired Calendar calendar,
                                                          @Autowired Drive drive){
        return new AnnouncementsScheduler(calendar, drive);
    }
}
