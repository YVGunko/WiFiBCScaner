package com.example.yg.wifibcscaner.data.repo;

import android.support.annotation.NonNull;

public class BoxRepo {
    public static String makeBoxNumber(@NonNull String num) {
        StringBuilder sb = new StringBuilder();
        sb.append("№ кор: ");
        sb.append(num);
        sb.append(" ");
        return sb.toString();
    }

    public static String makeBoxDesc(@NonNull String num, @NonNull String q) {
        StringBuilder sb = new StringBuilder();
        sb.append("№ кор: ");
        sb.append(num);
        sb.append(", Принято: ");
        sb.append(q);
        return sb.toString();
    }
}
