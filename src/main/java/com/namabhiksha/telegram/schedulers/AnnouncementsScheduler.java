package com.namabhiksha.telegram.schedulers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashSet;
import java.util.Set;

import static com.namabhiksha.telegram.util.CalendarConstants.AMERICA_TORONTO;

public class AnnouncementsScheduler {
    private final CommonUtil commonUtil;
    private final Set<String> parsedTimeSlots;

    @Value("${calendars.announcements.calendar-id}")
    private String calendarId;
    @Value("${calendars.announcements.chat-id}")
    private String chatId;
    @Value("${calendars.announcements.max-check-time}")
    private long maxTimeCheck;

    private static final Logger log = LoggerFactory.getLogger(AnnouncementsScheduler.class);

    public AnnouncementsScheduler(CommonUtil commonUtil) {
        this.commonUtil = commonUtil;
        this.parsedTimeSlots = new HashSet<>();
    }

    @Scheduled(cron = "${calendars.announcements.cron-expression}", zone = AMERICA_TORONTO)
    public void run() throws Exception {
        log.info("run::run invoked");
        commonUtil.getEvents(maxTimeCheck, calendarId,
                parsedTimeSlots, chatId);
    }
}
