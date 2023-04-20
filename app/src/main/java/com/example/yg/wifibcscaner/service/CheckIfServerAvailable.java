package com.example.yg.wifibcscaner.service;

import android.util.Log;

import com.example.yg.wifibcscaner.controller.AppController;

import java.io.IOException;

public class CheckIfServerAvailable {
    private static final String TAG = "CheckIfServerAvailable";
    public static boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Log.d(TAG, "/system/bin/ping -c 1 "+ AppController.getInstance().getDbHelper().defs.get_Host_IP());
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 "+ AppController.getInstance().getDbHelper().defs.get_Host_IP());
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException | InterruptedException e) { e.printStackTrace(); }
        return false;
    }
}
