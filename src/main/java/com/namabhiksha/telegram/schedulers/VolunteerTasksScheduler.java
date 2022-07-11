package com.namabhiksha.telegram.schedulers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashSet;
import java.util.Set;

import static com.namabhiksha.telegram.util.CalendarConstants.AMERICA_TORONTO;

public class VolunteerTasksScheduler {
    private final CommonUtil commonUtil;
    private final Set<String> parsedTimeSlots;
    @Value("${calendars.volunteers.calendar-id}")
    private String calendarId;
    @Value("${calendars.volunteers.chat-id}")
    private String chatId;
    @Value("${calendars.volunteers.max-check-time}")
    private long maxTimeCheck;

    private static final Logger log = LoggerFactory.getLogger(VolunteerTasksScheduler.class);

    public VolunteerTasksScheduler(CommonUtil commonUtil) {
        this.commonUtil = commonUtil;
        this.parsedTimeSlots = new HashSet<>();
    }

    @Scheduled(cron = "${calendars.volunteers.cron-expression}", zone = AMERICA_TORONTO)
    public void run() throws Exception {
        log.info("run::run invoked");
        commonUtil.getEvents(maxTimeCheck, calendarId,
                parsedTimeSlots, chatId);
    }
}
