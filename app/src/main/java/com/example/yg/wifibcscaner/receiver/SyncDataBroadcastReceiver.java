package com.example.yg.wifibcscaner.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * Created by Shahbaz Hashmi on 2020-03-22.
 * BroadcastReceiver to be triggered by Alarm Manager
 */

public class SyncDataBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "SyncDataReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "called");
        new ArticleRepository().fetchAndSaveArticles(false);
    }
}
