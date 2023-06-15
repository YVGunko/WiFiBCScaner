package com.example.yg.wifibcscaner.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.yg.wifibcscaner.BuildConfig;
import com.example.yg.wifibcscaner.DataBaseHelper;
import com.example.yg.wifibcscaner.data.Defs;
import com.example.yg.wifibcscaner.data.Deps;
import com.example.yg.wifibcscaner.data.Division;
import com.example.yg.wifibcscaner.data.OutDocs;
import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.service.SharedPrefs;
import com.example.yg.wifibcscaner.data.Sotr;
import com.example.yg.wifibcscaner.data.Operation;
import com.example.yg.wifibcscaner.service.ApiUtils;
import com.example.yg.wifibcscaner.service.MessageUtils;
import com.example.yg.wifibcscaner.service.PartBoxService;
import com.example.yg.wifibcscaner.data.user;
import com.example.yg.wifibcscaner.utils.AppUtils;
import com.example.yg.wifibcscaner.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.yg.wifibcscaner.utils.AppUtils.isDepAndSotrOper;

public class SettingsActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {

    private static final String TAG = "SettingsActivity";
    private PartBoxService boxesService;
    EditText host_v;
    TextView select_label, opers_select_label, labelSotr, labelDivision2;
    private DataBaseHelper mDBHelper;
    private int idd,ido,ids, idUser;
    private String division_code ;
    String strTitle = "Настройки";

    // Spinner element
    Spinner spinner, opers_spinner, spinnerSotr,  spinnerDivision;
    @Override
    protected void onResume() {
        super.onResume();
        this.setTitle(strTitle);
    }
    @Override
    public void onStop(){
        super.onStop();
        this.setTitle(strTitle);
    }
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mDBHelper = DataBaseHelper.getInstance(this);
        host_v=(EditText) findViewById(R.id.host);

        String devId;
        try {
            devId = ((mDBHelper.defs.getDeviceId().substring(0,6).length()==6) ? mDBHelper.defs.getDeviceId().substring(0,5) : "Unknown");}
        catch (Exception e) {
            devId = "unKnown";
        }

        strTitle = "Настройки"+". v."+ BuildConfig.VERSION_NAME+"."+BuildConfig.VERSION_CODE+". Id."+ devId;

        // Spinner element
        opers_spinner = (Spinner) findViewById(R.id.opers_spinner);
        spinnerDivision = (Spinner) findViewById(R.id.spinnerDivision);
        spinner = (Spinner) findViewById(R.id.spinner);
        spinnerSotr = (Spinner) findViewById(R.id.spinnerSotr);
        // Spinner click listener

        opers_spinner.setOnItemSelectedListener(this);
        spinnerDivision.setOnItemSelectedListener(this);
        spinner.setOnItemSelectedListener(this);
        spinnerSotr.setOnItemSelectedListener(this);
        // Выборка настроек по умолчанию

        mDBHelper.selectDefsTable();
        try {
            String url = mDBHelper.defs.getUrl();
            boxesService = ApiUtils.getBoxesService(url);
            host_v.setText(mDBHelper.defs.get_Host_IP());
            ocl_check(findViewById(R.id.check));
        }
        catch(Exception e){
            Log.e(TAG, "onCreate -> " ,e);
            MessageUtils.showToast(getApplicationContext(), "Настройки не загружены.", false);
        }


        // Loading spinner data from database
        loadSpinnerDivisionData();
        loadOpers_spinnerData();
        loadSpinnerData();
        loadSpinnerSotrData();

