package com.example.yg.wifibcscaner.data.repository;

import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
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

    SQLiteDatabase db;

    FetchListDataListener fetchListDataListener;

    public void setFetchListDataListener(FetchListDataListener fetchListDataListener) {
        this.fetchListDataListener = fetchListDataListener;
    }
    interface DownLoadCallback{
        void callingBack();
    }

    DownLoadCallback dCallback;

    public void registerCallBack(DownLoadCallback callback){
        this.dCallback = callback;
    }
    /**
     * only this method should be used from UI
     * handles all success and fallback
     *
     * @param context
     */
    public void getData(Context context) {

        if (fetchListDataListener != null)
            fetchListDataListener.onLoading();

        if (!AppUtils.isNetworkAvailable(AppController.getInstance())) {
            if (fetchListDataListener != null)
                fetchListDataListener.onError(AppController.getInstance().getResourses().getString(R.string.error_connection), true);
            return;
        }

        if (!downloadData(context))
            if (fetchListDataListener != null)
                DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                    fetchListDataListener.onError(AppController.getInstance().getResourses().getString(R.string.error_something_went_wrong), true);
                });
            else if (fetchListDataListener != null)
                DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                    fetchListDataListener.onSuccess(AppController.getInstance().getResourses().getString(R.string.downloaded_succesfully));
                });
    }
    /**
     * run a download sequence
     * @param context
     */
    public boolean downloadData(Context context) {
        AtomicBoolean result = new AtomicBoolean(false);
        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            try {
                Log.i(TAG, "downloadData -> update date: " + SharedPreferenceManager.getInstance().getUpdateDateString());
                //TODO here we should call next page download, but how ?
                //if last download went right we should have zero as next page to download
                SharedPreferenceManager.getInstance().setNextPageToLoadAsInc();

                do {
                    downloadSinglePage(DataBaseHelper.getInstance(context));
                    Log.w(TAG, "downloadData -> "+SharedPreferenceManager.getInstance().getCurrentPageToLoad());
                }
                while (SharedPreferenceManager.getInstance().getCurrentPageToLoad() != 0);

                SharedPreferenceManager.getInstance().setLastUpdatedTimestamp();
                result.set(true);
            } catch (Exception e) {
                Log.w(TAG, "downloadData -> " + AppController.getInstance().getResourses().getString(R.string.error_something_went_wrong));
                e.printStackTrace();
            }
        });
        return result.get();
    }

    /**
     * fetch data from server and saves into local db
     * @param mDBHelper is an instance of DataBaseHelper
     */
    public void downloadSinglePage(DataBaseHelper mDBHelper) {
         try {
                Log.i(TAG, "singlePageDownload -> page number: " + SharedPreferenceManager.getInstance().getCurrentPageToLoad());
                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getDataPageableV1(
                        SharedPreferenceManager.getInstance().getUpdateDateString(),
                            mDBHelper.defs.getDivision_code(),
                                SharedPreferenceManager.getInstance().getCurrentPageToLoad())
                        .enqueue(new Callback<OrderOutDocBoxMovePart>() {
                            @Override
                            public void onResponse(Call<OrderOutDocBoxMovePart> call,
                                                   Response<OrderOutDocBoxMovePart> response) {
                                if (response.isSuccessful()) {
                                    if (response.code() == 204) {
                                        //no content, so prepare environment to stop current request and prepare for next one
                                        SharedPreferenceManager.getInstance().setNextPageToLoadToZero();
                                        SharedPreferenceManager.getInstance().setUpdateDateToday();
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
                                            SharedPreferenceManager.getInstance().setNextPageToLoadToZero();
                                        }
                                }
                            }

                            @Override
                            public void onFailure(Call<OrderOutDocBoxMovePart> call, Throwable t) {
                                Log.w(TAG, "fetchAndSaveData -> API Request failed: " + t.getMessage());
                                SharedPreferenceManager.getInstance().setNextPageToLoadToZero();
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
                SharedPreferenceManager.getInstance().setNextPageToLoadToZero();
            }
    }

    /**
     * fetch data from server and saves into local db
     * @param mDBHelper is an instance of DataBaseHelper
     */
    public void fetchAndSaveData(DataBaseHelper mDBHelper) {

        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            try {
                Log.i(TAG, "fetchAndSaveData -> update date: " + SharedPreferenceManager.getInstance().getUpdateDateString());
                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getDataPageableV1(
                        SharedPreferenceManager.getInstance().getUpdateDateString(),
                        mDBHelper.defs.getDivision_code(),SharedPreferenceManager.getInstance().getCurrentPageToLoad())
                        .enqueue(new Callback<OrderOutDocBoxMovePart>() {
                            @Override
                            public void onResponse(Call<OrderOutDocBoxMovePart> call,
                                                   Response<OrderOutDocBoxMovePart> response) {
                                if (response.isSuccessful()) {
                                    if (response.code() == 204) {
                                        //no content, so prepare environment to stop current request and prepare for next one
                                        SharedPreferenceManager.getInstance().setNextPageToLoadToZero();
                                        SharedPreferenceManager.getInstance().setUpdateDateToday();
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
                if (fetchListDataListener != null)
                    DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                        fetchListDataListener.onError(AppController.getInstance().getResourses().getString(R.string.error_something_went_wrong), true);
                    });
            }
        });
    }


    private boolean isLocalDataAvailable() {
        db = AppController.getInstance().getDbHelper().getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, "Boxes");
        db.close();
        return count > 0;
    }

    /**
     * get all publishers/authors name
     * @return list of publishers
     *//*
    public ArrayList<String> getAllPublishers() {
        ArrayList<String> authorList = new ArrayList<>();
        db = AppController.getInstance().getDbHelper().getReadableDatabase();
        Cursor c = db.query(true, ArticleContract.ArticleEntry.TABLE_NAME, new String[]{ArticleContract.ArticleEntry.COLUMN_NAME_AUTHOR}, null, null, ArticleContract.ArticleEntry.COLUMN_NAME_AUTHOR, null, null, null);
        while (c.moveToNext()) {
            String author = c.getString(c.getColumnIndexOrThrow(ArticleContract.ArticleEntry.COLUMN_NAME_AUTHOR));
            authorList.add(author);
        }
        c.close();
        return authorList;
    }*/

    /**
     * get data from local db
     *//*
    private void getArticlesFromDb() {

        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {

            db = AppController.getInstance().getArticleDbHelper().getReadableDatabase();

            String sortOrder = null;
            String selection = null;
            String[] selectionArgs = null;

            if (filterModel != null) {
                if (filterModel.isSortByDateAsc()) {
                    sortOrder = ArticleContract.ArticleEntry.COLUMN_NAME_PUBLISHED_AT + " ASC";
                } else {
                    sortOrder = ArticleContract.ArticleEntry.COLUMN_NAME_PUBLISHED_AT + " DESC";
                }
                if (!TextUtils.isEmpty(filterModel.getFilterByAuthor())) {
                    selection = ArticleContract.ArticleEntry.COLUMN_NAME_AUTHOR + "= ?";
                    selectionArgs = new String[]{filterModel.getFilterByAuthor()};
                }
            }

            Cursor c = db.query(
                    ArticleContract.ArticleEntry.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );

            mArticleList = new ArrayList<>();
            while (c.moveToNext()) {

                String author = c.getString(c.getColumnIndexOrThrow(ArticleContract.ArticleEntry.COLUMN_NAME_AUTHOR));
                String title = c.getString(c.getColumnIndexOrThrow(ArticleContract.ArticleEntry.COLUMN_NAME_TITLE));
                String description = c.getString(c.getColumnIndexOrThrow(ArticleContract.ArticleEntry.COLUMN_NAME_DESCRIPTION));
                String url = c.getString(c.getColumnIndexOrThrow(ArticleContract.ArticleEntry.COLUMN_NAME_URL));
                String urlToImage = c.getString(c.getColumnIndexOrThrow(ArticleContract.ArticleEntry.COLUMN_NAME_URL_TO_IMAGE));
                String publishedAt = DateTimeUtils.getFormattedDateTime(c.getLong(c.getColumnIndexOrThrow(ArticleContract.ArticleEntry.COLUMN_NAME_PUBLISHED_AT)));
                String content = c.getString(c.getColumnIndexOrThrow(ArticleContract.ArticleEntry.COLUMN_NAME_CONTENT));
                String sourceId = c.getString(c.getColumnIndexOrThrow(ArticleContract.ArticleEntry.COLUMN_NAME_SOURCE_ID));
                String sourceName = c.getString(c.getColumnIndexOrThrow(ArticleContract.ArticleEntry.COLUMN_NAME_SOURCE_NAME));

                Source source = new Source(sourceId, sourceName);
                Article article = new Article(author, title, description, url, urlToImage, publishedAt, content, source);

                mArticleList.add(article);
            }
            c.close();

            if (fetchListDataListener != null)
                DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                    fetchListDataListener.onSuccess(mArticleList);
                });

        });
    }*/

}
