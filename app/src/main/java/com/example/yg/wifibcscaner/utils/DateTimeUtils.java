package com.example.yg.wifibcscaner.utils;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;

/**
 * Created by YG on 2020-03-21.
 */
public class DateTimeUtils {

    private static final String API_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String DESIRED_FORMAT = "E, d MMM yyyy h:mm a";

    public static long getTimestamp(String dateTime) {
        SimpleDateFormat inputFormat = new SimpleDateFormat(API_FORMAT, Locale.ENGLISH);
        Date date = null;
        try {
            date = inputFormat.parse(dateTime);
        } catch (Exception e) {
            return 0;
        }
        return date.getTime();
    }


    public static String getFormattedDateTime(long timestamp) {
        SimpleDateFormat outputFormat = new SimpleDateFormat(DESIRED_FORMAT, Locale.ENGLISH);

        Date date = null;
        String str = null;

        try {
            date = new Date(timestamp);
            str = outputFormat.format(date);
        } catch (Exception e) {
            str = "";
            e.printStackTrace();
        }
        return str;
    }

    private static final String DT_PATTERN = "dd.MM.yyyy HH:mm:ss";
    private static final String D0_PATTERN = "dd.MM.yyyy 00:00:00";
    private static final String Y0_PATTERN = "01.01.yyyy 00:00:00";
    private static final String DAY_PATTERN = "dd.MM.yyyy";

    public static long getDayTimeLong(final Date date) {
        return org.apache.commons.lang3.time.DateUtils.truncate(date, Calendar.LONG_FORMAT).getTime();
    }
    public static Date getStartOfDayDate(final Date date) {
        return org.apache.commons.lang3.time.DateUtils.truncate(date, Calendar.DATE);
    }
    public static long getStartOfDayLong(final Date date) {
        return org.apache.commons.lang3.time.DateUtils.truncate(date, Calendar.DATE).getTime();
    }
    public static String getDayTimeString(final Date date) {
        return org.apache.commons.lang3.time.DateFormatUtils.format(date, DT_PATTERN);
    }
    public static String getDayTimeString(final long lDate) {
        return org.apache.commons.lang3.time.DateFormatUtils.format(lDate, DT_PATTERN);
    }
    public static String getStartOfDayString(final Date date) {
        return org.apache.commons.lang3.time.DateFormatUtils.format(date, DAY_PATTERN);
    }
    public static String getStartOfDayString(final long lDate) {
        return org.apache.commons.lang3.time.DateFormatUtils.format(lDate, D0_PATTERN);
    }
    public static String getLongDateTimeString(final long lDate) {
        return org.apache.commons.lang3.time.DateFormatUtils.format(lDate, DT_PATTERN);
    }
    public static String getStartOfYearString(final long lDate) {
        return org.apache.commons.lang3.time.DateFormatUtils.format(lDate, Y0_PATTERN);
    }
    public static long getLongStartOfDayLong(final long lDate) {
        return org.apache.commons.lang3.time.DateUtils.truncate(lDate, Calendar.DATE).getTime();
    }

    public static Date getStartOfYear(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, 1);
        return cal.getTime();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Long getFirstDayOfYear() {
        LocalDate now = LocalDate.now(); // 2015-11-23
        LocalDate firstDay = now.with(firstDayOfYear()); // 2015-01-01
        return toMillis(firstDay);
    }

    public static Date lastDayOfMonth(final Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        return cal.getTime();
    }
    public static Date firstDayOfMonth(final Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }
    public static Date addDays(final Date date, final int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }
    public static int numberOfDaysInMonth(final Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
    public static int currentDayOfMonth(final Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }
    public static long getDateLong (final String sDate){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DAY_PATTERN);
            Date date = sdf.parse(sDate);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public static long getDateTimeLong (final String sDate){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DT_PATTERN);
            Date date = sdf.parse(sDate);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static LocalDateTime toLocalDateTime(final Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static LocalDate toLocalDate(final Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static LocalDate toLocalDate(final long dateToConvert) {
        return Instant.ofEpochMilli(dateToConvert).atZone(ZoneId.systemDefault()).toLocalDate();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static long toMillis(final LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();}
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static long toMillis(final LocalDate localDate) {
        return localDate.atTime(0,0) .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();}
}
