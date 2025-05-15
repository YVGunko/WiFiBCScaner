package com.example.yg.wifibcscaner.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.repo.DataSendRepo;

import java.util.Date;

public class ProdsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prods);

        String[] from = { "Ord", "Cust"};
        int[] to = { R.id.textView, R.id.textView2};

        SimpleAdapter adapter = new SimpleAdapter(this, AppController.getInstance().getDbHelper().lastShift(), R.layout.adapter_item, from, to);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.boxes_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Date dt;
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();
        // Операции для выбранного пункта меню
        switch (id) {
            case R.id.action_sendboxes:
                DataSendRepo dsRepo = new DataSendRepo();
                dsRepo.sendData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
