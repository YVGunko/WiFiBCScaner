package com.example.yg.wifibcscaner.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.yg.wifibcscaner.DataBaseHelper;
import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.activity.lastUpdateActivity;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.service.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class dbService extends AppCompatActivity {
    private DataBaseHelper mDBHelper = AppController.getInstance().getDbHelper();
    ProgressBar pbar;
    Button buttonStart;
    ListView listView;
    Button buttonSetDate;
    TextView dbServiceOutDocs, dbServiceOrders, dbServiceBoxes, dbServiceBoxMoves, dbServiceProds;


    public class tableRecord {
        private String Name ;
        private String Date ;
        private String Number;

        public tableRecord(String Name, String Date, String Number) {
            this.Name = Name;
            this.Date = Date;
            this.Number = Number;
        }

        public String getName() {
            return Name;
        }

        public void setName(String name) {
            Name = name;
        }

        public String getDate() {
            return Date;
        }

        public void setDate(String date) {
            Date = date;
        }

        public String getNumber() {
            return Number;
        }

        public void setNumber(String number) {
            Number = number;
        }
    } ;

    List<tableRecord> checkNameList = new ArrayList<tableRecord>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db_service);

        buttonStart = (Button) findViewById(R.id.dbServiceBtnStart);
        dbServiceOrders = (TextView) findViewById(R.id.dbServiceOrders);
        dbServiceOutDocs = (TextView) findViewById(R.id.dbServiceOutDocs);
        dbServiceBoxes = (TextView) findViewById(R.id.dbServiceBoxes);
        dbServiceBoxMoves = (TextView) findViewById(R.id.dbServiceBoxMoves);
        dbServiceProds = (TextView) findViewById(R.id.dbServiceProds);

        buttonSetDate = (Button) findViewById(R.id.dbServiceBtnSetDate);

        checkNameList.add(0, new tableRecord("MasterData", "???", "???"));
        checkNameList.add(1, new tableRecord("OutDocs", "???", "???"));
        checkNameList.add(2, new tableRecord("Boxes", "???", "???"));
        checkNameList.add(3, new tableRecord("BoxMoves", "???", "???"));
        checkNameList.add(4, new tableRecord("Prods", "???", "???"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.setTitle("Сервис БД. ");
    }

    public void updDo(View v) { //
        SyncIncoData task = new SyncIncoData();
        task.execute(new String[]{null});
    }
    public void setDate(View v) { //Вызов активности выбора даты начала
        startActivity(new Intent(this, lastUpdateActivity.class)); //Вызов активности lastUpdate
    }

    private class SyncIncoData extends AsyncTask<String, Integer, String> {
        Integer counter;

        @Override
        protected String doInBackground(String... urls) {
            try {
                //Получить количество записей и датц первой записи из основных таблиц
                checkNameList.set(0, new tableRecord("MasterData",
                        mDBHelper.getTableMinDate("MasterData"), mDBHelper.getTableRecordsCount("MasterData")));
                checkNameList.set(1, new tableRecord("OutDocs",
                        mDBHelper.getTableMinDate("OutDocs"), mDBHelper.getTableRecordsCount("OutDocs")));
                checkNameList.set(2, new tableRecord("Boxes",
                        mDBHelper.getTableMinDate("Boxes"), mDBHelper.getTableRecordsCount("Boxes")));
                checkNameList.set(3, new tableRecord("BoxMoves",
                        mDBHelper.getTableMinDate("BoxMoves"), mDBHelper.getTableRecordsCount("BoxMoves")));
                checkNameList.set(4, new tableRecord("Prods",
                        mDBHelper.getProdsMinDate(), mDBHelper.getTableRecordsCount("Prods")));

                publishProgress();

            } catch (Exception e) {
                Log.d("dbService", "Ошибка при обработке основных таблиц : " + e.getMessage());
                return null;
            }
            counter = 0;
            try {



            } catch (Exception e) {
                Log.d("UpdateActivity", "Error : " + e.getMessage());
                publishProgress(-1);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            buttonStart.setEnabled(false); // прячем кнопку

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            buttonStart.setEnabled(true); // прячем кнопку
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            dbServiceOrders = (TextView) findViewById(R.id.dbServiceOrders);
            dbServiceOrders.setText(checkNameList.get(0).Name+" , Date: "+checkNameList.get(0).Date+" , Records: "+checkNameList.get(0).Number);

            dbServiceOutDocs = (TextView) findViewById(R.id.dbServiceOutDocs);
            dbServiceOutDocs.setText(checkNameList.get(1).Name+" , Date: "+checkNameList.get(1).Date+" , Records: "+checkNameList.get(1).Number);

            dbServiceBoxes = (TextView) findViewById(R.id.dbServiceBoxes);
            dbServiceBoxes.setText(checkNameList.get(2).Name+" , Date: "+checkNameList.get(2).Date+" , Records: "+checkNameList.get(2).Number);

            dbServiceBoxMoves = (TextView) findViewById(R.id.dbServiceBoxMoves);
            dbServiceBoxMoves.setText(checkNameList.get(3).Name+" , Date: "+checkNameList.get(3).Date+" , Records: "+checkNameList.get(3).Number);

            dbServiceProds = (TextView) findViewById(R.id.dbServiceProds);
            dbServiceProds.setText(checkNameList.get(4).Name+" , Date: "+checkNameList.get(4).Date+" , Records: "+checkNameList.get(4).Number);
        }
    }

}
