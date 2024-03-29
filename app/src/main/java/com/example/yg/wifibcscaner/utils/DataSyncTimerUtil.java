package com.example.yg.wifibcscaner.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class DataSyncTimerUtil {
    public interface DataSyncListener {
        void doDataSync();
    }

    static Timer longTimer;
    static final int SYNC_TIME = 100000; // delay in milliseconds i.e. 5 min = 300000 ms or use timeout argument

    public static synchronized void startDataSyncTimer(final DataSyncListener dataSyncListener) {
        if (longTimer != null) {
            longTimer.cancel();
            longTimer = null;
        }
        if (longTimer == null) {
            longTimer = new Timer();
            longTimer.schedule(new TimerTask() {
                public void run() {
                    cancel();
                    longTimer = null;
                    dataSyncListener.doDataSync();
                }
            }, SYNC_TIME);
        }
    }

    public static synchronized void stopDataSyncTimer() {
        if (longTimer != null) {
            longTimer.cancel();
            longTimer = null;
        }
    }
}
