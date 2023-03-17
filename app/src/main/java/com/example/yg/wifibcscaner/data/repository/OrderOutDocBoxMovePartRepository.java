package com.example.yg.wifibcscaner.data.repository;

import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;
import android.content.Context;
import java.util.concurrent.atomic.AtomicBoolean;

import com.example.yg.wifibcscaner.DataBaseHelper;
import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.dto.OrderOutDocBoxMovePart;
import com.example.yg.wifibcscaner.interfaces.FetchListDataListener;
import com.example.yg.wifibcscaner.utils.ApiUtils;
import com.example.yg.wifibcscaner.utils.AppUtils;
import com.example.yg.wifibcscaner.utils.NotificationUtils;
import com.example.yg.wifibcscaner.utils.SharedPreferenceManager;
import com.example.yg.wifibcscaner.utils.executors.DefaultExecutorSupplier;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by Yury Gunko on 2023-02-17.
 */
public class OrderOutDocBoxMovePartRepository {

    private static final String TAG = "dataDownloadRepository";
    private static final String MY_CHANNEL_ID = "Download Status";

    NotificationUtils notificationUtils;

    public void setNotificationUtils(NotificationUtils notificationUtils) {
        this.notificationUtils = notificationUtils;
    }

    /**
     * only this method should be used from UI
     * handles all success and fallback
     *
     * @param context
     */
    public void getData(Context context) {

        notificationUtils = new NotificationUtils();
        setNotificationUtils(notificationUtils);

        if (notificationUtils != null)
            Log.d(TAG, "notificationUtils -> null");

        if (!AppUtils.isNetworkAvailable(AppController.getInstance())) {
            Log.d(TAG, "isNetworkAvailable -> no");
            if (notificationUtils != null)
                //notificationUtils.notify(context, AppController.getInstance().getResourses().getString(R.string.error_connection));
            DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                notificationUtils.notify(context,
                        AppController.getInstance().getResourses().getString(R.string.error_something_went_wrong),
                        MY_CHANNEL_ID);
            });
            return;
        }

        downloadData(context);
    }
    /**
     * run a download sequence
     * @param context
     */
    public void downloadData(Context context) {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            try {
                Log.d(TAG, "downloadData -> update date: " + SharedPreferenceManager.getInstance().getUpdateDateString());
                Log.d(TAG, "downloadData -> current page to load: " + SharedPreferenceManager.getInstance().getCurrentPageToLoad());

                DataBaseHelper mDbHelper = AppController.getInstance().getDbHelper();
                SharedPreferenceManager.getInstance().setNextPageToLoadToZero();
                String updateDate = SharedPreferenceManager.getInstance().getUpdateDateString();
                ApiUtils.getOrderService(mDbHelper.defs.getUrl()).getDataPageableV1(
                        updateDate,
                        mDbHelper.defs.getDivision_code(),
                        SharedPreferenceManager.getInstance().getCurrentPageToLoad())
                        .enqueue(downloadDataCallback(updateDate));

                SharedPreferenceManager.getInstance().setLastUpdatedTimestamp();
                Log.e(TAG, "downloadData -> " + AppController.getInstance().getResourses().getString(R.string.downloaded_succesfully));
                if (notificationUtils != null)
                    DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                        notificationUtils.notify(AppController.getInstance(), AppController.getInstance().getResourses().getString(R.string.downloaded_succesfully), MY_CHANNEL_ID);
                    });
            } catch (Exception e) {
                Log.e(TAG, "downloadData -> " + AppController.getInstance().getResourses().getString(R.string.error_something_went_wrong));
                e.printStackTrace();
                if (notificationUtils != null)
                    DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                        notificationUtils.notify(AppController.getInstance(), AppController.getInstance().getResourses().getString(R.string.error_something_went_wrong), MY_CHANNEL_ID);
                    });
            }
        });
        return ;
    }
    private Callback<OrderOutDocBoxMovePart> downloadDataCallback(String updateDate) {
        return new Callback<OrderOutDocBoxMovePart>() {
            @Override
            public void onResponse(Call<OrderOutDocBoxMovePart> call, Response<OrderOutDocBoxMovePart> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Responce code: "+response.code());
                    if (response.code() == 204) {
                        //no content, so prepare environment to stop current request and prepare for next one
                        SharedPreferenceManager.getInstance().setNextPageToLoadToZero();
                        SharedPreferenceManager.getInstance().setUpdateDateNow();
                        return ;
                    }
                    if (response.code() != 200) return ;
                    //save order, boxes, boxMoves, partBox
                    if (response.body() != null &&
                            response.body().orderReqList != null &&
                            !response.body().orderReqList.isEmpty())
                        try {
                            Log.d(TAG, "saveToDB here.");
                            String dt = AppController.getInstance().getDbHelper().saveToDB(response.body());
                            if (dt == null) return;

                            SharedPreferenceManager.getInstance().setNextPageToLoadAsInc();
                            SharedPreferenceManager.getInstance().setUpdateDate(dt);
                            Log.d(TAG, "downloadDataCallback -> pageNumber: " + SharedPreferenceManager.getInstance().getCurrentPageToLoad());

                            if (SharedPreferenceManager.getInstance().getCurrentPageToLoad() != 0) {
                                ApiUtils.getOrderService(AppController.getInstance().getDbHelper().defs.getUrl()).getDataPageableV1(
                                        updateDate,
                                        AppController.getInstance().getDbHelper().defs.getDivision_code(),
                                        SharedPreferenceManager.getInstance().getCurrentPageToLoad())
                                            .enqueue(downloadDataCallback(updateDate));
                            }
                        } catch (RuntimeException re) {
                            Log.w(TAG, re);
                            SharedPreferenceManager.getInstance().setNextPageToLoadToZero();
                        }
                }
            }

            @Override
            public void onFailure(Call<OrderOutDocBoxMovePart> call, Throwable t) {
                Log.w(TAG, "downloadDataCallback -> API Request failed: " + t.getMessage());
                SharedPreferenceManager.getInstance().setNextPageToLoadToZero();
                if (notificationUtils != null)
                    DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                        notificationUtils.notify(AppController.getInstance(), AppController.getInstance().getResourses().getString(R.string.error_something_went_wrong), MY_CHANNEL_ID);
                    });
            }

        };
    }



    /**
     * fetch data from server and saves into local db
     * @param mDBHelper is an instance of DataBaseHelper
     */
    public void fetchAndSaveData(DataBaseHelper mDBHelper) {

        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            try {
                Log.i(TAG, "fetchAndSaveData -> update date: " + SharedPreferenceManager.getInstance().getUpdateDateString());
                ApiUtils.getOrderService(AppController.getInstance().getDbHelper().defs.getUrl()).getDataPageableV1(
                        SharedPreferenceManager.getInstance().getUpdateDateString(),
                        AppController.getInstance().getDbHelper().defs.getDivision_code(),
                        SharedPreferenceManager.getInstance().getCurrentPageToLoad())
                        .enqueue(new Callback<OrderOutDocBoxMovePart>() {
                            @Override
                            public void onResponse(Call<OrderOutDocBoxMovePart> call,
                                                   Response<OrderOutDocBoxMovePart> response) {
                                if (response.isSuccessful()) {
                                    if (response.code() == 204) {
                                        //no content, so prepare environment to stop current request and prepare for next one
                                        SharedPreferenceManager.getInstance().setNextPageToLoadToZero();
                                        SharedPreferenceManager.getInstance().setUpdateDateNow();
                                        return ;
                                    }
                                    if (response.code() != 200) return ;
                                    //save order, boxes, boxMoves, partBox
                                    if (response.body() != null &&
                                            response.body().orderReqList != null &&
                                            !response.body().orderReqList.isEmpty())
                                    try {
                                        mDBHelper.insertOrdersInBulk(response.body().orderReqList);

                                        if (response.body().outDocReqList != null &&
                                                !response.body().outDocReqList.isEmpty())
                                                    mDBHelper.insertOutDocInBulk(response.body().outDocReqList);

                                        if (response.body().boxReqList != null &&
                                                !response.body().boxReqList.isEmpty())
                                                    mDBHelper.insertBoxInBulk(response.body().boxReqList);

                                        if (response.body().movesReqList != null &&
                                                !response.body().movesReqList.isEmpty())
                                                    mDBHelper.insertBoxMoveInBulk(response.body().movesReqList);

                                        if (response.body().partBoxReqList != null &&
                                                !response.body().partBoxReqList.isEmpty())
                                                    mDBHelper.insertProdInBulk(response.body().partBoxReqList);

                                        SharedPreferenceManager.getInstance().setNextPageToLoadAsInc();
                                        //TODO callback here
                                    } catch (RuntimeException re) {
                                        Log.w(TAG, re);
                                        throw new RuntimeException("To catch onto method level.");
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<OrderOutDocBoxMovePart> call, Throwable t) {
                                Log.w(TAG, "fetchAndSaveData -> API Request failed: " + t.getMessage());
                            }

                        });
                //TODO here we should call next page download, but how ?
                SharedPreferenceManager.getInstance().setLastUpdatedTimestamp();

            } catch (Exception e) {
                e.printStackTrace();
                if (notificationUtils != null)
                    DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                        notificationUtils.notify(AppController.getInstance(), AppController.getInstance().getResourses().getString(R.string.error_something_went_wrong), MY_CHANNEL_ID);
                    });
            }
        });
    }

}
