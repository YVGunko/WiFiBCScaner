package com.example.yg.wifibcscaner.utils;

import java.util.List;

public class StringUtils {
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
}
