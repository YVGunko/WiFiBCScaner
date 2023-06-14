package com.example.yg.wifibcscaner;

import com.example.yg.wifibcscaner.utils.DateTimeUtils;

import org.junit.Test;

import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getStartOfYear;
import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
    @Test
    public void getStartOfYear_isCorrect() throws Exception {
        System.out.println(DateTimeUtils.getFirstDayOfYear());
        assertEquals("1672520400000", String.valueOf(DateTimeUtils.getFirstDayOfYear()));
    }
}