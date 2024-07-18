package com.example.yg.wifibcscaner.activity;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.repo.DataLoadRepo;
import com.example.yg.wifibcscaner.data.repo.DepartmentRepo;
import com.example.yg.wifibcscaner.data.repo.DivisionRepo;
import com.example.yg.wifibcscaner.data.repo.OperRepo;
import com.example.yg.wifibcscaner.data.repo.SotrRepo;
import com.example.yg.wifibcscaner.data.repo.UserRepo;
import com.example.yg.wifibcscaner.service.MessageUtils;
import com.example.yg.wifibcscaner.utils.AppUtils;
import com.example.yg.wifibcscaner.utils.DateTimeUtils;

import java.time.LocalDate;


public class lastUpdateActivity extends AppCompatActivity {
    private static final String TAG = "sProject -> lastUpdateActivity";
    DatePicker picker;
    Button btnGet;
    TextView tvw;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_last_update);
        tvw=(TextView)findViewById(R.id.textView1);

        Bundle bundle = getIntent().getExtras();
        Long longDate = AppUtils.getLong(bundle, "presetDate");
        LocalDate date = DateTimeUtils.toLocalDate(longDate);
        picker=(DatePicker)findViewById(R.id.datePicker1);
        picker.updateDate(date.getYear(), date.getMonth().getValue()-1, date.getDayOfMonth());

        btnGet=(Button)findViewById(R.id.button1);
        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String spMonth = "";
                    String spDay = "";

                    if (String.format("%d", picker.getDayOfMonth()).length()==1) {
                        spDay = "0"+String.format("%d", picker.getDayOfMonth());
                    }else{
                        spDay = String.format("%d", picker.getDayOfMonth());
                    };
                    if (String.format("%d", (picker.getMonth() + 1)).length()==1) {
                        spMonth = "0"+String.format("%d", (picker.getMonth() + 1));
                    }else{
                        spMonth = String.format("%d", (picker.getMonth() + 1));
                    };
                    tvw.setText("Выбрана дата: "+ spDay+"."+ spMonth+"."+picker.getYear());

                    AppController.getInstance().setGlobalUpdateDate(spDay+"."+ spMonth+"."+picker.getYear()+" 00:00:00");
                    MessageUtils.showToast("Начата синхронизация данных с даты "+spDay+"."+ spMonth+"."+picker.getYear(), true);

                    DataLoadRepo dataLoadRepo = new DataLoadRepo();
                    dataLoadRepo.loadData();

                    UserRepo userRepo = new UserRepo();
                    userRepo.downloadUser();

                    final DivisionRepo divRepo = new DivisionRepo();
                    divRepo.downloadDivision();

                    final DepartmentRepo depRepo = new DepartmentRepo();
                    depRepo.downloadDepartment();

                    final OperRepo operRepo = new OperRepo();
                    operRepo.downloadOperation();

                    final SotrRepo sotrRepo = new SotrRepo();
                    sotrRepo.downloadSotr();
                } catch (Exception e) {
                    Log.e(TAG, "Ответ сервера на запрос новых заказов: " + e.getMessage());
                } finally {
                    finish();
                }
            }
        });
    }
}
