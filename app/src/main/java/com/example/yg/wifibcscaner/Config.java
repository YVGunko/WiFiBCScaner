package com.example.yg.wifibcscaner;

import com.example.yg.wifibcscaner.utils.DateTimeUtils;

import java.util.Date;

public class Config {

    public static final int SYNC_ALARM_REQUEST_CODE = 101;
    public static final String ACTION_USB_PERMISSION = "com.example.yg.wifibcscaner.USB_PERMISSION";
    public static final String DEFAULT_UPDATE_DATE = DateTimeUtils.getStartOfDayString(DateTimeUtils.addDays(new Date(), -186));

    //public static final String NEWS_FEED_URL = BuildConfig.BASE_URL+"/staticResponse.json";

}
