package com.namabhiksha.telegram.schedulers;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttachment;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.drive.Drive;
import com.namabhiksha.telegram.util.CalendarConstants;
import com.namabhiksha.telegram.util.MessageBuilder;
import com.namabhiksha.telegram.util.MultipartHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.namabhiksha.telegram.util.CalendarConstants.AMERICA_TORONTO;
import static com.namabhiksha.telegram.util.CalendarConstants.START_TIME;

public class CommonUtil {
    private final Calendar calendar;
    private final Drive drive;
    private final String telegramURL;
    private final String apiToken;

    private final String errorAlertChatId;
    private final String zoomLinkText;

    private static final Logger log = LoggerFactory.getLogger(CommonUtil.class);

    public CommonUtil(Calendar calendar,
                      Drive drive,
                      String telegramURL,
                      String apiToken,
                      String errorAlertChatId, String zoomLinkText) {
        this.calendar = calendar;
        this.drive = drive;
        this.telegramURL = telegramURL;
        this.apiToken = apiToken;
        this.errorAlertChatId = errorAlertChatId;
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

                    processEventAttachments(chatId, event, eventAttachments);
                } else {
                    plainTextMessage(chatId, event);
                }

                parsedTimeSlots.add(event.getId());
            }
        }
    }

    private void processEventAttachments(String chatId, Event event, List<EventAttachment> eventAttachments) throws IOException {
        String description;
        for (EventAttachment eventAttach : eventAttachments) {

            String fileId = eventAttach.getFileId();
            description = MessageBuilder.removeHTMLBlob(event.getDescription());
            log.info("getEvents::description = [{}]", description);

/*                 File file = null;
        try (InputStream stream = new ClassPathResource(CalendarConstants.IMAGES + event.getSummary()+ CalendarConstants.JPG).getInputStream()) {

            file = new File(event.getSummary());
            // convert input stream to file
            FileUtils.copyInputStreamToFile(stream, file);

        } catch (IOException ex) {
            ex.printStackTrace();
        }*/

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            com.google.api.services.drive.model.File file = drive.files().get(fileId).execute();
            String fileName = file.getName();

            log.info("getEvents::fileName = [{}]", fileName);

            drive.files().get(fileId).executeMediaAndDownloadTo(outputStream);

            FileOutputStream outputfile = new FileOutputStream(fileName);
            outputStream.writeTo(outputfile);
            outputfile.close();
            outputStream.close();

            switch (file.getMimeType()) {
                case CalendarConstants.IMAGE_JPEG:
                    MultipartHelper.processPhoto(chatId,
                            apiToken,
                            fileName,
                            description,
                            telegramURL,
                            errorAlertChatId,
                            zoomLinkText);
                    break;
                case CalendarConstants.AUDIO_MPEG:
                case CalendarConstants.AUDIO_X_M_4_A:
                case CalendarConstants.AUDIO_OGG:
                    MultipartHelper.processMusic(chatId,
                            apiToken,
                            fileName,
                            description,
                            telegramURL,
                            errorAlertChatId,
                            zoomLinkText);
                    break;
                default:
                    plainTextMessage(chatId, event);
                    break;
            }
        }
    }

    private void plainTextMessage(String chatId, Event event) {
        String description = MessageBuilder.removeHTMLBlob(event.getDescription());

        log.info("plainTextMessage::description = [{}]", description);

        MultipartHelper.processTextMessage(chatId,
                apiToken,
                description,
                telegramURL,
                zoomLinkText);
    }

}
