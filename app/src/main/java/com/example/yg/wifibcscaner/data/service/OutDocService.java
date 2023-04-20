package com.example.yg.wifibcscaner.data.service;

import android.support.annotation.NonNull;

public class OutDocService {
    public static String makeOutDocDesc(@NonNull String[] strings) {
        StringBuilder sb = new StringBuilder ();
        if (strings [0] != null) {
            if (strings.length > 1 && strings [0] != null && strings [1] != null) {
                sb.append(makeOutDocNumAndDateDesc(strings[0], strings[1]));
            }
            if (strings.length > 3 && strings [2] != null && strings [3] != null) {
                sb.append(makeOutDocBoxAndProdDesc(strings [2], strings [3]));
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
        sb.append("\nКор.: ");
        sb.append(box);
        sb.append(", Подошвы: ");
        sb.append(prod);
        return sb.toString ();
    }

    public static String makeOutDocDesc(String num, String dt, String selectOutDocById) {
        StringBuilder sb = new StringBuilder ();
        if (num != null && dt != null) {
            sb.append(makeOutDocNumAndDateDesc(num,dt));
            if (selectOutDocById != null) {
                sb.append(selectOutDocById);
            }
        }else{
            sb.append("Накладная не выбрана");
        }
        return sb.toString ();
    }
}
