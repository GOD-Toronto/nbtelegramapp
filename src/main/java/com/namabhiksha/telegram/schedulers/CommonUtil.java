package com.namabhiksha.telegram.schedulers;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttachment;
import com.google.api.services.calendar.model.Events;
import com.namabhiksha.telegram.util.CalendarConstants;
import com.namabhiksha.telegram.util.MessageBuilder;
import com.namabhiksha.telegram.util.MultipartHelper;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.namabhiksha.telegram.util.CalendarConstants.AMERICA_TORONTO;
import static com.namabhiksha.telegram.util.CalendarConstants.START_TIME;

public class CommonUtil {
    private final Calendar calendar;
    private final String telegramURL;
    private final String apiToken;
    private final String zoomLinkText;

    private static final Logger log
            = org.apache.logging.log4j.LogManager.getLogger(CommonUtil.class);

    public CommonUtil(Calendar calendar,
                      String telegramURL,
                      String apiToken,
                      String zoomLinkText) {
        this.calendar = calendar;
        this.telegramURL = telegramURL;
        this.apiToken = apiToken;
        this.zoomLinkText = zoomLinkText;
    }

    public void getEvents(long milliseconds, String calendarIdValue,
                          Set<String> parsedTimeSlots, String chatId) throws Exception {
        log.info("getEvents");
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
            log.info("getEvents::No upcoming events found.");
        } else {
            for (Event event : items) {

                if (event.getSummary().contains(CalendarConstants.LAST_SLOT)) {
                    log.info("getEvents::Self cleaning the data structures");
                    parsedTimeSlots.clear();
                    continue;
                } else if (parsedTimeSlots.contains(event.getId())) {
                    log.info("getEvents::No new events to be notified");
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

                    File file = null;
                    try (InputStream stream = new ClassPathResource(CalendarConstants.IMAGES + event.getSummary()+ CalendarConstants.JPG).getInputStream()) {

                        file = new File(event.getSummary());
                        // convert input stream to file
                        FileUtils.copyInputStreamToFile(stream, file);

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    MultipartHelper.processPhoto(chatId,
                            apiToken,
                            file,
                            description,
                            telegramURL,
                            zoomLinkText);

                } else {
                    plainTextMessage(chatId, event);
                }

                parsedTimeSlots.add(event.getId());
            }
        }
    }

    private void plainTextMessage(String chatId, Event event) throws IOException {
        String description = MessageBuilder.removeHTMLBlob(event.getDescription());

        log.info("plainTextMessage::description = [{}]", description);

        MultipartHelper.processTextMessage(chatId,
                apiToken,
                description,
                telegramURL,
                zoomLinkText);
    }

}
