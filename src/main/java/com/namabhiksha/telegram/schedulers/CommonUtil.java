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
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.namabhiksha.telegram.util.CalendarConstants.AMERICA_TORONTO;
import static com.namabhiksha.telegram.util.CalendarConstants.START_TIME;

public class CommonUtil {
    @Value("${util.telegram-url}")
    private String telegramURL;

    @Value("${util.api-token}")
    private String apiToken;

    @Value("${util.zoom-url}")
    private String zoomUrl;

    private static final Logger log
            = org.apache.logging.log4j.LogManager.getLogger(CommonUtil.class);

    public void getEvents(long milliseconds, Calendar calendar, String calendarIdValue,
                                 Set<String> parsedTimeSlots, Drive drive, String chatId) throws Exception {
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
                    plainTextMessage(chatId, apiToken, telegramURL, zoomUrl, event);
                }

                parsedTimeSlots.add(event.getId());
            }
        }
    }

    private void plainTextMessage(String chatId, String apiToken, String telegramURL, String zoomUrl, Event event) throws IOException {
        String description = MessageBuilder.removeHTMLBlob(event.getDescription());

        log.info("plainTextMessage::description = [{}]", description);

        MultipartHelper.processTextMessage(chatId,
                apiToken,
                description,
                telegramURL,
                zoomUrl);
    }

}
