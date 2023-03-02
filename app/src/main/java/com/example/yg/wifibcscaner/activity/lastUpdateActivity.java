package com.example.yg.wifibcscaner.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.example.yg.wifibcscaner.DataBaseHelper;
import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.utils.SharedPreferenceManager;


public class lastUpdateActivity extends AppCompatActivity {
    private DataBaseHelper mDBHelper;
    DatePicker picker;
    Button btnGet;
    TextView tvw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_update);
        mDBHelper = DataBaseHelper.getInstance(this);
        tvw=(TextView)findViewById(R.id.textView1);
        picker=(DatePicker)findViewById(R.id.datePicker1);
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
                mDBHelper.globalUpdateDate = spDay+"."+ spMonth+"."+picker.getYear()+" 00:00:00";
                SharedPreferenceManager.getInstance().setUpdateDate(spDay+"."+ spMonth+"."+picker.getYear()+" 00:00:00");
            }
        });
    }
}
