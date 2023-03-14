package com.example.yg.wifibcscaner.utils;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.yg.wifibcscaner.MainActivity;
import com.example.yg.wifibcscaner.R;


public class NotificationUtils {
    private static final String TAG = "NotificationUtils";
    // pendingIntentId to reference the pending intent (unique)
    private static final int PENDING_INTENT_ID_SPROJECT = 7062;
    private static final int NOTIFICATION_ID_SPROJECT = 3201;

    public static void notify(Context context, String message, String channelId)
    {
        Log.d(TAG, ": entered notify");

        String notificationTitle = context.getString(R.string.app_name);
        createMainNotificationChannel(context, channelId);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(notificationTitle)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(contentIntent(context))
                .setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        // NOTIFICATION_ID allows you to update or cancel the notification later on...
        notificationManagerCompat.notify(NOTIFICATION_ID_SPROJECT, builder.build());
    }

    public static void createMainNotificationChannel(Context context, String chanelId) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = context.getString(R.string.main_channel);
            String channelDescription = context.getString(R.string.main_channel_description);
            NotificationChannel notificationChannel =
                    new NotificationChannel(chanelId, channelName,
                            NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(channelDescription);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationChannel.setShowBadge(true);
            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }


    public static PendingIntent contentIntent(Context context)
    {
        Intent startActivityIntent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(
                context,
                PENDING_INTENT_ID_SPROJECT,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
