package com.example.yg.wifibcscaner.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import com.example.yg.wifibcscaner.data.repo.DataLoadRepo;
import com.example.yg.wifibcscaner.data.repo.OrderRepo;

public class MyJobService extends JobService {

    private final OrderRepo orderRepo = new OrderRepo();

    private static final String TAG = "sProject -> ".concat(MyJobService.class.getSimpleName());
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob() was called");

        //выбрать максимальную дату загрузки заказа из MasterData. Запросить все заказы старше этой даты но только за месяц.
        DataLoadRepo dataLoadRepo = new DataLoadRepo();
        dataLoadRepo.loadData();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob() was called");
        return true;
    }
}
