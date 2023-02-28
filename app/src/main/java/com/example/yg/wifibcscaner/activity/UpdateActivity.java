package com.example.yg.wifibcscaner.activity;

//import android.content.Context;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.yg.wifibcscaner.DataBaseHelper;
import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.data.model.Deps;
import com.example.yg.wifibcscaner.data.model.Operation;
import com.example.yg.wifibcscaner.data.model.Sotr;
import com.example.yg.wifibcscaner.data.repository.OrderOutDocBoxMovePartRepository;
import com.example.yg.wifibcscaner.utils.ApiUtils;
import com.example.yg.wifibcscaner.utils.MessageUtils;
import com.example.yg.wifibcscaner.data.model.user;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.view.Menu;
import android.view.MenuItem;

import static com.example.yg.wifibcscaner.utils.DateTimeUtils.*;

public class UpdateActivity extends AppCompatActivity {
    private DataBaseHelper mDBHelper;
    ProgressBar pbar;
    Button buttonStart;
    ListView listView;
    Button buttonSetDate;
    Integer iPartBox, iPartBoxRecordNumber, iPartBoxPage, ibmPage, iBox, iBoxRecordNumber, iBoxMove, iBoxMoveRecordNumber ;
    String strUpdateDate = mDBHelper.dtMin;
    Long lUpdateDate = mDBHelper.ldtMin;

    String[] checkNameList = {
            "Сотрудники",
            "Бригады",
            "Операции",
            "Накладные",
            "Заказы",
            "Коробки",
            "Движения коробок",
            "Подошва"
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.update_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();
        // Операции для выбранного пункта меню
        switch (id) {
            case R.id.action_update_menu_1:
                startActivity(new Intent(this, dbService.class));  //Вызов активности Коробки
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        mDBHelper = DataBaseHelper.getInstance(this);
        buttonStart = (Button) findViewById(R.id.buttonStart);
        pbar = (ProgressBar) findViewById(R.id.progressBarpbar);
        listView = (ListView) findViewById(R.id.listView);
        buttonSetDate = (Button) findViewById(R.id.buttonSetDate);
        // используем адаптер данных
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_checked, checkNameList);

        listView.setAdapter(adapter);
        listView.setEnabled(false);
    }


    @Override
    protected void onResume() {
        super.onResume();
        this.setTitle("Синхронизация данных. ");
    }

    private class SyncIncoData extends AsyncTask<String, Integer, String> {
        Integer counter;

