package com.example.yg.wifibcscaner.interfaces;

import com.example.yg.wifibcscaner.data.dto.OrderOutDocBoxMovePart;

public interface FetchListDataListener {

    void onLoading();

    void onSuccess(OrderOutDocBoxMovePart data);

    void onError(String errMsg, boolean canRetry);

    void onErrorPrompt(String errMsg);

}