        labelDivision2 = (TextView) findViewById(R.id.labelDivision2);
        labelDivision2.setText(mDBHelper.getDivisionsName(mDBHelper.defs.getDivision_code()));
        opers_select_label = (TextView) findViewById(R.id.opers_select_label);
        opers_select_label.setText(mDBHelper.getOpers_Name_by_id(mDBHelper.defs.get_Id_o()));
        select_label = (TextView) findViewById(R.id.select_label);
        select_label.setText(mDBHelper.getDeps_Name_by_id(mDBHelper.defs.get_Id_d()));
        labelSotr = (TextView) findViewById(R.id.labelSotr);
        labelSotr.setText(mDBHelper.getSotr_Name_by_id(mDBHelper.defs.get_Id_s()));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();
        // Операции для выбранного пункта меню
        switch (id) {
            case R.id.action_receive_spr:
                try {

                    ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getUser("01.01.2018 00:00:00").enqueue(new Callback<List<user>>() {
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
                        }

                        @Override
                        public void onFailure(Call<List<user>> call, Throwable t) {
                            Log.d(TAG, "Ответ сервера на запрос новых users: " + t.getMessage());
                        }
                    });

                    ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getOperation("01.01.2018 00:00:00").enqueue(new Callback<List<Operation>>() {
                        // TODO Обработать результат. Записать поле sent... если успешно
                        @Override
                        public void onResponse(Call<List<Operation>> call, Response<List<Operation>> response) {
                            //Log.d("1","Ответ сервера на запрос новых операций: " + response.body());
                            if(response.isSuccessful()) {
                                for(Operation operation : response.body())
                                    mDBHelper.insertOpers(operation);
                                MessageUtils.showToast(getApplicationContext(), "Ок! Новые операции приняты!", false);
                                loadOpers_spinnerData();
                                opers_select_label = (TextView) findViewById(R.id.opers_select_label);
                                opers_select_label.setText(mDBHelper.getOpers_Name_by_id(mDBHelper.defs.get_Id_o()));
                            }else {
                                MessageUtils.showToast(getApplicationContext(), "Ошибка при приеме операций!", false);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Operation>> call, Throwable t) {
                            MessageUtils.showToast(getApplicationContext(), "Ошибка при приеме операций!", false);
                            Log.d(TAG,"Ответ сервера на запрос новых операций: " + t.getMessage());
                        }
                    });

                    ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getSotr("01.01.2018 00:00:00").enqueue(new Callback<List<Sotr>>() {
                        // TODO Обработать результат. Записать поле sent... если успешно
                        @Override
                        public void onResponse(Call<List<Sotr>> call, Response<List<Sotr>> response) {
                            //Log.d("UpdateActivity","Ответ сервера на запрос новых сотрудников: " + response.body());
                            if(response.isSuccessful()) {
                                for(Sotr sotr : response.body())
                                    mDBHelper.insertSotr(sotr);
                                MessageUtils.showToast(getApplicationContext(), "Ок! Новые сотрудники приняты!", false);
                                loadSpinnerSotrData();
                                labelSotr = (TextView) findViewById(R.id.labelSotr);
                                labelSotr.setText(mDBHelper.getSotr_Name_by_id(mDBHelper.defs.get_Id_s()));
                            }else {
                                MessageUtils.showToast(getApplicationContext(), "Ошибка при приеме сотрудников!", false);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Sotr>> call, Throwable t) {
                            MessageUtils.showToast(getApplicationContext(), "Ошибка при приеме сотрудников!", false);
                            Log.d("UpdateActivity","Ответ сервера на запрос новых сотрудников: " + t.getMessage());
                        }
                    });


                    ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getDeps("01.01.2018 00:00:00").enqueue(new Callback<List<Deps>>() {
                        // TODO Обработать результат. Записать поле sent... если успешно
                        @Override
                        public void onResponse(Call<List<Deps>> call, Response<List<Deps>> response) {
                            //Log.d("UpdateActivity","Ответ сервера на запрос новых бригад: " + response.body());
                            if(response.isSuccessful()) {
                                for(Deps deps : response.body())
                                    mDBHelper.insertDeps(deps);
                                MessageUtils.showToast(getApplicationContext(), "Ок! Новые бригады приняты!", false);
                                loadSpinnerData();
                                select_label = (TextView) findViewById(R.id.select_label);
                                select_label.setText(mDBHelper.getDeps_Name_by_id(mDBHelper.defs.get_Id_d()));
                            }else {
                                MessageUtils.showToast(getApplicationContext(), "Ошибка при приеме бригад!", false);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Deps>> call, Throwable t) {
                            MessageUtils.showToast(getApplicationContext(), "Ошибка при приеме бригад!", false);
                            Log.d("UpdateActivity","Ответ сервера на запрос новых бригад: " + t.getMessage());
                        }
                    });

                } catch (Exception e) {
                    MessageUtils.showToast(getApplicationContext(), "Ошибка при приеме заказов!", false);
                    Log.d("UpdateActivity","Ответ сервера на запрос новых заказов: " + e.getMessage());
                }
                // Loading spinner data from database



                return true;
            case R.id.action_receive_box:
                try {
                    SettingsActivity.SyncIncoData task = new SettingsActivity.SyncIncoData();
                    task.execute(new String[]{null});

                } catch (Exception e) {

                    Log.d(TAG, "Ответ сервера на запрос новых заказов: " + e.getMessage());
                }
                return true;

            case R.id.action_db_need_replace:
                try {
                    openDbReplaceDialog();
                } catch (Exception e) {

                    Log.d(TAG, "Запрос на очистку БД: " + e.getMessage());
                }
                return true;
            case R.id.set_outdocs_number_start_date:
                try {
                    openDateSetActivity();
                } catch (Exception e) {

                    Log.d(TAG, "set_outdocs_number_start_date -> " + e.getMessage());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void openDateSetActivity() {
        Intent intent = new Intent(this, lastUpdateActivity.class); //Вызов активности lastUpdate

        long dateFrom = DateTimeUtils.getStartOfDayLong(DateTimeUtils.addDays(new Date(), -5));

        intent.putExtra("presetDate", dateFrom); // sent your putExtra data here to pass through intent
        startActivityForResult(intent, 1000);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1000) {
            if(resultCode == Activity.RESULT_OK){
                //mDBHelper.globalUpdateDate = data.getStringExtra("presetDate");
                final long longExtra = data.getExtras().getLong("presetDate", 0);
                if (longExtra == 0) {
                    Log.d(TAG,"lastUpdateActivity.onActivityResult -> DateTimePicker returned 0");
                    return;
                }
                if (SharedPrefs.getInstance(getApplicationContext()) != null) {
                    SharedPrefs.getInstance(getApplicationContext()).setOutdocsNumerationStartDate(longExtra);
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.w(TAG,"lastUpdateActivity.onActivityResult -> RESULT_CANCELED");
            }
        }
    }
    private static void triggerRebirth(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        context.startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }
    private void openDbReplaceDialog() {
        List<Integer> selectedItems = new ArrayList();
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                SettingsActivity.this);
        quitDialog.setTitle(R.string.dialog_db_need_replace)
            .setMultiChoiceItems(R.array.options_db_need_replace,null,
                    new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which,
                                            boolean isChecked) {
                            if (isChecked) {
                                // If the user checked the item, add it to the selected items
                                selectedItems.add(which);
                            } else if (selectedItems.contains(which)) {
                                // Else, if the item is already in the array, remove it
                                selectedItems.remove(which);
                            }
                        }
                    });

        quitDialog.setPositiveButton("Да!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int length =getResources().getStringArray(R.array.options_db_need_replace).length;
                if (selectedItems.size() == length) {
                    if (SharedPrefs.getInstance(getApplicationContext()) != null) {
                        SharedPrefs.getInstance(getApplicationContext()).setDbNeedReplace(true);
                    }
                    //SharedPreferences prefs = getSharedPreferences(SharedPrefs.PREFS_NAME, MODE_PRIVATE);
                    //prefs.edit().putBoolean(SharedPrefs.PREF_DB_NEED_REPLACE, true).apply();
                    triggerRebirth(SettingsActivity.this);
                } else
                {
                    MessageUtils.showToast(getApplicationContext(),"Операция не выполнена!", false);
                }
            }
        });

