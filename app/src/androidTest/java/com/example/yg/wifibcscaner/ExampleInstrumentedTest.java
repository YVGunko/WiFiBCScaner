package com.example.yg.wifibcscaner;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.repo.BoxRepo;
import com.example.yg.wifibcscaner.data.repo.OrderRepo;
import com.example.yg.wifibcscaner.service.foundBox;
import com.example.yg.wifibcscaner.service.foundOrder;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.example.yg.wifibcscaner", appContext.getPackageName());
    }
    @Test
    public void testCheckAvailability() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        DataBaseHelper mDBHelper = AppController.getInstance().getDbHelper();
        AppController.getInstance().getDbHelper().openDataBase();
        assertEquals(7, mDBHelper.checkAvailability("S40155-0016").size());

        AppController.getInstance().getDbHelper().openDataBase();
        assertEquals(true, mDBHelper.checkAvailability("fo.getOrd()").isEmpty());

        final OrderRepo orderRepo = new OrderRepo();
        final BoxRepo boxRepo = new BoxRepo();
        final String barcode = "S40155-0016.30.12.360.1";
        final String prefix = "S";

        foundOrder fo = orderRepo.searchOrder(barcode, prefix);
        assertEquals(true, fo.getOrd().equals("S40155-0016"));

        foundBox fb = boxRepo.searchBox(fo.get_id(), barcode);
        AppController.getInstance().getDbHelper().openDataBase();

        assertEquals(true, mDBHelper.addBox(fo, fb,
                "6a5d8550-46ad-42e0-aa21-cc731c7ff994"));
    }
}
