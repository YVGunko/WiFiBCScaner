package com.example.yg.wifibcscaner;

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
import com.example.yg.wifibcscaner.service.ApiUtils;
import com.example.yg.wifibcscaner.service.MessageUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.view.Menu;
import android.view.MenuItem;

import static com.example.yg.wifibcscaner.DataBaseHelper.getDayTimeLong;
import static com.example.yg.wifibcscaner.DataBaseHelper.getStartOfDayString;

public class UpdateActivity extends AppCompatActivity {
    private static final String TAG = "UpdateActivity";
    private DataBaseHelper mDBHelper;
    ProgressBar pbar;
    Button buttonStart;
    ListView listView;
    Button buttonSetDate;
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
                startActivity(new Intent(this,dbService.class));  //Вызов активности Коробки
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
                        Log.d(TAG, "serverUpdateTime: " + response.body());
                        if (response.isSuccessful()) {
                            mDBHelper.serverUpdateTime = response.body();
                        }
                    }

                    @Override
                    public void onFailure(Call<Long> call, Throwable t) {
                        Log.d(TAG, "Ошибка при запросе времени обновления с сервера: " + t.getMessage());
                    }
                });
            } catch (Exception e) {
                Log.d(TAG, "Ошибка при запросе времени обновления с сервера : " + e.getMessage());
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
                                Log.d(TAG, "Ответ сервера на запрос новых users: " + response.body().size());
                            }
                        }
                        counter = counter + 5;
                        publishProgress(1);
                    }

                    @Override
                    public void onFailure(Call<List<user>> call, Throwable t) {
                        Log.d(TAG, "Ответ сервера на запрос новых users: " + t.getMessage());
                    }
                });

                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getDivision().enqueue(new Callback<List<Division>>() {
                    @Override
                    public void onResponse(Call<List<Division>> call, Response<List<Division>> response) {

                        if (response.isSuccessful() && !response.body().isEmpty()) {
                                mDBHelper.insertDivisionInBulk(response.body());
                            if (response.body().size() != 0) {
                                Log.d(TAG, "Ответ сервера на запрос новых подразделений: " + response.body().size());
                            }
                        }
                        counter = counter + 5;
                        publishProgress(1);
                    }

                    @Override
                    public void onFailure(Call<List<Division>> call, Throwable t) {
                        Log.d(TAG, "Ответ сервера на запрос новых сотрудников: " + t.getMessage());
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
                                Log.d(TAG, "Ответ сервера на запрос новых сотрудников: " + response.body().size());
                            }
                        }
                        counter = counter + 5;
                        publishProgress(1);
                    }

                    @Override
                    public void onFailure(Call<List<Sotr>> call, Throwable t) {
                        Log.d(TAG, "Ответ сервера на запрос новых сотрудников: " + t.getMessage());
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
                                Log.d(TAG, "Ответ сервера на запрос новых бригад: " + response.body().size());
                        }
                        counter = counter + 5;
                        publishProgress(2);
                    }

                    @Override
                    public void onFailure(Call<List<Deps>> call, Throwable t) {
                        Log.d(TAG, "Ответ сервера на запрос новых бригад: " + t.getMessage());
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
                //выбрать максимальную дату загрузки заказа из MasterData. Запросить все заказы старше этой даты но только за месяц.
                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getOrders(strUpdateDate, mDBHelper.defs.get_idUser(), mDBHelper.defs.getDeviceId()).enqueue(new Callback<List<Orders>>() {
                    // TODO Обработать результат. Записать поле sent... если успешно
                    @Override
                    public void onResponse(Call<List<Orders>> call, Response<List<Orders>> response) {
                        Log.d(TAG, "Ответ сервера на запрос новых заказов: " + response.body().size());
                        if (response.isSuccessful() && !response.body().isEmpty()) {
                                mDBHelper.insertOrdersInBulk(response.body());
                            //Прописать даты в lastUpdate
                            if (response.body().size() != 0)
                                mDBHelper.setLastUpdate(new lastUpdate(Orders.TABLE_orders,
                                    mDBHelper.serverUpdateTime,
                                        getDayTimeLong(new Date()),
                                    true));
                            counter = counter + 10;
                        } else {
                            Log.d(TAG, "getOrders response is empty.");
                        }
                        publishProgress(5);
                    }

                    @Override
                    public void onFailure(Call<List<Orders>> call, Throwable t) {
                        Log.d(TAG, "Ответ сервера на запрос новых заказов: " + t.getMessage());
                        publishProgress(-1);
                    }
                });
                if (!mDBHelper.globalUpdateDate.equals("")) {
                    strUpdateDate = mDBHelper.globalUpdateDate;
                } else {
                    strUpdateDate = mDBHelper.getMaxOutDocsDate();
                }
                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getOutDocGet(strUpdateDate).enqueue(new Callback<List<OutDocs>>() {
                    @Override
                    public void onResponse(Call<List<OutDocs>> call, Response<List<OutDocs>> response) {
                        MessageUtils messageUtils = new MessageUtils();
                        Log.d(TAG, "Ответ сервера на запрос новых накладных: " + response.body().size());
                        if (response.isSuccessful() && !response.body().isEmpty()) {
                            mDBHelper.insertOutDocInBulk(response.body());
                            if (response.body().size() != 0){
                                Log.d(TAG, "Ок! Новые накладные приняты!");
                                //Прописать даты в lastUpdate
                                if (response.body().size() != 0)
                                    mDBHelper.setLastUpdate(new lastUpdate(OutDocs.TABLE,
                                            mDBHelper.serverUpdateTime,
                                            getDayTimeLong(new Date()),
                                            true));
                            }
                            counter = counter + 5;
                            publishProgress(4);

                            if (!mDBHelper.globalUpdateDate.equals("")) {
                                strUpdateDate = mDBHelper.globalUpdateDate;
                            } else {
                                strUpdateDate = mDBHelper.getLongDateTimeString(mDBHelper.getTableUpdateDate("Boxes"));
                            }
                            ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getBoxesByDate(strUpdateDate, mDBHelper.defs.get_idUser(), mDBHelper.defs.getDeviceId()).enqueue(new Callback<List<Boxes>>() {
                                // TODO Обработать результат. Записать поле sent... если успешно
                                @Override
                                public void onResponse(Call<List<Boxes>> call, Response<List<Boxes>> response) {

                                    if (response.isSuccessful() && !response.body().isEmpty()) {
                                        mDBHelper.insertBoxInBulk(response.body());

                                        if (response.body().size() != 0) {
                                            if (mDBHelper.getTableUpdateDate(Boxes.TABLE_boxes) < mDBHelper.sDateToLong(response.body().get(response.body().size()-1).get_DT()))
                                            {
                                                mDBHelper.setLastUpdate(new lastUpdate(Boxes.TABLE_boxes,
                                                        mDBHelper.sDateToLong(response.body().get(response.body().size()-1).get_DT()),
                                                        getDayTimeLong(new Date()),
                                                        true));
                                                Log.d(TAG, "Записана новая дата в таблицу LastUpdate для Boxes: " + response.body().get(response.body().size()-1).get_DT());
                                            }
                                            Log.d(TAG, "Ответ сервера на запрос синхронизации коробок: " + response.body().size());
                                        }
                                    }
                                    counter = counter + 10;
                                    publishProgress(6);
                                }

                                @Override
                                public void onFailure(Call<List<Boxes>> call, Throwable t) {
                                    Log.d("UpdateActivity", "Ответ сервера на запрос синхронизации коробок: " + t.getMessage());
                                    publishProgress(-1);
                                }
                            });
                            if (!mDBHelper.globalUpdateDate.equals("")) {
                                strUpdateDate = mDBHelper.globalUpdateDate;
                            } else {
                                strUpdateDate = mDBHelper.getLongDateTimeString(mDBHelper.getTableUpdateDate("BoxMoves"));
                            }
                            ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getBoxMovesByDatePagebleCount(strUpdateDate).enqueue(new Callback<Integer>() {
                                // Получаем количество страниц. В цикле запускаем = количество запросов.
                                @Override
                                public void onResponse(Call<Integer> call, Response<Integer> response) {
                                    if (response.isSuccessful()) {
                                        int totalPages = response.body().intValue();
                                        for (int i = 0; i < totalPages; i++) {
                                            ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getBoxMovesByDatePageble(strUpdateDate, mDBHelper.defs.get_idUser(), mDBHelper.defs.getDeviceId(), i).
                                                    enqueue(new Callback<List<BoxMoves>>() {
                                                        @Override
                                                        public void onResponse
                                                                (Call<List<BoxMoves>> call, Response<List<BoxMoves>> response) {
                                                            if (response.isSuccessful() && !response.body().isEmpty()) {
                                                                mDBHelper.insertBoxMoveInBulk(response.body());

                                                                if (response.body().size() != 0) {
                                                                    if (mDBHelper.getTableUpdateDate(BoxMoves.TABLE_bm) < mDBHelper.sDateToLong(response.body().get(response.body().size()-1).get_DT()))
                                                                    {
                                                                        mDBHelper.setLastUpdate(new lastUpdate(BoxMoves.TABLE_bm,
                                                                                mDBHelper.sDateToLong(response.body().get(response.body().size()-1).get_DT()),
                                                                                getDayTimeLong(new Date()),
                                                                                true));
                                                                        Log.d("UpdateActivity", "Записана новая дата в таблицу LastUpdate для BoxMoves: " + response.body().get(response.body().size()-1).get_DT());
                                                                    }
                                                                    Log.d("UpdateActivity", "Ответ сервера на запрос синхронизации BoxMoves: " + response.body().size());
                                                                }
                                                            }
                                                            counter = counter + 20;
                                                            publishProgress(7);
                                                        }

                                                        @Override
                                                        public void onFailure
                                                                (Call<List<BoxMoves>> call, Throwable t) {
                                                            Log.d("UpdateActivity", "Ответ сервера на запрос синхронизации движений коробок: " + t.getMessage());
                                                            publishProgress(-1);
                                                        }
                                                    });
                                        }
                                    }
                                }
                                @Override
                                public void onFailure(Call<Integer> call, Throwable t) {
                                    Log.d("1", "getBoxMovesByDatePagebleCount onFailure: " + t.getMessage());
                                }
                            });

                            if (!mDBHelper.globalUpdateDate.equals("")) {
                                strUpdateDate = mDBHelper.globalUpdateDate;
                            } else {
                                strUpdateDate = mDBHelper.getLongDateTimeString(mDBHelper.getTableUpdateDate("Prods"));
                            }
                            ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getPartBoxByDatePagebleCount(strUpdateDate).enqueue(new Callback<Integer>() {
                                // Получаем количество страниц. В цикле запускаем = количество запросов.
                                @Override
                                public void onResponse(Call<Integer> call, Response<Integer> response) {
                                    if (response.isSuccessful()) {

                                        int totalPages = response.body().intValue();
                                        for (int i = 0; i < totalPages; i++) {
                                            ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getPartBoxByDatePageble(strUpdateDate, mDBHelper.defs.get_idUser(), mDBHelper.defs.getDeviceId(), i).
                                                    enqueue(new Callback<List<Prods>>() {
                                                        @Override
                                                        public void onResponse(Call<List<Prods>> call, Response<List<Prods>> response) {
                                                            if (response.isSuccessful() && !response.body().isEmpty()) {
                                                                mDBHelper.insertProdInBulk(response.body());
                                                                if (response.body().size() != 0) {
                                                                    if (mDBHelper.getTableUpdateDate("Prods") < mDBHelper.sDateToLong(response.body().get(response.body().size()-1).get_P_date()))
                                                                    {
                                                                        mDBHelper.setLastUpdate(new lastUpdate(Prods.TABLE_prods,
                                                                                mDBHelper.sDateToLong(response.body().get(response.body().size()-1).get_P_date()),
                                                                                getDayTimeLong(new Date()),
                                                                                true));
                                                                        Log.d("UpdateActivity", "Записана новая дата в таблицу LastUpdate для Prods: " + response.body().get(response.body().size()-1).get_P_date());
                                                                    }
                                                                    Log.d("UpdateActivity", "Ответ сервера на запрос синхронизации PartBox: " + response.body().size());
                                                                }

                                                            }
                                                            counter = counter + 40;
                                                            publishProgress(8);
                                                        }

                                                        @Override
                                                        public void onFailure(Call<List<Prods>> call, Throwable t) {
                                                            Log.d("UpdateActivity", "Ответ сервера на запрос синхронизации PartBox: " + t.getMessage());
                                                            publishProgress(-1);
                                                        }
                                                    });
                                        }
                                    }
                                }
                                @Override
                                public void onFailure(Call<Integer> call, Throwable t) {
                                    Log.d(TAG, "getBoxMovesByDatePagebleCount onFailure: " + t.getMessage());
                                }
                            });

                        } else {
                            Log.d(TAG, "getOutDocs response is empty.");
                            publishProgress(5);
                            publishProgress(6);
                            publishProgress(7);
                            publishProgress(8);
                        }
                    }
                    @Override
                    public void onFailure(Call<List<OutDocs>> call, Throwable t) {
                        Log.d(TAG, "Ответ сервера на запрос новых накладных: " + t.getMessage());
                        publishProgress(-1);
                    }
                });
            } catch (Exception e) {
                Log.d(TAG, "Error : " + e.getMessage());
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

            switch (values[0]) {
                case -1:
                    //listView.setSelection(0);
                    values[0] = 0;
                    break;
                case 1:
                case 2:
                    values[0] = values[0] * 2;
                    break;
                case 3:
                    values[0] = values[0] * 1;
                    break;
                case 4:
                case 5:
                    values[0] = values[0] * 3;
                    break;
                case 6:
                    values[0] = values[0] * 3;
                    MessageUtils.showToast(getApplicationContext(), "Синхронизация коробок завершена.", false);
                    break;
                case 7:
                    values[0] = values[0] * 4;
                    MessageUtils.showToast(getApplicationContext(), "Синхронизация движений подошвы завершена.", false);
                    break;
                case 8:
                    values[0] = values[0] * 5;
                    MessageUtils.showToast(getApplicationContext(), "Синхронизация подошвы завершена.", false);
                    mDBHelper.globalUpdateDate = "";
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
        startActivity(new Intent(this,lastUpdateActivity.class)); //Вызов активности lastUpdate
    }
}