package com.example.yg.wifibcscaner.data.repo;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.yg.wifibcscaner.DataBaseHelper;
import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.dto.OrderOutDocBoxMovePart;
import com.example.yg.wifibcscaner.service.ApiUtils;
import com.example.yg.wifibcscaner.service.MessageUtils;
import com.example.yg.wifibcscaner.service.SharedPrefs;
import com.example.yg.wifibcscaner.utils.SharedPrefs;
import com.example.yg.wifibcscaner.utils.executors.DefaultExecutorSupplier;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderOutDocBoxMovePartRepository {
    private Context mContext;
    private AtomicInteger nextPage;
    private static int pageSize = 200;
    private static final String TAG = "orderAndStuffRepo";


    public void downloadData(String updateDate) {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            try {
                Log.d(TAG, "downloadData -> update date: " + updateDate);
                Log.d(TAG, "downloadData -> current page to load: " + nextPage);

                DataBaseHelper mDbHelper = DataBaseHelper.getInstance(mContext);
                final String url = mDbHelper.defs.getUrl();
                final String division_code = mDbHelper.defs.getDivision_code();
                final long operationId = mDbHelper.defs.get_Id_o();
                nextPage.set(0);

                ApiUtils.getOrderService(url).getDataPageableV1(
                        updateDate,
                        division_code,
                        operationId,
                        nextPage.getAndIncrement(),
                        pageSize)
                        .enqueue(downloadDataCallback(updateDate, url, division_code, operationId));

            } catch (Exception e) {
                Log.e(TAG, "downloadData -> " + R.string.error_something_went_wrong);
                e.printStackTrace();
                DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                    MessageUtils.showToast(mContext, "Ошибка. downloadData. "+R.string.error_something_went_wrong, true);
                });
            }
        });
        return;
    }

    private Callback<OrderOutDocBoxMovePart> downloadDataCallback(String updateDate, String url,) {
        return new Callback<OrderOutDocBoxMovePart>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call<OrderOutDocBoxMovePart> call, Response<OrderOutDocBoxMovePart> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Responce code: " + response.code());
                    if (response.code() == 204) {
                        //no content, so prepare environment to stop current request and prepare for next one
                        nextPage.set(0);
                        DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                            MessageUtils.showToast(mContext, "Завершено. Responce code = 204", true);
                        });
                        return;
                    }
                    if (response.code() != 200) return;
                    //save order, boxes, boxMoves, partBox
                    if (response.body() != null &&
                            response.body().orderReqList != null &&
                            !response.body().orderReqList.isEmpty())
                        try {
                            Log.d(TAG, "saveToDB here.");
                            String dt = mDbHelper.saveToDB(response.body());
                            if (dt == null) return;

                            Log.d(TAG, "downloadDataCallback -> pageNumber: " + nextPage.get());

                            if (nextPage.get() != 0) {
                                ApiUtils.getOrderService(mDbHelper.defs.getUrl()).getDataPageableV1(
                                        updateDate,
                                        mDbHelper.defs.getDivision_code(),
                                        mDbHelper.defs.get_Id_o(),
                                        nextPage.getAndIncrement(), pageSize)
                                        .enqueue(downloadDataCallback(updateDate, mDbHelper));
                            }
                        } catch (RuntimeException re) {
                            Log.w(TAG, re);
                            nextPage.set(0);
                        }
                }
            }

            @Override
            public void onFailure(Call<OrderOutDocBoxMovePart> call, Throwable t) {
                Log.w(TAG, "downloadDataCallback -> API Request failed: " + t.getMessage());
                nextPage.set(0);
                DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                    MessageUtils.showToast(mContext, "Ошибка. downloadDataCallback. onFailure", true);
                });
            }

        };
    }


    /**
     * fetch data from server and saves into local db
     *
     * @param mDBHelper is an instance of DataBaseHelper
     */
    public void fetchAndSaveData(DataBaseHelper mDBHelper) {

        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            try {
                Log.i(TAG, "fetchAndSaveData -> update date: " + SharedPrefs.getInstance().getUpdateDateString());
                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getDataPageableV1(
                        SharedPreferenceManager.getInstance().getUpdateDateString(),
                        mHelper().defs.getDivision_code(),
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
                                        return;
                                    }
                                    if (response.code() != 200) return;
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
