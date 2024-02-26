package com.example.yg.wifibcscaner.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.yg.wifibcscaner.DataBaseHelper;
import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.controller.AppController;

public class ProdsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prods);

        String[] from = { "Ord", "Cust"};
        int[] to = { R.id.textView, R.id.textView2};

        SimpleAdapter adapter = new SimpleAdapter(this, AppController.getInstance().getDbHelper().listprods(), R.layout.adapter_item, from, to);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
    }
}
