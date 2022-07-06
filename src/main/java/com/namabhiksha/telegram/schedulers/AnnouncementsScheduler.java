package com.namabhiksha.telegram.schedulers;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttachment;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.namabhiksha.telegram.util.CalendarConstants;
import com.namabhiksha.telegram.util.MessageBuilder;
import com.namabhiksha.telegram.util.MultipartHelper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.namabhiksha.telegram.util.CalendarConstants.AMERICA_TORONTO;
import static com.namabhiksha.telegram.util.CalendarConstants.START_TIME;
import static org.springframework.util.ResourceUtils.getFile;

public class AnnouncementsScheduler {

    private final Calendar calendar;
    private final Drive drive;
    @Value("${util.telegram-url}")
    private String telegramURL;
    @Value("${util.announcements.calendar-id}")
    private String calendarIdValue;
    @Value("${util.announcements.chat-id}")
    private String chatId;

    @Value("${util.announcements.max-check-time}")
    private long maxTimeCheck;
    @Value("${util.api-token}")
    private String apiToken;
    @Value("${util.zoom-url}")
    private String zoomUrl;
    private final Set<String> parsedTimeSlots;

    private static final Logger log
            = org.apache.logging.log4j.LogManager.getLogger(AnnouncementsScheduler.class);

    public AnnouncementsScheduler(Calendar calendar, Drive drive) {
        this.calendar = calendar;
        this.drive = drive;
        this.parsedTimeSlots = new HashSet<>();
    }

    private String createUrlString(String apiToken, String action) {
        return String.format(telegramURL, apiToken, action);
    }

    @Scheduled(fixedRateString = "${util.nama-slots.schedule-time}", timeUnit = TimeUnit.MINUTES, zone = AMERICA_TORONTO)
    public void run() throws Exception {
        log.info("run::run invoked");
        getEvents(maxTimeCheck);
    }

    private void getEvents(long milliseconds) throws Exception {
        log.info("getEvents");
        long currentMilliSeconds = System.currentTimeMillis();
        DateTime ctimemin = new DateTime(currentMilliSeconds);
        DateTime ctimemax = new DateTime(currentMilliSeconds + milliseconds);

        Events events = calendar.events().list(calendarIdValue)
                .setMaxResults(1)
                .setTimeMin(ctimemin)
                .setTimeMax(ctimemax)
                .setTimeZone(AMERICA_TORONTO)
                .setOrderBy(START_TIME)
                .setSingleEvents(true)
                .execute();


        List<Event> items = events.getItems();
        if (items.isEmpty()) {
            log.info("getEvents::No upcoming events found.");
        } else {
            for (Event event : items) {

                if (event.getSummary().contains(CalendarConstants.LAST_SLOT)) {
                    parsedTimeSlots.clear();
                    continue;
                } else if (parsedTimeSlots.contains(event.getId())) {
                    continue;
                }

                Optional<List<EventAttachment>> eventAttachItemsOptional = Optional.ofNullable(event.getAttachments());
                String description = null;
                // either the event has posters or just the text..
                if (eventAttachItemsOptional.isPresent()) {
                    List<EventAttachment> eventAttachments = event.getAttachments();

                    if (eventAttachments.isEmpty()) {
                        continue;
                    }

                    EventAttachment eventAttach = event.getAttachments().get(0);

                    String fileId = eventAttach.getFileId();
                    description = MessageBuilder.removeHTMLBlob(event.getDescription());
                    log.info("getEvents::description = [{}]", description);

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    File file = drive.files().get(fileId).execute();
                    String fileName = file.getName();

                    log.info("getEvents::fileName = [{}]", fileName);

                    drive.files().get(fileId).executeMediaAndDownloadTo(outputStream);

                    FileOutputStream outputfile = new FileOutputStream(fileName);
                    outputStream.writeTo(outputfile);
                    outputfile.close();
                    outputStream.close();

                    MultipartHelper.processPhoto(chatId,
                            apiToken,
                            fileName,
                            description,
                            telegramURL,
                            zoomUrl);

                } else {
                    description = MessageBuilder.removeHTMLBlob(event.getDescription());
                    log.info("getEvents::description = [{}]", description);
                    MultipartHelper.processTextMessage(chatId,
                            apiToken,
                            description,
                            telegramURL,
                            zoomUrl);
                }

                parsedTimeSlots.add(event.getId());
            }
        }
    }
}