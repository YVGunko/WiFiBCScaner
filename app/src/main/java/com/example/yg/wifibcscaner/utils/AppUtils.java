package com.example.yg.wifibcscaner.utils;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class AppUtils {
    /*
    *  @param bundle
   * @param key
   * @return
           */
    public static Long getLong(Bundle bundle, String key) {
        if (isEmpty(bundle, key)) {
            return null;
        } else {
            return bundle.getLong(key);
        }
    }

    /**
     * check whether a key is empty
     *
     * @param bundle
     * @param key
     * @return
     */
    public static boolean isEmpty(Bundle bundle, String key) {
        return bundle == null || !bundle.containsKey(key);
    }
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
        return StringUtils.isNotEmpty(string) || string.equals("Пусто") || string.equals("0");
    }
    public static boolean isEmpty(final String string) {
        return StringUtils.isBlank(string) & !string.equals("Пусто") & !string.equals("0");
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

    public static boolean isOneScanOnlyOper(final int key){
        final int[] array = {7, 9};
        return ArrayUtils.contains(array, key);
    }
}
