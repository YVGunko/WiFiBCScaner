package com.example.yg.wifibcscaner.data.repository;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.yg.wifibcscaner.DataBaseHelper;
import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.dto.OrderOutDocBoxMovePart;
import com.example.yg.wifibcscaner.data.model.Deps;
import com.example.yg.wifibcscaner.data.model.Division;
import com.example.yg.wifibcscaner.data.model.Operation;
import com.example.yg.wifibcscaner.data.model.Sotr;
import com.example.yg.wifibcscaner.data.model.user;
import com.example.yg.wifibcscaner.utils.ApiUtils;
import com.example.yg.wifibcscaner.utils.AppUtils;
import com.example.yg.wifibcscaner.utils.NotificationUtils;
import com.example.yg.wifibcscaner.utils.SharedPreferenceManager;
import com.example.yg.wifibcscaner.utils.executors.DefaultExecutorSupplier;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BaseClassRepo {
    private static final String TAG = "baseClassRepo";
    private static final String MY_CHANNEL_ID = "Download Status";

    NotificationUtils notificationUtils;

    public void setNotificationUtils(NotificationUtils notificationUtils) {
        this.notificationUtils = notificationUtils;
    }
    /**
     * run a download sequence
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
                            AppController.getInstance().getResourses().getString(R.string.network_unawailable),
                            MY_CHANNEL_ID);
                });
            return;
        }

        downloadDivision();
        downloadDeps(context);
        downloadOperation(context);
        downloadUser(context);
        downloadSotr(context);
    }

    private void downloadDivision() {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            try {
                DataBaseHelper mDbHelper = AppController.getInstance().getDbHelper();

                ApiUtils.getOrderService(mDbHelper.defs.getUrl()).getDiv().enqueue(new Callback<List<Division>>() {
                    @Override
                    public void onResponse(Call<List<Division>> call, Response<List<Division>> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null &&
                                    !response.body().isEmpty())
                                mDbHelper.insertDivisionInBulk(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Division>> call, Throwable t) {
                        Log.w(TAG, "baseClassRepo -> downloadData -> Division -> " + t.getMessage());
                    }
                });

                Log.d(TAG, "baseClassRepo -> downloadData -> Division -> " + AppController.getInstance().getResourses().getString(R.string.deps_downloaded_succesfully));
                if (notificationUtils != null)
                    DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                        notificationUtils.notify(AppController.getInstance(), AppController.getInstance().getResourses().getString(R.string.deps_downloaded_succesfully), MY_CHANNEL_ID);
                    });
            } catch (Exception e) {
                Log.e(TAG, "baseClassRepo -> downloadData ->  Division -> " + AppController.getInstance().getResourses().getString(R.string.error_something_went_wrong));
                e.printStackTrace();
                if (notificationUtils != null)
                    DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                        notificationUtils.notify(AppController.getInstance(), AppController.getInstance().getResourses().getString(R.string.error_something_went_wrong), MY_CHANNEL_ID);
                    });
            }
        });
        return ;
    }

    /**
     * run a download sequence
     * @param context
     */
    private void downloadDeps(Context context) {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            try {
                DataBaseHelper mDbHelper = AppController.getInstance().getDbHelper();

                ApiUtils.getOrderService(mDbHelper.defs.getUrl()).getDeps("01.01.2018 00:00:00").enqueue(new Callback<List<Deps>>() {
                    // TODO Обработать результат. Записать поле sent... если успешно
                    @Override
                    public void onResponse(Call<List<Deps>> call, Response<List<Deps>> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null &&
                                    !response.body().isEmpty())
                                mDbHelper.insertDepsInBulk(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Deps>> call, Throwable t) {
                        Log.w(TAG, "baseClassRepo -> downloadData -> Deps -> " + t.getMessage());
                    }
                });

                Log.d(TAG, "baseClassRepo -> downloadData -> Deps -> " + AppController.getInstance().getResourses().getString(R.string.deps_downloaded_succesfully));
                if (notificationUtils != null)
                    DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                        notificationUtils.notify(AppController.getInstance(), AppController.getInstance().getResourses().getString(R.string.deps_downloaded_succesfully), MY_CHANNEL_ID);
                    });
            } catch (Exception e) {
                Log.e(TAG, "baseClassRepo -> downloadData ->  Deps -> " + AppController.getInstance().getResourses().getString(R.string.error_something_went_wrong));
                e.printStackTrace();
                if (notificationUtils != null)
                    DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                        notificationUtils.notify(AppController.getInstance(), AppController.getInstance().getResourses().getString(R.string.error_something_went_wrong), MY_CHANNEL_ID);
                    });
            }
        });
        return ;
    }

    /**
     * run a download sequence
     * @param context
     */
    private void downloadUser(Context context) {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            try {
                DataBaseHelper mDbHelper = AppController.getInstance().getDbHelper();

                ApiUtils.getOrderService(mDbHelper.defs.getUrl()).getUser("01.01.2018 00:00:00").enqueue(new Callback<List<user>>() {
                    // TODO Обработать результат. Записать поле sent... если успешно
                    @Override
                    public void onResponse(Call<List<user>> call, Response<List<user>> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null &&
                                    !response.body().isEmpty())
                                mDbHelper.insertUserInBulk(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<user>> call, Throwable t) {
                        Log.w(TAG, "baseClassRepo -> downloadData -> User -> " + t.getMessage());
                    }
                });

                Log.d(TAG, "baseClassRepo -> downloadData -> User -> " + AppController.getInstance().getResourses().getString(R.string.users_downloaded_succesfully));
                if (notificationUtils != null)
                    DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                        notificationUtils.notify(AppController.getInstance(), AppController.getInstance().getResourses().getString(R.string.users_downloaded_succesfully), MY_CHANNEL_ID);
                    });
            } catch (Exception e) {
                Log.e(TAG, "baseClassRepo -> downloadData ->  User -> " + AppController.getInstance().getResourses().getString(R.string.error_something_went_wrong));
                e.printStackTrace();
                if (notificationUtils != null)
                    DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                        notificationUtils.notify(AppController.getInstance(), AppController.getInstance().getResourses().getString(R.string.error_something_went_wrong), MY_CHANNEL_ID);
                    });
            }
        });
        return ;
    }
    private void downloadOperation(Context context) {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            try {
                DataBaseHelper mDbHelper = AppController.getInstance().getDbHelper();

                ApiUtils.getOrderService(mDbHelper.defs.getUrl()).getOperation("01.01.2018 00:00:00").enqueue(new Callback<List<Operation>>() {
                    // TODO Обработать результат. Записать поле sent... если успешно
                    @Override
                    public void onResponse(Call<List<Operation>> call, Response<List<Operation>> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null &&
                                    !response.body().isEmpty())
                                mDbHelper.insertOperationInBulk(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Operation>> call, Throwable t) {
                        Log.w(TAG, "baseClassRepo -> downloadData -> Operation -> " + t.getMessage());
                    }
                });

                Log.d(TAG, "baseClassRepo -> downloadData -> Operation -> " + AppController.getInstance().getResourses().getString(R.string.opers_downloaded_succesfully));
                if (notificationUtils != null)
                    DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                        notificationUtils.notify(AppController.getInstance(), AppController.getInstance().getResourses().getString(R.string.opers_downloaded_succesfully), MY_CHANNEL_ID);
                    });
            } catch (Exception e) {
                Log.e(TAG, "baseClassRepo -> downloadData ->  Operation -> " + AppController.getInstance().getResourses().getString(R.string.error_something_went_wrong));
                e.printStackTrace();
                if (notificationUtils != null)
                    DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                        notificationUtils.notify(AppController.getInstance(), AppController.getInstance().getResourses().getString(R.string.error_something_went_wrong), MY_CHANNEL_ID);
                    });
            }
        });
        return ;
    }

    private void downloadSotr(Context context) {
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            try {
                DataBaseHelper mDbHelper = AppController.getInstance().getDbHelper();

                ApiUtils.getOrderService(mDbHelper.defs.getUrl()).getSotr("01.01.2018 00:00:00").enqueue(new Callback<List<Sotr>>() {
                    // TODO Обработать результат. Записать поле sent... если успешно
                    @Override
                    public void onResponse(Call<List<Sotr>> call, Response<List<Sotr>> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null &&
                                    !response.body().isEmpty())
                                mDbHelper.insertSotrInBulk(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Sotr>> call, Throwable t) {
                        Log.w(TAG, "baseClassRepo -> downloadData -> Sotr -> " + t.getMessage());
                    }
                });

                Log.d(TAG, "baseClassRepo -> downloadData -> Sotr -> " + AppController.getInstance().getResourses().getString(R.string.sotr_downloaded_succesfully));
                if (notificationUtils != null)
                    DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                        notificationUtils.notify(AppController.getInstance(), AppController.getInstance().getResourses().getString(R.string.sotr_downloaded_succesfully), MY_CHANNEL_ID);
                    });
            } catch (Exception e) {
                Log.e(TAG, "baseClassRepo -> downloadData ->  Sotr -> " + AppController.getInstance().getResourses().getString(R.string.error_something_went_wrong));
                e.printStackTrace();
                if (notificationUtils != null)
                    DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                        notificationUtils.notify(AppController.getInstance(), AppController.getInstance().getResourses().getString(R.string.error_something_went_wrong), MY_CHANNEL_ID);
                    });
            }
        });
        return ;
    }
}
