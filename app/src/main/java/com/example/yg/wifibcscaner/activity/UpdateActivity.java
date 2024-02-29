package com.example.yg.wifibcscaner.activity;

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

import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.BoxMoves;
import com.example.yg.wifibcscaner.data.model.Boxes;
import com.example.yg.wifibcscaner.DataBaseHelper;
import com.example.yg.wifibcscaner.data.model.Deps;
import com.example.yg.wifibcscaner.data.model.Division;
import com.example.yg.wifibcscaner.data.model.Orders;
import com.example.yg.wifibcscaner.data.model.OutDocs;
import com.example.yg.wifibcscaner.data.model.Prods;
import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.data.model.Sotr;
import com.example.yg.wifibcscaner.data.model.Operation;
import com.example.yg.wifibcscaner.data.dto.OrderOutDocBoxMovePart;
import com.example.yg.wifibcscaner.data.repo.DepartmentRepo;
import com.example.yg.wifibcscaner.data.repo.DivisionRepo;
import com.example.yg.wifibcscaner.data.repo.OperRepo;
import com.example.yg.wifibcscaner.data.repo.OrderOutDocBoxMovePartRepository;
import com.example.yg.wifibcscaner.data.repo.OrderRepo;
import com.example.yg.wifibcscaner.data.repo.SotrRepo;
import com.example.yg.wifibcscaner.data.repo.UserRepo;
import com.example.yg.wifibcscaner.service.ApiUtils;
import com.example.yg.wifibcscaner.service.MessageUtils;
import com.example.yg.wifibcscaner.data.model.user;
import com.example.yg.wifibcscaner.utils.DateTimeUtils;

import static com.example.yg.wifibcscaner.utils.DateTimeUtils.sDateToLong;

import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.view.Menu;
import android.view.MenuItem;

import org.apache.commons.lang3.StringUtils;

import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getDayTimeLong;

public class UpdateActivity extends AppCompatActivity {
    private static final String TAG = "UpdateActivity";
    private DataBaseHelper mDBHelper = AppController.getInstance().getDbHelper();
    private final OperRepo operRepo = new OperRepo();
    private final DivisionRepo divRepo = new DivisionRepo();
    private final DepartmentRepo depRepo = new DepartmentRepo();
    private final OrderRepo orderRepo = new OrderRepo();
    private final SotrRepo sotrRepo = new SotrRepo();
    private final UserRepo userRepo = new UserRepo();

    public String globalUpdateDate = "";

    private static final Long ldtMin = DateTimeUtils.getStartOfDayLong(DateTimeUtils.addDays(new Date(), -DateTimeUtils.numberOfDaysInMonth(new Date())));
    private static final String dtMin = DateTimeUtils.getStartOfDayString(ldtMin);

    ProgressBar pbar;
    Button buttonStart;
    ListView listView;
    Button buttonSetDate;


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
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d(TAG, "serverUpdateTime: " + response.body());
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

                ApiUtils.getOrderService(mDBHelper.defs.getUrl())
                        .getUser(StringUtils.isNotBlank(globalUpdateDate) ? globalUpdateDate : userRepo.getUserUpdateDate(dtMin))
                        .enqueue(new Callback<List<user>>() {
                    // TODO Обработать результат. Записать поле sent... если успешно
                    @Override
                    public void onResponse(Call<List<user>> call, Response<List<user>> response) {
                        if (response.isSuccessful() && !response.body().isEmpty()) {
                            for (user user : response.body())
                                userRepo.insertUser(user);
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
                            divRepo.insertDivisionInBulk(response.body());
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
                ApiUtils.getOrderService(mDBHelper.defs.getUrl())
                        .getOperation(StringUtils.isNotBlank(globalUpdateDate) ? globalUpdateDate : operRepo.getOperUpdateDate(dtMin))
                        .enqueue(new Callback<List<Operation>>() {
                    // TODO Обработать результат. Записать поле sent... если успешно
                    @Override
                    public void onResponse(Call<List<Operation>> call, Response<List<Operation>> response) {
                        if (response.isSuccessful()) {
                            for (Operation deps : response.body())
                                operRepo.insertOpers(deps);
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
                ApiUtils.getOrderService(mDBHelper.defs.getUrl())
                        .getSotr(StringUtils.isNotBlank(globalUpdateDate) ? globalUpdateDate : sotrRepo.getSotrUpdateDate(dtMin))
                        .enqueue(new Callback<List<Sotr>>() {
                    @Override
                    public void onResponse(Call<List<Sotr>> call, Response<List<Sotr>> response) {

                        if (response.isSuccessful()) {
                            for (Sotr sotr : response.body())
                                sotrRepo.insertSotr(sotr);
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

                ApiUtils.getOrderService(mDBHelper.defs.getUrl())
                        .getDeps(StringUtils.isNotBlank(globalUpdateDate) ? globalUpdateDate : depRepo.getDepUpdateDate(dtMin))
                        .enqueue(new Callback<List<Deps>>() {
                    @Override
                    public void onResponse(Call<List<Deps>> call, Response<List<Deps>> response) {
                        if (response.isSuccessful()) {
                            for (Deps deps : response.body())
                                depRepo.insertDeps(deps);
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

                //выбрать максимальную дату загрузки заказа из MasterData. Запросить все заказы старше этой даты но только за месяц.
                OrderOutDocBoxMovePartRepository orderOutDocBoxMovePartRepository = new OrderOutDocBoxMovePartRepository();
                orderOutDocBoxMovePartRepository.downloadData(StringUtils.isNotBlank(globalUpdateDate) ? globalUpdateDate :  orderRepo.getOrderUpdateDate(dtMin));

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
            for (int i = 0; i < listView.getMaxScrollAmount(); i++) {
                listView.setItemChecked(i, false);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            buttonStart.setEnabled(true); // прячем кнопку
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            listView.setItemChecked(values[0] - 1, true);

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
                    globalUpdateDate = "";
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
        Intent intent = new Intent(this, lastUpdateActivity.class); //Вызов активности lastUpdate
        final long dateFrom = DateTimeUtils.getStartOfDayLong(DateTimeUtils.addDays(new Date(), -1));
        intent.putExtra("presetDate", dateFrom); // sent your putExtra data here to pass through intent
        startActivityForResult(intent, 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                final long longExtra = data.getExtras().getLong("presetDate", 0);
                if (longExtra == 0) {
                    Log.d(TAG, "lastUpdateActivity.onActivityResult -> DateTimePicker returned 0");
                    return;
                }
                globalUpdateDate = DateTimeUtils.getStartOfDayString(longExtra);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.w(TAG, "lastUpdateActivity.onActivityResult -> RESULT_CANCELED");
            }
        }
    }
}