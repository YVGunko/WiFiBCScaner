package com.example.yg.wifibcscaner.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.repo.OrderOutDocBoxMovePartRepository;
import com.example.yg.wifibcscaner.data.repo.OrderRepo;
import com.example.yg.wifibcscaner.utils.DateTimeUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class MyJobService extends JobService {

    private final OrderRepo orderRepo = new OrderRepo();

    private static final String TAG = "sProject -> ".concat(MyJobService.class.getSimpleName());
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob() was called");
        Long ldtMin = DateTimeUtils.getStartOfDayLong(DateTimeUtils.addDays(new Date(), -DateTimeUtils.numberOfDaysInMonth(new Date())));
        String dtMin = DateTimeUtils.getStartOfDayString(ldtMin);
        //выбрать максимальную дату загрузки заказа из MasterData. Запросить все заказы старше этой даты но только за месяц.
        OrderOutDocBoxMovePartRepository orderOutDocBoxMovePartRepository = new OrderOutDocBoxMovePartRepository();
        orderOutDocBoxMovePartRepository.downloadData(StringUtils.isNotBlank(AppController.getInstance().getGlobalUpdateDate())
                ? AppController.getInstance().getGlobalUpdateDate()
                : orderRepo.getOrderUpdateDate(dtMin));

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob() was called");
        return true;
    }
}
