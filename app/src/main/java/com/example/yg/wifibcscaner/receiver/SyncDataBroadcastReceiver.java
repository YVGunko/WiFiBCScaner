package com.example.yg.wifibcscaner.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.yg.wifibcscaner.service.DataExchangeService;


public class SyncDataBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "SyncDataReceiver";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, " is called");
        new DataExchangeService().call();
    }
}
