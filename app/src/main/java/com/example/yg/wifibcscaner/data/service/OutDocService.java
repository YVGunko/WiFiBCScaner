package com.example.yg.wifibcscaner.data.service;

import android.support.annotation.NonNull;

public class OutDocService {
    public static String makeOutDocDesc(@NonNull String[] strings) {
        StringBuilder sb = new StringBuilder ();
        if (strings [0] != null) {
            sb.append("Нкл. ");
            sb.append(strings[0]);
            if (strings.length > 1 && strings [1] != null) {
                sb.append(" от ");
                sb.append(strings[1].substring(0, strings[1].indexOf(" ")));
            }
            if (strings.length > 2 && strings [2] != null) {
                sb.append(" Кор: ");
                sb.append(strings[2]);
            }
            if (strings.length > 3 && strings [3] != null) {
                sb.append(" Под: ");
                sb.append(strings[3]);
            }
        }else{
            sb.append("Накладная не выбрана");
        }
        return sb.toString ();
    }
    public static String makeOutDocNumAndDateDesc(@NonNull String num, @NonNull String dt) {
        StringBuilder sb = new StringBuilder ();
        sb.append("Нкл. ");
        sb.append(num);
        sb.append(" от ");
        sb.append(dt.substring(0, dt.indexOf(" ")));
        return sb.toString ();
    }
    public static String makeOutDocBoxAndProdDesc(@NonNull String box, @NonNull String prod) {
        StringBuilder sb = new StringBuilder ();
        sb.append("Кор. ");
        sb.append(box);
        sb.append(" Под. ");
        sb.append(prod);
        return sb.toString ();
    }
}
