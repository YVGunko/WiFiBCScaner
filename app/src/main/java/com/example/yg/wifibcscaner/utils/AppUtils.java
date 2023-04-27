package com.example.yg.wifibcscaner.utils;

import android.database.Cursor;
import android.os.Build;
import android.support.annotation.RequiresApi;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class AppUtils {
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static boolean contains(final int[] arr, final int key) {
        return Arrays.stream(arr).anyMatch(i -> i == key);
    }

    public static boolean isOutDocOnlyOper(final int key) {
        final int[] array = {7, 9, 9999};
        return ArrayUtils.contains(array, key);
    }

    public static boolean isDepAndSotrOper(final int key) {
        final int[] array = {7, 9, 9999};
        return !ArrayUtils.contains(array, key);
    }

    public static boolean isOneOfFirstOper(final int key) {
        final int[] array = {1, 7};
        return ArrayUtils.contains(array, key);
    }

    public static boolean isNotEmpty(final String string) {
        return StringUtils.isNotEmpty(string) & !string.equals("Пусто") & !string.equals("0");
    }

    public static int getFirstOperFor(final int key) {
        return (key == 9) ? 7 : 1;
    }

    public static boolean isIncomeOper(final int key){
        final int[] array = {7};
        return ArrayUtils.contains(array, key);
    }

    public static boolean isOutComeOper(final int key){
        final int[] array = {9};
        return ArrayUtils.contains(array, key);
    }
    /*private String makeBoxdef(Cursor cursor) {
        String def = "№ " + cursor.getString(0);
        def += " / " + cursor.getString(1) + "\n";
        def += "Подошва: " + cursor.getString(2) + ", ";
        if (AppUtils.isNotEmpty(cursor.getString(3)))
            def += ", " + retStringFollowingCRIfNotNull(cursor.getString(3));
        else def += "\n";
        def += "Заказ: " + cursor.getString(4) + ". № кор: " + cursor.getString(6) + ". Регл: " + cursor.getString(5) + " ";
        def += "В кор: " + cursor.getString(7) + ". " + cursor.getString(8)+", " + cursor.getString(9);
        return def;
    }*/
}
