package com.example.yg.wifibcscaner.utils;

import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yg.wifibcscaner.data.repo.BoxRepo;

import java.util.List;
import java.util.UUID;

public class MyStringUtils {
    private static final String TAG = "StringUtils";
    public static final char SINGLE_QUOTE = '\'';
    public static final char COMMA = ',';

    public static String getUUID() {
        // Creating a random UUID (Universally unique identifier).
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
    public static String filter(String str){
        StringBuilder filtered = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char current = str.charAt(i);
            if (current >= 0x2E && current <= 0x39) {
                filtered.append(current);
            }
        }
        return filtered.toString();
    }

    public static String retStringFollowingCRIfNotNull (String s){
        String retString = "";
        try {
            if (!(s==null || s.equals("")))
                retString = s+ "\n" ;
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        return retString;
    }

    public static String toSqlInString(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(SINGLE_QUOTE);
            sb.append(s);
            sb.append(SINGLE_QUOTE);
            if (list.indexOf(s) != (list.size() - 1)) {
                sb.append(COMMA);
            }
        }
        return sb.toString();
    }

    public static String makeLastBoxDef(Cursor cursor) {
        StringBuilder sb = new StringBuilder();
        try {

            String[] order = {cursor.getString(0), cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), cursor.getString(4), cursor.getString(6)};

            sb.append(makeOrderDesc(order));

            sb.append(BoxRepo.makeBoxNumber(cursor.getString(8)));

            sb.append("Всего: ");
            sb.append(cursor.getString(7));
            sb.append(" ");
            sb.append("В кор.: ");
            sb.append(cursor.getString(9));
            sb.append(".");
            sb.append("\n");

            if (cursor.getInt(10) != 0) {
                sb.append(makeSotrDesc(new String[]{cursor.getString(11), cursor.getString(13)}));
            }
            return sb.toString();
        } catch (
                CursorIndexOutOfBoundsException e) {
            Log.e(TAG, "makeLastBoxDef -> ", e);
            return sb.toString();
        }
    }


    public static String makeSotrDesc(@NonNull String[] strings) {
        StringBuilder sb = new StringBuilder ();
        sb.append ("Сотрудник: ");
        if (strings.length > 0 && strings [0] != null) {
            sb.append(strings[0].substring(0, strings[0].indexOf(" ")));
        }
        if (strings.length > 1 && strings [1] != null) {
            sb.append(", ");
            sb.append(strings[1]);
        }

        return sb.toString ();
    }
    public static String makeOrderDesc(@NonNull String[] strings) {
        StringBuilder sb = new StringBuilder ();
        sb.append ("№");
        if (strings.length > 0 && strings [0] != null) {
            sb.append(strings[0]);
        }
        if (strings.length > 1 && strings [1] != null) {
            sb.append(", ");
            sb.append(strings[1]); //customer
        }
        if (strings.length > 2 && strings [2] != null) {
            sb.append("\n");
            sb.append("Подошва: ");
            sb.append(strings [2]);//nomenklature
        }

        if (strings.length > 3 && strings [3] != null && !strings [3].equals("")) {
            sb.append(", ");
            sb.append(retStringFollowingCRIfNotNull(strings [3]));//attribut
        } else sb.append("\n");

        if (strings.length > 4 && strings [4] != null) {
            sb.append("Всего пар: ");
            sb.append(strings [4]);// total ordered number of nomeklature - Q_ord
        }
        if (strings.length > 5 && strings [5] != null) {
            sb.append(", Всего кор: ");
            sb.append(strings [5]);// total number of boxes - Q_box
            sb.append("\n");
        }

        return sb.toString ();
    }
    public static String makeUserDesc(@NonNull String name) {
        StringBuilder sb = new StringBuilder();
        sb.append("Пользователь: ");

        if (name != null) {
            sb.append(name);
        }else{
            sb.append(" не установлен");
        }
        return sb.toString ();
    }
/* order definition
* */
    public static String makeOrderdef(Cursor cursor) {
        String def = "№ " + cursor.getString(2);
        def += " / " + cursor.getString(3) + "\n";
        def += "Подошва: " + cursor.getString(4) ;
        if (AppUtils.isNotEmpty(cursor.getString(5)))
            def += ", " + cursor.getString(5);
        def += "\nЗаказ: " + cursor.getString(6) +
                ". Регл: " + cursor.getString(7) +
                ". Всего кор: " + cursor.getString(8) + "\n";
        return def;
    }
}
