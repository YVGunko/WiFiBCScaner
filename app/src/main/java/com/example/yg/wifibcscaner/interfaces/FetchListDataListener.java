package com.example.yg.wifibcscaner.interfaces;

public interface FetchListDataListener {

    void onLoading();

    void onSuccess(String scsMsg);

    void onError(String errMsg, boolean canRetry);

}
