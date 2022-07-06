package com.namabhiksha.telegram.controllers;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.drive.Drive;
import com.namabhiksha.telegram.util.CalendarConstants;
import com.namabhiksha.telegram.util.MessageBuilder;
import com.namabhiksha.telegram.util.MultipartHelper;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.namabhiksha.telegram.util.CalendarConstants.AMERICA_TORONTO;
import static com.namabhiksha.telegram.util.CalendarConstants.START_TIME;

public class NamaSlotsScheduler {

    private final Calendar calendar;
    private final Drive drive;
    @Value("${util.telegram-url}")
    private String telegramURL;
    @Value("${util.nama-slots.calendar-id}")
    private String calendarIdValue;
    @Value("${util.nama-slots.chat-id}")
    private String chatId;
    @Value("${util.nama-slots.max-check-time}")
    private long maxTimeCheck;
    @Value("${util.api-token}")
    private String apiToken;
    @Value("${util.zoom-url}")
    private String zoomUrl;

    private final Set<String> parsedTimeSlots;

    private static final Logger log
            = org.apache.logging.log4j.LogManager.getLogger(NamaSlotsScheduler.class);

    public NamaSlotsScheduler(Calendar calendar, Drive drive) {
        this.calendar = calendar;
        this.drive = drive;
        this.parsedTimeSlots = new HashSet<>();
    }

    private String createUrlString(String apiToken, String action) {
        return String.format(telegramURL, apiToken, action);
    }

    @Scheduled(fixedRateString = "${util.nama-slots.schedule-time}", timeUnit = TimeUnit.MINUTES, zone = AMERICA_TORONTO)
    public void run() throws Exception {
        log.info("run invoked");
        getEvents(maxTimeCheck);
    }

    private void getEvents(long milliseconds) throws Exception {
        log.info("run invoked");
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
            log.info("No upcoming events found.");
        } else {
            for (Event event: items) {

                if (event.getSummary().contains(CalendarConstants.LAST_SLOT)) {
                    parsedTimeSlots.clear();
                    continue;
                } else if (parsedTimeSlots.contains(event.getId())) {
                    continue;
                } else {
                    String description = MessageBuilder.removeHTMLBlob(event.getDescription());

                    log.info(description);

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
