package com.example.yg.wifibcscaner.utils;

import android.database.Cursor;

public class DbUtils {
    public static void tryCloseCursor(Cursor c) {
        if (c != null && !c.isClosed()) {
            c.close();
        }
    }
}
