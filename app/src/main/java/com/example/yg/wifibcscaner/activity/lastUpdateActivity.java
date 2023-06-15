package com.example.yg.wifibcscaner.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.utils.AppUtils;
import com.example.yg.wifibcscaner.utils.DateTimeUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getDateTimeLong;


public class lastUpdateActivity extends AppCompatActivity {
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

                Intent returnIntent = new Intent();
                returnIntent.putExtra("presetDate", getDateTimeLong(spDay+"."+ spMonth+"."+picker.getYear()+" 00:00:00"));
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }
}
