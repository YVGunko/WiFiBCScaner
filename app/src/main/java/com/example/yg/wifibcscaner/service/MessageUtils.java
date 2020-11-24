package com.example.yg.wifibcscaner.service;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import me.drakeet.support.toast.ToastCompat;

/**
 * Created by yg on 25.01.2018.
 */

public class MessageUtils {
    public void showMessage (Context context, String s){
        int duration = Toast.LENGTH_SHORT;

        if (android.os.Build.VERSION.SDK_INT >= 25) {
            ToastCompat.makeText(context, s, duration)
                    .setBadTokenListener(toast -> {
                        Log.e("failed toast",s);
                    }).show();
        } else {
            Toast.makeText(context, s, duration).show();
        }
    }
    public void showLongMessage (Context context, String s){
        int duration = Toast.LENGTH_LONG;
        if (android.os.Build.VERSION.SDK_INT >= 25) {
            ToastCompat.makeText(context, s, duration)
                    .setBadTokenListener(toast -> {
                        Log.e("failed toast",s);
                    }).show();
        } else {
            Toast.makeText(context, s, duration).show();
        }
    }
}
