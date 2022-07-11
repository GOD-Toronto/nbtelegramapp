package com.namabhiksha.telegram.schedulers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashSet;
import java.util.Set;

import static com.namabhiksha.telegram.util.CalendarConstants.AMERICA_TORONTO;

public class NamaSlotsScheduler {
    private final CommonUtil commonUtil;
    private final Set<String> parsedTimeSlots;
    @Value("${calendars.nama-slots.calendar-id}")
    private String calendarIdValue;
    @Value("${calendars.nama-slots.chat-id}")
    private String chatId;
    @Value("${calendars.nama-slots.max-check-time}")
    private long maxTimeCheck;

    private static final Logger log = LoggerFactory.getLogger(NamaSlotsScheduler.class);

    public NamaSlotsScheduler(CommonUtil commonUtil) {
        this.commonUtil = commonUtil;
        this.parsedTimeSlots = new HashSet<>();
    }

    @Scheduled(cron = "${calendars.nama-slots.cron-expression}", zone = AMERICA_TORONTO)
    public void run() throws Exception {
        log.info("run::run invoked");
        commonUtil.getEvents(maxTimeCheck, calendarIdValue,
                parsedTimeSlots, chatId);
    }
}
