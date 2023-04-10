package com.example.yg.wifibcscaner.utils;

import android.database.Cursor;

import java.util.List;

public class StringUtils {
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

    public static final char SINGLE_QUOTE = '\'';
    public static final char COMMA = ',';

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
        sb.append("№ ");
        sb.append(cursor.getString(0));
        sb.append(" / ");
        sb.append(cursor.getString(1));
        sb.append("\n");
        sb.append("Подошва: ");
        sb.append(cursor.getString(2));
        sb.append(". ");
        if ((cursor.getString(3) != null) && (!cursor.getString(3).equals(""))) {
            sb.append("Атрибут: ");
            sb.append(retStringFollowingCRIfNotNull(cursor.getString(3)));
        } else sb.append("\n");
        sb.append("Заказ: ");
        sb.append(cursor.getString(4));
        sb.append(". № кор: ");
        sb.append(cursor.getString(6));
        sb.append(". Регл: ");
        sb.append(cursor.getString(5));
        sb.append(" ");
        sb.append("В кор: ");
        sb.append(cursor.getString(7));
        sb.append(".");
        sb.append("\n");

        if ((cursor.getString(8) != null) && (
                !cursor.getString(8).equals("") || !cursor.getString(8).equals("Пусто"))) {
            sb.append(cursor.getString(8));
            sb.append(", ");
            sb.append(cursor.getString(9));
            sb.append("\n"); //Бригада
        }

        sb.append("Накл ");
        sb.append(cursor.getString(10));
        sb.append(" от ");
        sb.append(cursor.getString(11));
        return sb.toString();
    }
}
