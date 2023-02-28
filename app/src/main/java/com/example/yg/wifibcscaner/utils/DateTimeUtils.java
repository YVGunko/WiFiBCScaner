package com.example.yg.wifibcscaner.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Shahbaz Hashmi on 2020-03-21.
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

    public static long getDayTimeLong(Date date) {
        return org.apache.commons.lang3.time.DateUtils.truncate(date, Calendar.LONG_FORMAT).getTime();
    }
    public static Date getStartOfDayDate(Date date) {
        return org.apache.commons.lang3.time.DateUtils.truncate(date, Calendar.DATE);
    }
    public static long getStartOfDayLong(Date date) {
        return org.apache.commons.lang3.time.DateUtils.truncate(date, Calendar.DATE).getTime();
    }
    public static String getDayTimeString(Date date) {
        return org.apache.commons.lang3.time.DateFormatUtils.format(date, DT_PATTERN);
    }
    public static String getStartOfDayString(Date date) {
        return org.apache.commons.lang3.time.DateFormatUtils.format(date, DAY_PATTERN);
    }
    public static String getStartOfDayString(Long lDate) {
        return org.apache.commons.lang3.time.DateFormatUtils.format(lDate, D0_PATTERN);
    }
    public static String getLongDateTimeString(Long lDate) {
        return org.apache.commons.lang3.time.DateFormatUtils.format(lDate, DT_PATTERN);
    }
    public static String getStartOfYearString(Long lDate) {
        return org.apache.commons.lang3.time.DateFormatUtils.format(lDate, Y0_PATTERN);
    }
    public static long getLongStartOfDayLong(Long lDate) {
        return org.apache.commons.lang3.time.DateUtils.truncate(lDate, Calendar.DATE).getTime();
    }
    public static Date getStartOfYear(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, 1);
        return cal.getTime();
    }
    public static Date lastDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        return cal.getTime();
    }
    public static Date firstDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }
    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }
    public static int numberOfDaysInMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
    public static int currentDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }
}
