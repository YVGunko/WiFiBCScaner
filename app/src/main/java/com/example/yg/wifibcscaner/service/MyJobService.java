package com.example.yg.wifibcscaner.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.yg.wifibcscaner.data.repo.DataLoadRepo;
import com.example.yg.wifibcscaner.data.repo.DepartmentRepo;
import com.example.yg.wifibcscaner.data.repo.DivisionRepo;
import com.example.yg.wifibcscaner.data.repo.SotrRepo;
import com.example.yg.wifibcscaner.data.repo.UserRepo;
import com.example.yg.wifibcscaner.data.repo.OperRepo;

public class MyJobService extends JobService {

    private static final String TAG = "sProject -> ".concat(MyJobService.class.getSimpleName());
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob() was called");

        DataLoadRepo dataLoadRepo = new DataLoadRepo();
        dataLoadRepo.loadData(() -> {
            Log.d(TAG, "Job is complete, calling jobFinished");
            jobFinished(params, false); // ✅ Signal job completion
        });
        DivisionRepo divRepo = new DivisionRepo();
        divRepo.downloadDivision();
        OperRepo operRepo = new OperRepo();
        operRepo.downloadOperation();
        DepartmentRepo depRepo = new DepartmentRepo();
        depRepo.downloadDepartment();
        SotrRepo sotrRepo = new SotrRepo();
        sotrRepo.downloadSotr();
        UserRepo userRepo = new UserRepo();
        userRepo.downloadUser();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob() was called");
        return true;
    }
}
