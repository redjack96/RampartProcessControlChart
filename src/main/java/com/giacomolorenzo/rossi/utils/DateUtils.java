package com.giacomolorenzo.rossi.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

public class DateUtils {

    private DateUtils() {
    }

    /**
     * Restituisce la data all' inizio della giornata a partire da una stringa yyyy-MM-dd
     *
     * @param jiraDate data che CONTIENE il formato yyyy-MM-dd
     * @return la corrispondente Date all' inizio della giornata.
     */
    public static Date getDateFromYearMonthDayString(String jiraDate) {
        // Pattern-Matching della data
        var datePattern = Pattern.compile("([0-9]{4})-([0-9]{2})-([0-9]{2})");
        var dateMatcher = datePattern.matcher(jiraDate);
        if (dateMatcher.find()) {
            // Il gruppo zero corrisponde all' intera stringa trovata
            String year = dateMatcher.group(1);
            String month = dateMatcher.group(2);
            String day = dateMatcher.group(3);

            // Rimuovo i leading zero per il mese e per il giorno
            if (month.charAt(0) == '0') month = month.substring(1);
            if (day.charAt(0) == '0') day = day.substring(1);

            var calendar = Calendar.getInstance();

            calendar.set(YEAR, Integer.parseInt(year));
            calendar.set(MONTH, Integer.parseInt(month) - 1); // i mesi partono da 0 in Calendar
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
            return atStartOfDay(calendar.getTime());
        }
        return null;
    }

    public static Date toDate(LocalDate localDate) {
        //Creating Instant instance
        var instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

        //Creating Date instance using instant instance.
        return Date.from(instant);
    }

    public static Date atStartOfDay(Date d) {
        return toDate(toLocalDate(d));
    }

    public static Date toDate(LocalDateTime localDate) {
        return toDate(localDate.toLocalDate());
    }

    public static LocalDate toLocalDate(Date date) {
        return LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * @param startDate data1
     * @param endDate   data2
     * @return restituisce la data esattamente in mezzo tra startDate ed endDate, senza considerare l' ora
     */
    public static Date getMedianDate(Date startDate, Date endDate) {
        var startLocalDate = toLocalDate(startDate).atStartOfDay();
        var endLocalDate = toLocalDate(endDate).atStartOfDay();

        if (startDate.after(endDate)) {
            var copy = startLocalDate;
            startLocalDate = endLocalDate;
            endLocalDate = copy;
        }


        startDate = toDate(startLocalDate);
        endDate = toDate(endLocalDate);

        return new Date((startDate.getTime() + endDate.getTime()) / 2);
    }

    public static Date getMaxDate(List<Date> dateList) {
        var maxDate = dateList.get(0);
        for (Date date : dateList) {
            if (maxDate.before(date)) {
                maxDate = date;
            }
        }
        return maxDate;
    }

    public static Date getMinDate(List<Date> dateList) {
        var minDate = dateList.get(0);
        for (Date date : dateList) {
            if (minDate.after(date)) {
                minDate = date;
            }
        }
        return minDate;
    }

    public static String toYearMonthDayString(Date date) {
        var simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(date);
    }

    public static int numberOfDaysBetween(Date start, Date end) {
        return Math.abs((int) ((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)));
    }

    public static YearMonth getYearMonthFromDate(Date date) {
        return YearMonth.from(toLocalDate(date));
    }

    public static List<YearMonth> getYearMonthListFromDateRange(Date start, Date end) {
        Calendar c = Calendar.getInstance();
        List<YearMonth> ymList = new ArrayList<>();
        c.setTime(end);
        final int finalYear = c.get(YEAR);
        final int finalMonth = c.get(MONTH) + 1;  // +1 perché va da 0 a 11
        c.setTime(start);
        int year = c.get(YEAR);
        int month = c.get(MONTH) + 1; // +1 perché va da 0 a 11
        while (month <= finalMonth || year <= finalYear) {
            YearMonth ym = YearMonth.of(year, month);
            ymList.add(ym);
            YearMonth plus = ym.plus(1, ChronoUnit.MONTHS);
            year = plus.getYear();
            month = plus.getMonthValue(); // da 1 a 12
        }

        return ymList;
    }

    public static boolean isInMonth(Date date, YearMonth ym) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(MONTH) + 1 == ym.getMonthValue() && c.get(YEAR) == ym.getYear();
    }
}
