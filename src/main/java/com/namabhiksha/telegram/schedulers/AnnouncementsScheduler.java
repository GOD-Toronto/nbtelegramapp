package com.namabhiksha.telegram.schedulers;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashSet;
import java.util.Set;

import static com.namabhiksha.telegram.util.CalendarConstants.AMERICA_TORONTO;

public class AnnouncementsScheduler {
    private final CommonUtil commonUtil;
    private final Set<String> parsedTimeSlots;

    @Value("${util.announcements.calendar-id}")
    private String calendarId;
    @Value("${util.announcements.chat-id}")
    private String chatId;
    @Value("${util.announcements.max-check-time}")
    private long maxTimeCheck;

    private static final Logger log
            = org.apache.logging.log4j.LogManager.getLogger(AnnouncementsScheduler.class);

    public AnnouncementsScheduler(CommonUtil commonUtil) {
        this.commonUtil = commonUtil;
        this.parsedTimeSlots = new HashSet<>();
    }

    @Scheduled(cron = "${util.announcements.cron-expression}", zone = AMERICA_TORONTO)
    public void run() throws Exception {
        log.info("run::run invoked");
        commonUtil.getEvents(maxTimeCheck, calendarId,
                parsedTimeSlots, chatId);
    }
}