        @Override
        protected String doInBackground(String... urls) {
            try {
                //Запросить с сервера время
                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getServerUpdateTime().enqueue(new Callback<Long>() {
                    // TODO Обработать результат. Записать поле sent... если успешно
                    @Override
                    public void onResponse(Call<Long> call, Response<Long> response) {
                        Log.d("serverUpdateTime", "serverUpdateTime: " + response.body());
                        if (response.isSuccessful()) {
                            mDBHelper.serverUpdateTime = response.body();
                        }
                    }

                    @Override
                    public void onFailure(Call<Long> call, Throwable t) {
                        Log.d("UpdateActivity", "Ошибка при запросе времени обновления с сервера: " + t.getMessage());
                    }
                });
            } catch (Exception e) {
                Log.d("1", "Ошибка при запросе времени обновления с сервера : " + e.getMessage());
                return null;
            }
            counter = 0;
            try {

                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getUser(mDBHelper.getMaxUserDate()).enqueue(new Callback<List<user>>() {
                    // TODO Обработать результат. Записать поле sent... если успешно
                    @Override
                    public void onResponse(Call<List<user>> call, Response<List<user>> response) {
                        if (response.isSuccessful()) {
                            for (user user : response.body())
                                mDBHelper.insertUser(user);
                            if (response.body().size() != 0) {
                                Log.d("UpdateActivity", "Ответ сервера на запрос новых users: " + response.body().size());
                            }
                        }
                        counter = counter + 5;
                        publishProgress(1);
                    }

                    @Override
                    public void onFailure(Call<List<user>> call, Throwable t) {
                        Log.d("UpdateActivity", "Ответ сервера на запрос новых users: " + t.getMessage());
                    }
                });

                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getSotr(mDBHelper.getMaxSotrDate()).enqueue(new Callback<List<Sotr>>() {
                    // TODO Обработать результат. Записать поле sent... если успешно
                    @Override
                    public void onResponse(Call<List<Sotr>> call, Response<List<Sotr>> response) {

                        if (response.isSuccessful()) {
                            for (Sotr sotr : response.body())
                                mDBHelper.insertSotr(sotr);
                            if (response.body().size() != 0) {
                                Log.d("UpdateActivity", "Ответ сервера на запрос новых сотрудников: " + response.body().size());
                            }
                        }
                        counter = counter + 5;
                        publishProgress(1);
                    }

                    @Override
                    public void onFailure(Call<List<Sotr>> call, Throwable t) {
                        Log.d("UpdateActivity", "Ответ сервера на запрос новых сотрудников: " + t.getMessage());
                    }
                });

                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getDeps(mDBHelper.getMaxDepsDate()).enqueue(new Callback<List<Deps>>() {
                    // TODO Обработать результат. Записать поле sent... если успешно
                    @Override
                    public void onResponse(Call<List<Deps>> call, Response<List<Deps>> response) {
                        if (response.isSuccessful()) {
                            for (Deps deps : response.body())
                                mDBHelper.insertDeps(deps);
                            if (response.body().size() != 0)
                                Log.d("UpdateActivity", "Ответ сервера на запрос новых бригад: " + response.body().size());
                        }
                        counter = counter + 5;
                        publishProgress(2);
                    }

                    @Override
                    public void onFailure(Call<List<Deps>> call, Throwable t) {
                        Log.d("UpdateActivity", "Ответ сервера на запрос новых бригад: " + t.getMessage());
                    }
                });


                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getOperation(mDBHelper.getMaxOpersDate()).enqueue(new Callback<List<Operation>>() {
                    // TODO Обработать результат. Записать поле sent... если успешно
                    @Override
                    public void onResponse(Call<List<Operation>> call, Response<List<Operation>> response) {
                        if (response.isSuccessful()) {
                            for (Operation deps : response.body())
                                mDBHelper.insertOpers(deps);
                            if (response.body().size() != 0)
                                Log.d("UpdateActivity", "Ок! Новые операции приняты!");
                        }
                        counter = counter + 5;
                        publishProgress(3);
                    }

                    @Override
                    public void onFailure(Call<List<Operation>> call, Throwable t) {
                        Log.d("UpdateActivity", "Ответ сервера на запрос новых операций: " + t.getMessage());
                    }
                });

                strUpdateDate = mDBHelper.dtMin;
                lUpdateDate = mDBHelper.ldtMin;

                if (!mDBHelper.globalUpdateDate.equals("")) {
                    strUpdateDate = mDBHelper.globalUpdateDate;
                } else {
                    strUpdateDate = mDBHelper.getMaxOrderDate();
                }
                new OrderOutDocBoxMovePartRepository().fetchAndSaveData(DataBaseHelper.getInstance(getApplicationContext()));

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
            pbar.setProgress(0);
            for (int i = 0; i < listView.getMaxScrollAmount() ; i++) {listView.setItemChecked(i, false);}
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            buttonStart.setEnabled(true); // прячем кнопку
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            listView.setItemChecked(values[0]-1, true);
            MessageUtils messageUtils = new MessageUtils();

            switch (values[0]) {
                case -1:
                    //listView.setSelection(0);
                    values[0] = 0;
                    break;
                case 1:
                    values[0] = values[0] * 2;
                    break;
                case 2:
                    values[0] = values[0] * 2;
                    break;
                case 3:
                    values[0] = values[0] * 1;
                    break;
                case 4:
                    values[0] = values[0] * 3;
                    break;
                case 5:
                    values[0] = values[0] * 3;
                    break;
                case 6:
                    values[0] = values[0] * 3;
                    messageUtils.showLongMessage(getApplicationContext(), "Синхронизация коробок завершена.");
                    break;
                case 7:
                    values[0] = values[0] * 4;
                    messageUtils.showLongMessage(getApplicationContext(), "Синхронизация движений подошвы завершена.");
                    break;
                case 8:
                    values[0] = values[0] * 5;
                    messageUtils.showLongMessage(getApplicationContext(), "Синхронизация подошвы завершена. Загружено "+
                            String.valueOf(iPartBox));
                    break;

            }
            Integer cProgress = pbar.getProgress();
            pbar.setProgress(cProgress + values[0]);
        }
    }

    public void updDo(View v) { //
            SyncIncoData task = new SyncIncoData();
            task.execute(new String[]{null});
    }
    public void setDate(View v) { //Вызов активности выбора даты начала
        startActivity(new Intent(this, lastUpdateActivity.class)); //Вызов активности lastUpdate
    }
}