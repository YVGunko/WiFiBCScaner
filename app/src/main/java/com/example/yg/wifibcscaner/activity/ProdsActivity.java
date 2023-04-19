package com.example.yg.wifibcscaner.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.yg.wifibcscaner.DataBaseHelper;
import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.controller.AppController;

public class ProdsActivity extends AppCompatActivity {
    //Переменная для работы с БД
    private DataBaseHelper mDBHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prods);
        mDBHelper = AppController.getInstance().getDbHelper();

        String[] from = { "Ord", "Cust"};
        int[] to = { R.id.textView, R.id.textView2};

//Создаем адаптер
        SimpleAdapter adapter = new SimpleAdapter(this, mDBHelper.listprods(), R.layout.adapter_item, from, to);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
    }
}
