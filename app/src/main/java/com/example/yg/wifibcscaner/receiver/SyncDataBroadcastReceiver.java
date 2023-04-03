package com.example.yg.wifibcscaner.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.yg.wifibcscaner.data.repository.OrderOutDocBoxMovePartRepository;


public class SyncDataBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "SyncDataReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, " is called");
        new OrderOutDocBoxMovePartRepository().getData();
    }
}
