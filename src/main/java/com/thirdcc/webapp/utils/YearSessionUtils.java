package com.thirdcc.webapp.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class YearSessionUtils {

    private final Logger log = LoggerFactory.getLogger(YearSessionUtils.class);
    private static final ZoneId zoneId = ZoneId.systemDefault();
    private static final Month[] months = {
        Month.SEPTEMBER,
        Month.OCTOBER,
        Month.NOVEMBER,
        Month.DECEMBER,
        Month.JANUARY,
        Month.FEBRUARY,
        Month.MARCH,
        Month.APRIL,
        Month.MAY,
        Month.JUNE,
        Month.JULY,
        Month.AUGUST,
    };

    // e.g. 2019/2020
    public static boolean isValidYearSession(String yearSession) {
        return yearSession.matches("(\\d{4})/(\\d{4})");
    }

    public static String getCurrentYearSession() {
        return toYearSession(Instant.now());
    }
    
    public static String getNextYearSession() {
        return toYearSession(LocalDateTime.now().plusYears(1).atZone(zoneId).toInstant());
    }

    public static String toYearSession(Instant instant) {
        LocalDate localDate = instant.atZone(zoneId).toLocalDate();
        int year = localDate.getYear();
        Month month = localDate.getMonth();

        int firstYear = year;
        int secondYear = year + 1;

        boolean isInPreviousYearSession = month.getValue() < getFirstMonthOfYearSession().getValue();
        if (isInPreviousYearSession) {
            firstYear = year - 1;
            secondYear = year;
        }
        return String.format("%d/%d", firstYear, secondYear);
    }

    public static Month getFirstMonthOfYearSession() {
        return months[0];
    }

    public static List<Month> getAllMonthsOfYearSession() {
        return new ArrayList<>(Arrays.asList(months));
    }

    public static Instant getFirstInstantOfYearSession(String yearSession) {
        if (!isValidYearSession(yearSession)) {
            throw new IllegalArgumentException("invalid yearSession of " + yearSession);
        }
        int year = Integer.parseInt(yearSession.substring(0, 4));
        LocalDate firstLocalDateOfYearSession = LocalDate.of(year, getFirstMonthOfYearSession(), 1);
        return firstLocalDateOfYearSession.atStartOfDay(zoneId).toInstant();
    }

    public static String addYearSessionWithSemester(String yearSession, Integer numOfSem) {
        Integer numOfYear = Math.round(numOfSem / 2.0f);
        Integer firstYear = Integer.parseInt(yearSession.substring(0, 4));
        Integer secondYear = Integer.parseInt(yearSession.substring(yearSession.length() - 4));
        firstYear += numOfYear;
        secondYear += numOfYear;
        return String.format("%d/%d", firstYear, secondYear);
    }

    public static boolean isBefore(String earlierYS, String laterYs) {
        if (!isValidYearSession(earlierYS) || !isValidYearSession(laterYs)) {
            throw new IllegalArgumentException("invalid yearSession of " + earlierYS + " or " + laterYs);
        }
        return earlierYS.compareTo(laterYs) < 1;
    }
    
    public static boolean isBetweenMonth(Month monthToCheck, Month start, Month end) {
        return monthToCheck.compareTo(start) >= 0 && monthToCheck.compareTo(end) <= 0;
    }

}
