package com.namabhiksha.telegram.schedulers;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.drive.Drive;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashSet;
import java.util.Set;

import static com.namabhiksha.telegram.util.CalendarConstants.AMERICA_TORONTO;

public class TestSlotsScheduler {
    private final Calendar calendar;
    private final Drive drive;
    private final CommonUtil commonUtil;
    private final Set<String> parsedTimeSlots;
    @Value("${util.nama-slots.calendar-id}")
    private String calendarIdValue;
    @Value("${util.nama-slots.chat-id}")
    private String chatId;
    @Value("${util.nama-slots.max-check-time}")
    private long maxTimeCheck;

    private static final Logger log
            = org.apache.logging.log4j.LogManager.getLogger(TestSlotsScheduler.class);

    public TestSlotsScheduler(Calendar calendar, Drive drive, CommonUtil commonUtil) {
        this.calendar = calendar;
        this.drive = drive;
        this.commonUtil = commonUtil;
        this.parsedTimeSlots = new HashSet<>();
    }

    @Scheduled(cron = "${util.nama-slots.cron-expression}", zone = AMERICA_TORONTO)
    public void run() throws Exception {
        log.info("run::run invoked");
    }
}
