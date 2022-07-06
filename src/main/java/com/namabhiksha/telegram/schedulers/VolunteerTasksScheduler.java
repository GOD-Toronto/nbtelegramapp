package com.namabhiksha.telegram.schedulers;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.drive.Drive;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.namabhiksha.telegram.util.CalendarConstants.AMERICA_TORONTO;

public class VolunteerTasksScheduler {
    private final Calendar calendar;
    private final Drive drive;
    private final CommonUtil commonUtil;
    private final Set<String> parsedTimeSlots;
    @Value("${util.volunteers.calendar-id}")
    private String calendarIdValue;
    @Value("${util.volunteers.chat-id}")
    private String chatId;
    @Value("${util.volunteers.max-check-time}")
    private long maxTimeCheck;


    private static final Logger log
            = org.apache.logging.log4j.LogManager.getLogger(VolunteerTasksScheduler.class);

    public VolunteerTasksScheduler(Calendar calendar, Drive drive, CommonUtil commonUtil) {
        this.calendar = calendar;
        this.drive = drive;
        this.commonUtil = commonUtil;
        this.parsedTimeSlots = new HashSet<>();
    }

    @Scheduled(fixedRateString = "${util.volunteers.schedule-time}", timeUnit = TimeUnit.MINUTES, zone = AMERICA_TORONTO)
    public void run() throws Exception {
        log.info("run::run invoked");
        commonUtil.getEvents(maxTimeCheck, calendar, calendarIdValue,
                parsedTimeSlots, drive, chatId);
    }
}
