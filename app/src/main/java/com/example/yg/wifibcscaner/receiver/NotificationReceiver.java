package com.example.yg.wifibcscaner.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

// BroadcastReciever for updating notifications
public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";
    public NotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Intent broadcast received");
        updateNotification();
    }

    public void startNotificationReceiver(Context context){

    }
    public void updateNotification() {

        /*converting drawable into a bitmap
        Bitmap mascotImage  = BitmapFactory
                .decodeResource(getResources(),R.drawable.mascot_1);*/
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder();
        notifyBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                .bigPicture(mascotImage)
                .setBigContentTitle("Notification Updated!"));
        mNotifyManager.notify(NOTIFICATION_ID, notifyBuilder.build());

    }
}

