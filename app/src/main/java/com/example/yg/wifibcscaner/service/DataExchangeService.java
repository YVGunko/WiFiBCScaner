package com.example.yg.wifibcscaner.service;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.utils.AppUtils;
import com.example.yg.wifibcscaner.utils.NotificationUtils;
import com.example.yg.wifibcscaner.utils.executors.DefaultExecutorSupplier;
import com.example.yg.wifibcscaner.data.repository.BaseClassRepo;
import com.example.yg.wifibcscaner.data.repository.OrderOutDocBoxMovePartRepository;
import com.example.yg.wifibcscaner.data.repository.OutDocWithBoxWithMovesWithPartsRepo;

public class DataExchangeService {
    private static final String TAG = "DataExchangeService";
    private static final String MY_CHANNEL_ID = "Data Exchange";

    NotificationUtils notificationUtils;

    public void setNotificationUtils(NotificationUtils notificationUtils) {
        this.notificationUtils = notificationUtils;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void call() {

        notificationUtils = new NotificationUtils();
        setNotificationUtils(notificationUtils);

        if (notificationUtils != null)
            Log.d(TAG, "notificationUtils -> null");

        if (!AppUtils.isNetworkAvailable(AppController.getInstance())) {
            Log.d(TAG, "isNetworkAvailable -> no");
            if (notificationUtils != null)
                //notificationUtils.notify(context, AppController.getInstance().getResourses().getString(R.string.error_connection));
                DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                    notificationUtils.notify(AppController.getInstance().getApplicationContext(),
                            AppController.getInstance().getResourses().getString(R.string.network_unawailable),
                            MY_CHANNEL_ID);
                });
            return;
        }
        new OutDocWithBoxWithMovesWithPartsRepo().call();
        new BaseClassRepo().getData();
        new OrderOutDocBoxMovePartRepository().getData();
    }
}
