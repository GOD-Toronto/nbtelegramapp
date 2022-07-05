package com.godcanada.telegram.controllers;

import com.godcanada.telegram.util.CalendarConstants;
import com.godcanada.telegram.util.MessageBuilder;
import com.godcanada.telegram.util.MultipartHelper;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.drive.Drive;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.godcanada.telegram.util.CalendarConstants.AMERICA_TORONTO;
import static com.godcanada.telegram.util.CalendarConstants.START_TIME;

public class GoogleCalendarScheduler {

    private final Calendar calendar;
    private final Drive drive;
    @Value("${telegramURL}")
    private String telegramURL;
    @Value("${calendarIdValue}")
    private String calendarIdValue;
    @Value("${chatId}")
    private String chatId;
    @Value("${apiToken}")
    private String apiToken;
    @Value("${zoomUrl}")
    private String zoomUrl;
    private final Set<String> parsedTimeSlots;

    private static final Logger log
            = org.apache.logging.log4j.LogManager.getLogger(GoogleCalendarScheduler.class);

    public GoogleCalendarScheduler(Calendar calendar, Drive drive) {
        this.calendar = calendar;
        this.drive = drive;
        this.parsedTimeSlots = new HashSet<>();
    }

    private String createUrlString(String apiToken, String action) {
        return String.format(telegramURL, apiToken, action);
    }

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.MINUTES, zone = AMERICA_TORONTO)
    public void run() throws Exception {
        log.info("run invoked");
        getEvents(1800000);
    }

    private void getEvents(long milliseconds) throws Exception {
        log.info("run invoked");
        long currentMilliSeconds = System.currentTimeMillis();
        DateTime ctimemin = new DateTime(currentMilliSeconds);
        DateTime ctimemax = new DateTime(currentMilliSeconds + milliseconds);

        Events events = calendar.events().list(calendarIdValue)
                .setMaxResults(2)
                .setTimeMin(ctimemin)
                .setTimeMax(ctimemax)
                .setTimeZone(AMERICA_TORONTO)
                .setOrderBy(START_TIME)
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();
        if (items.isEmpty()) {
            log.info("No upcoming events found.");
        } else {
            for (Event event: items) {

                if (event.getSummary().contains(CalendarConstants.LAST_SLOT)) {
                    parsedTimeSlots.clear();
                    continue;
                } else if (parsedTimeSlots.contains(event.getId())) {
                    continue;
                }

/*                Optional<List<EventAttachment>> eventAttachItemsOptional = Optional.ofNullable(event.getAttachments());
                String description = null;
                // either the event has posters or just the text..
                if (eventAttachItemsOptional.isPresent()) {
                    List<EventAttachment> eventAttachments = event.getAttachments();

                    if (!eventAttachments.isEmpty()) {
                        EventAttachment eventAttachment = eventAttachments.get(0);

                        description = MessageBuilder.removeHTMLBlob(event.getDescription());

                        log.info(description);

                        MultipartHelper.processPhoto(chatId,
                                apiToken,
                                doGetInputStream(eventAttachment.getFileId()),
                                eventAttachment.getFileId(),
                                description,
                                telegramURL,
                                zoomUrl);
                    }

                } else {
                    description = MessageBuilder.removeHTMLBlob(event.getDescription());

                    log.info(description);

                    MultipartHelper.processTextMessage(chatId,
                            apiToken,
                            description,
                            telegramURL,
                            zoomUrl);
                }*/

                String description = MessageBuilder.removeHTMLBlob(event.getDescription());

                log.info(description);

                MultipartHelper.processTextMessage(chatId,
                        apiToken,
                        description,
                        telegramURL,
                        zoomUrl);

                parsedTimeSlots.add(event.getId());
            }
        }
    }

    private InputStream doGetInputStream(String fileId) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        drive.files().get( fileId ).executeMediaAndDownloadTo( out );
        return new ByteArrayInputStream( out.toByteArray() );
    }
}