        quitDialog.setNegativeButton("Нет.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        quitDialog.show();
    }
    public void ocl_check(View v) { //Вызов активности проверки подключения к серверу
        MessageUtils.showToast(getApplicationContext(),"Поиск сервера....", true);
        checkConnection();
    }

    public void checkConnection() {
            String url = mDBHelper.defs.getUrl();
            boxesService = ApiUtils.getBoxesService(url);
            boxesService.checkConnection().enqueue(new Callback<Object>() {

                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    if(!response.isSuccessful()) {
                        MessageUtils.showToast(getApplicationContext(),"Введенный URL недоступен! Введите верный!", false);
                        host_v.requestFocus();
                    }else {
                        MessageUtils.showToast(getApplicationContext(),"Соединение установлено!", false);
                    }
                }
                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    MessageUtils.showToast(getApplicationContext(),"Введенный URL недоступен! Введите верный!", false);
                    host_v.requestFocus();
                }
            });
    }
    private void loadOpers_spinnerData() {
        // database handler
        if (division_code==null) division_code=mDBHelper.defs.getDivision_code();
        // Spinner Drop down elements
        List<String> lables = mDBHelper.getAllnameOpers(division_code);

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lables);

        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        opers_spinner.setAdapter(dataAdapter);
    }
    private void loadSpinnerDivisionData() {
        // database handler

        // Spinner Drop down elements
        List<String> lables = mDBHelper.getAllDivisionsName();

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lables);

        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinnerDivision.setAdapter(dataAdapter);
    }
    private void loadSpinnerData() { //Departments Deps Бригады
        if (division_code==null) division_code=mDBHelper.defs.getDivision_code();
        if (ido==0) ido=mDBHelper.defs.get_Id_o();
        List<String> lables = mDBHelper.getAllnameDeps(division_code, ido);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lables);
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataAdapter.notifyDataSetChanged();

        spinner.setAdapter(dataAdapter);
    }
    private void loadSpinnerSotrData() {
        // database handler

        // Spinner Drop down elements
        if (division_code==null) division_code=mDBHelper.defs.getDivision_code();
        if (idd==0) idd=mDBHelper.defs.get_Id_d();
        if (ido==0) ido=mDBHelper.defs.get_Id_o();
        List<String> lables = mDBHelper.getAllnameSotr(division_code, idd, ido);

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lables);

        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinnerSotr.setAdapter(dataAdapter);
        dataAdapter.notifyDataSetChanged();
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
// On selecting a spinner item
        Spinner sp = (Spinner) parent;
        if(sp.getId() == R.id.spinnerDivision) {
            if (position != 0) {
                String label = parent.getItemAtPosition(position).toString();
//Выбрать _id Division и записать в Defs;
                division_code = mDBHelper.getDivisionsCodeByName(label);
                ido=idd=ids=-1;
                labelDivision2 = (TextView) findViewById(R.id.labelDivision2);
                labelDivision2.setText(label);
                try {
                    loadOpers_spinnerData();
                    opers_select_label = (TextView) findViewById(R.id.select_label);
                    opers_select_label.setText(spinner.getItemAtPosition(0).toString());}
                catch (Exception e){
                    Log.d(TAG, "Error : " + e.getMessage());
                }
                try {
                    loadSpinnerData();
                    select_label = (TextView) findViewById(R.id.select_label);
                    select_label.setText(spinner.getItemAtPosition(0).toString());}
                catch (Exception e){
                    Log.d(TAG, "Error : " + e.getMessage());
                }
                try {
                    loadSpinnerSotrData();
                    labelSotr = (TextView) findViewById(R.id.labelSotr);
                    labelSotr.setText(spinnerSotr.getItemAtPosition(0).toString());}
                catch (Exception e){
                    Log.d(TAG, "Error : " + e.getMessage());
                }
                //MessageUtils.showToast(getApplicationContext(),"Вы выбрали: " + label, false);
            }
        }

        if(sp.getId() == R.id.opers_spinner) {
            if (position != 0) {
                String label = parent.getItemAtPosition(position).toString();
//Выбрать _id Opers и записать в Defs;
                ido = mDBHelper.getOpers_id_by_Name(label);
                opers_select_label = (TextView) findViewById(R.id.opers_select_label);
                opers_select_label.setText(label);
               // Showing selected spinner item
                //messageUtils.showMessage(getApplicationContext(),"Вы выбрали: " + label);
                try {
                    loadSpinnerData();
                    select_label = (TextView) findViewById(R.id.select_label);
                    select_label.setText(spinner.getItemAtPosition(0).toString());}
                catch (Exception e){
                    Log.d(TAG, "Error : " + e.getMessage());
                }
                try {
                    loadSpinnerSotrData();
                    labelSotr = (TextView) findViewById(R.id.labelSotr);
                    labelSotr.setText(spinnerSotr.getItemAtPosition(0).toString());}
                catch (Exception e){
                    Log.d(TAG, "Error : " + e.getMessage());
                }
            }
        }

        if(sp.getId() == R.id.spinner) {
            if (position != 0) {
               String mlabel = parent.getItemAtPosition(position).toString();
//Выбрать _id Deps и записать в Defs;
                idd = mDBHelper.getDeps_id_by_Name(mlabel);
                select_label = (TextView) findViewById(R.id.select_label);
                select_label.setText(mlabel);
               // Showing selected spinner item
                //messageUtils.showMessage(getApplicationContext(), "Вы выбрали: " + mlabel);
                ids=-1;
                try {
                    loadSpinnerSotrData();
                    labelSotr = (TextView) findViewById(R.id.labelSotr);
                    labelSotr.setText(spinnerSotr.getItemAtPosition(0).toString());}
                catch (Exception e){
                    Log.e(TAG, "Deps selection -> ", e);
                }
            }
        }
        if(sp.getId() == R.id.spinnerSotr) {
            if (position != 0) {
                String slabel = parent.getItemAtPosition(position).toString();
                //Выбрать _id Sotr и записать в Defs;
                ids = mDBHelper.getSotr_id_by_Name(slabel);
                if (ids != 0) {
                    labelSotr = (TextView) findViewById(R.id.labelSotr);
                    labelSotr.setText(slabel);
                }else{
                    MessageUtils.showToast(getApplicationContext(),"Ошибка при поиске выбранного сотрудника!", false);
                    Log.w(TAG, "Ошибка при поиске выбранного сотрудника!"+slabel);
                }
            } else {
                if (idd != 0) {
                    position = 1;
                    try {
                        String slabel = parent.getItemAtPosition(position).toString();
                        //Выбрать _id Sotr и записать в Defs;
                        ids = mDBHelper.getSotr_id_by_Name(slabel);
                        if (ids != 0) {
                            labelSotr = (TextView) findViewById(R.id.labelSotr);
                            labelSotr.setText(slabel);
                        }else{
                            Log.w(TAG, "Ошибка при поиске выбранного сотрудника!"+slabel);
                        }
                    }
                    catch (Exception e){
                        Log.d(TAG, "Error : " + e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }
    public void ocl_bSave(View v) {
        if (ido != mDBHelper.defs.get_Id_o()) {
            if (mDBHelper.currentOutDoc == null) {
                mDBHelper.currentOutDoc = new OutDocs("", 0,0, "", "01.01.2018 00:00:00",
                        mDBHelper.defs.getDivision_code(), mDBHelper.defs.get_idUser(),
                        isDepAndSotrOper(mDBHelper.defs.get_Id_o()) ? mDBHelper.defs.get_Id_s() : 0,
                        isDepAndSotrOper(mDBHelper.defs.get_Id_o()) ? mDBHelper.defs.get_Id_d() : 0);
            } else {
                mDBHelper.currentOutDoc.set_id("");
                mDBHelper.currentOutDoc.set_number(0);
                mDBHelper.currentOutDoc.set_comment("");
                mDBHelper.currentOutDoc.set_DT("01.01.2018 00:00:00");
                mDBHelper.currentOutDoc.set_Id_o(0);
                mDBHelper.currentOutDoc.set_sentToMasterDate(null);
                mDBHelper.currentOutDoc.setDivision_code("0");
                mDBHelper.currentOutDoc.setIdUser(0);
                mDBHelper.currentOutDoc.setIdSotr(0);
                mDBHelper.currentOutDoc.setIdDeps(0);
            }
        }

        if (!AppUtils.isNotEmpty(division_code)) {
            division_code="0";
            idd = 0; ids = 0; ido = 0;
        }else{
            if (ido==0) ido = mDBHelper.defs.get_Id_o();
            if (ido==-1) idd = 0;
            if (idd==0) idd = mDBHelper.defs.get_Id_d();
            if (idd==-1) idd = 0;
            if (ids==0) ids = mDBHelper.defs.get_Id_s();
            if (ids==-1) ids = 0;
        }
        String ip = host_v.getText().toString();

        Defs defs = new Defs(idd, ido, ids, ip, "4242",division_code,mDBHelper.defs.getDeviceId());
        if (mDBHelper.updateDefsTable(defs) != 0) {
            MessageUtils.showToast(getApplicationContext(),"Сохранено.", false);
        } else {
            MessageUtils.showToast(getApplicationContext(),"Ошибка при сохранении.", false);
        }
        mDBHelper.selectDefsTable();
    }

    private class SyncIncoData extends AsyncTask<String, Integer, String> {
        Integer counter;

        @Override
        protected String doInBackground(String... urls) {
            counter = 0;
            try {
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
                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getOperation("01.01.2018 00:00:00").enqueue(new Callback<List<Operation>>() {
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

                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getDeps("01.01.2018 00:00:00").enqueue(new Callback<List<Deps>>() {
                    @Override
                    public void onResponse(Call<List<Deps>> call, Response<List<Deps>> response) {
                        if (response.isSuccessful()) {
                            for (Deps deps : response.body())
                                mDBHelper.insertDeps(deps);
                            if (response.body().size() != 0)
                                Log.d("UpdateActivity", "Ок, новые бригады приняты");

                        }
                        counter = counter + 5;
                        publishProgress(2);
                    }

                    @Override
                    public void onFailure(Call<List<Deps>> call, Throwable t) {
                        Log.d("UpdateActivity", "Ответ сервера на запрос новых бригад: " + t.getMessage());
                    }
                });

                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getSotr("01.01.2018 00:00:00").enqueue(new Callback<List<Sotr>>() {
                    @Override
                    public void onResponse(Call<List<Sotr>> call, Response<List<Sotr>> response) {
                        //Log.d("1", "Ответ сервера на запрос новых сотрудников: " + response.body());
                        if (response.isSuccessful()) {
                            for (Sotr sotr : response.body())
                                mDBHelper.insertSotr(sotr);
                            if (response.body().size() != 0) {
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

            } catch (Exception e) {
                Log.d("1", "Error : " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            MessageUtils.showToast(getApplicationContext(), "Обновление продолжается... Подождите...", true);
        }
    }
}