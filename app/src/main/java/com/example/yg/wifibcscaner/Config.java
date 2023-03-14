package com.example.yg.wifibcscaner;

import com.example.yg.wifibcscaner.utils.DateTimeUtils;

import java.util.Date;

import static com.example.yg.wifibcscaner.utils.DateTimeUtils.addDays;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getStartOfDayLong;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getStartOfDayString;
import static com.example.yg.wifibcscaner.utils.DateTimeUtils.numberOfDaysInMonth;

public class Config {

    public static final int SYNC_ALARM_REQUEST_CODE = 101;
    public static final int SYNC_NOTIF_REQUEST_CODE = 102;
    public static final String ACTION_USB_PERMISSION = "com.example.yg.wifibcscaner.USB_PERMISSION";
    public static final String DEFAULT_UPDATE_DATE = getStartOfDayString(getStartOfDayLong(addDays(new Date(), -numberOfDaysInMonth(new Date()))));

    //public static final String NEWS_FEED_URL = BuildConfig.BASE_URL+"/staticResponse.json";

}
