package com.example.yg.wifibcscaner.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.repo.OrderRepo;

import java.util.ArrayList;
import java.util.HashMap;


public class OrdersActivity extends AppCompatActivity {
    private final OrderRepo orderRepo = new OrderRepo();

    private class SyncIncoData extends AsyncTask<Void, Integer, ArrayList<HashMap<String, String>>> {
        TextView pbar;
        Integer counter;
        ArrayList<HashMap<String, String>> readOrders = new ArrayList<HashMap<String, String>>();

        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(Void... urls) {
            counter = 0;
            try {
                readOrders = orderRepo.listorders();
            } catch (Exception e) {
                Log.d("OrdersActivity","OrdersActivity.mDBHelper.listorders(): " + e.getMessage());
            }
            return readOrders;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbar = (TextView) findViewById(R.id.textView);
            pbar.setText("Подождите, данные загружаются...");
        }

        @Override
        protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
            String[] from = {"Ord", "Cust"};
            int[] to = {R.id.textView, R.id.textView2};
            SimpleAdapter adapter = new SimpleAdapter(OrdersActivity.this, result, R.layout.adapter_item, from, to);
            ListView listView = (ListView) findViewById(R.id.listView);
            listView.setAdapter(adapter);
            pbar = (TextView) findViewById(R.id.textView);
            pbar.setText("Заказ. Клиент.");
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        SyncIncoData task = new SyncIncoData();
        task.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.setTitle("Заказы. ");
    }
}
