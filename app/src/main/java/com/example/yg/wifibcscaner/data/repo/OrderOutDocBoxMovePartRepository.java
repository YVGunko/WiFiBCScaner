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
                nextPage.set(0);
                Log.d(TAG, "downloadData -> update date: " + updateDate);
                Log.d(TAG, "downloadData -> current page to load: " + nextPage);

                ApiUtils.getOrderService(AppController.getInstance().getDbHelper().defs.getUrl()).getDataPageableV1(
                        updateDate,
                        AppController.getInstance().getDbHelper().defs.getDivision_code(),
                        AppController.getInstance().getDbHelper().defs.get_Id_o(),
                        nextPage.getAndIncrement(),
                        pageSize)
                        .enqueue(downloadDataCallback(updateDate));

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

    private Callback<OrderOutDocBoxMovePart> downloadDataCallback(String updateDate) {
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
                            String dt = AppController.getInstance().getDbHelper().saveToDB(response.body());
                            if (dt == null) return;

                            Log.d(TAG, "downloadDataCallback -> pageNumber: " + nextPage.get());

                            if (nextPage.get() != 0) {
                                ApiUtils.getOrderService(AppController.getInstance().getDbHelper().defs.getUrl()).getDataPageableV1(
                                        updateDate,
                                        AppController.getInstance().getDbHelper().defs.getDivision_code(),
                                        AppController.getInstance().getDbHelper().defs.get_Id_o(),
                                        nextPage.getAndIncrement(),
                                        pageSize)
                                        .enqueue(downloadDataCallback(updateDate));
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
}
