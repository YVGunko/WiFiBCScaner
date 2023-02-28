package com.example.yg.wifibcscaner.data.repository;

import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.Date;

import com.example.yg.wifibcscaner.DataBaseHelper;
import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.dto.OrderOutDocBoxMovePart;
import com.example.yg.wifibcscaner.interfaces.FetchListDataListener;
import com.example.yg.wifibcscaner.utils.ApiUtils;
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

    //private DataBaseHelper mDBHelper;

    FetchListDataListener fetchListDataListener;

    private OrderOutDocBoxMovePart mData;

    public void setFetchListDataListener(FetchListDataListener fetchListDataListener) {
        this.fetchListDataListener = fetchListDataListener;
    }

    /**
     * only this method should be used from UI
     * handles all success and fallback
     *
     * @param filterModel
     * @param mustFetchNewData
     *//*
    public void getData(boolean mustFetchNewData) {

        if (fetchListDataListener != null)
            fetchListDataListener.onLoading();

        if (!mustFetchNewData) {
            mustFetchNewData = SharedPreferenceManager.getInstance().isLocalDataExpired();
        }

        boolean isLocalDataAvailable = isLocalDataAvailable();

        if (isLocalDataAvailable && !mustFetchNewData) {
            getArticlesFromDb();
        }

        if (!AppUtils.isNetworkAvailable(AppController.getInstance())) {
            if (isLocalDataAvailable) {
                if (fetchListDataListener != null)
                    fetchListDataListener.onErrorPrompt(AppController.getInstance().getResourses().getString(R.string.error_connection));
            } else {
                if (fetchListDataListener != null)
                    fetchListDataListener.onError(AppController.getInstance().getResourses().getString(R.string.error_connection), true);
            }
            return;
        }

        if (!isLocalDataAvailable || mustFetchNewData)
            fetchAndSaveData(true);
    }*/


    /**
     * fetch data from server and saves into local db
     * param returnData flag to return data
     */
    public void fetchAndSaveData(DataBaseHelper mDBHelper) {

        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            String strUpdateDate ;

            if (!mDBHelper.globalUpdateDate.equals("")) {
                strUpdateDate = mDBHelper.globalUpdateDate;
            } else {
                strUpdateDate = mDBHelper.getMaxOrderDate();
            }
            Log.i(TAG, "fetchAndSaveData -> update date: " + strUpdateDate);
            try {
                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getDataPageableV1(
                        strUpdateDate,
                        mDBHelper.defs.getDivision_code(),0)
                        .enqueue(new Callback<OrderOutDocBoxMovePart>() {
                            @Override
                            public void onResponse(Call<OrderOutDocBoxMovePart> call,
                                                   Response<OrderOutDocBoxMovePart> response) {
                                if (response.isSuccessful()) {
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

                                        SharedPreferenceManager.getInstance().setNextPageTimestamp();
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
