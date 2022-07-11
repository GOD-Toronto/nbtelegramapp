package com.namabhiksha.telegram.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.namabhiksha.telegram.schedulers.AnnouncementsScheduler;
import com.namabhiksha.telegram.schedulers.CommonUtil;
import com.namabhiksha.telegram.schedulers.NamaSlotsScheduler;
import com.namabhiksha.telegram.schedulers.VolunteerTasksScheduler;
import com.namabhiksha.telegram.util.CalendarConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

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

    @Value("${util.telegram-url}")
    private String telegramURL;

    @Value("${util.api-token}")
    private String apiToken;

    @Value("${util.zoom-url}")
    private String zoomUrl;


    /** Application name. */
    private static final String APPLICATION_NAME = "CalendarUtility";
    /** Global instance of the JSON factory. */

    private final List<String> scopes = Arrays.asList("https://www.googleapis.com/auth/calendar.events.public.readonly",
            DriveScopes.DRIVE_READONLY);

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

        return GoogleCredential.fromStream(new ClassPathResource(CalendarConstants.CONFIG + serviceAccountJson).getInputStream())
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
    public CommonUtil getCommonUtil(@Autowired Calendar calendar, @Autowired Drive drive) {
        final StringBuilder zoomLinkTextBuilder = new StringBuilder();
        zoomLinkTextBuilder.append("\n");
        zoomLinkTextBuilder.append("-------------------");
        zoomLinkTextBuilder.append("\n");
        zoomLinkTextBuilder.append(CalendarConstants.PLEASE_JOIN);
        zoomLinkTextBuilder.append("<a href=\"" + zoomUrl + "\">" + zoomUrl + "</a>");
        return new CommonUtil(calendar, drive, telegramURL, apiToken, zoomLinkTextBuilder.toString());
    }

    @Bean
    public NamaSlotsScheduler namaSlotsScheduler(@Autowired CommonUtil commonUtil) {
        return new NamaSlotsScheduler(commonUtil);
    }

    @Bean
    public AnnouncementsScheduler announcementsScheduler(@Autowired CommonUtil commonUtil){
        return new AnnouncementsScheduler(commonUtil);
    }

    @Bean
    public VolunteerTasksScheduler volunteerTasksScheduler(@Autowired CommonUtil commonUtil){
        return new VolunteerTasksScheduler(commonUtil);
    }
}
