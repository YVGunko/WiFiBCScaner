package com.example.yg.wifibcscaner;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Spinner;

import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.Defs;
import com.example.yg.wifibcscaner.data.model.Deps;
import com.example.yg.wifibcscaner.data.model.Operation;
import com.example.yg.wifibcscaner.data.model.OutDocs;
import com.example.yg.wifibcscaner.data.model.Sotr;
import com.example.yg.wifibcscaner.data.model.user;
import com.example.yg.wifibcscaner.data.repository.BaseClassRepo;
import com.example.yg.wifibcscaner.data.repository.OrderOutDocBoxMovePartRepository;
import com.example.yg.wifibcscaner.interfaces.FetchListDataListener;
import com.example.yg.wifibcscaner.utils.ApiUtils;
import com.example.yg.wifibcscaner.utils.MessageUtils;
import com.example.yg.wifibcscaner.service.PartBoxService;
import com.example.yg.wifibcscaner.utils.SharedPreferenceManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {

    private static final String TAG = "SettingsActivity";

    private OrderOutDocBoxMovePartRepository orderOutDocBoxMovePartRepository;
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

        orderOutDocBoxMovePartRepository = new OrderOutDocBoxMovePartRepository();

        mDBHelper = DataBaseHelper.getInstance(this);
        host_v=(EditText) findViewById(R.id.host);

        String devId = "Unknown";
        try {
            devId = ((mDBHelper.defs.getDeviceId().substring(0,6).length()==6) ? mDBHelper.defs.getDeviceId().substring(0,5) : "Unknown");}
        catch (Exception e) {
            devId = "unKnown";
        }

        strTitle = "Настройки"+". v."+BuildConfig.VERSION_NAME+"."+BuildConfig.VERSION_CODE+". Id."+ devId;

        final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
        animation.setDuration(500); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in

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

        MessageUtils messageUtils = new MessageUtils();

        mDBHelper.selectDefsTable();
        try {
            String url = mDBHelper.defs.getUrl();
            boxesService = ApiUtils.getBoxesService(url);
            host_v.setText(mDBHelper.defs.get_Host_IP());
            ocl_check(findViewById(R.id.check));
        }
        catch(Exception e){
            messageUtils.showMessage(getApplicationContext(),e.getMessage());
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
        MessageUtils messageUtils = new MessageUtils();
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();
        // Операции для выбранного пункта меню
        switch (id) {
            case R.id.action_receive_spr:
                new BaseClassRepo().getData();

                return true;
            case R.id.action_receive_box:
                try {
                    orderOutDocBoxMovePartRepository.getData();

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
            default:
                return super.onOptionsItemSelected(item);
        }

    }
    private void resetApplication() {
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(
                getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
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
                    SharedPreferenceManager.getInstance().setDbNeedReplace(true);
                    triggerRebirth(SettingsActivity.this);
                } else
                {
                    MessageUtils messageUtils = new MessageUtils();
                    messageUtils.showLongMessage(getApplicationContext(),"Операция не выполнена!");
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
        MessageUtils messageUtils = new MessageUtils();
        messageUtils.showLongMessage(getApplicationContext(),"Connecting please wait....");
        checkConnection();
    }

    public void checkConnection() {
            String url = mDBHelper.defs.getUrl();
            boxesService = ApiUtils.getBoxesService(url);
            boxesService.checkConnection().enqueue(new Callback<Object>() {

                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    MessageUtils messageUtils = new MessageUtils();
                    if(!response.isSuccessful()) {
                        messageUtils.showLongMessage(getApplicationContext(),"Введенный URL недоступен! Введите верный!");
                        host_v.requestFocus();
                    }else {
                        messageUtils.showLongMessage(getApplicationContext(),"Соединение установлено!");
                    }
                }
                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    MessageUtils messageUtils = new MessageUtils();
                    messageUtils.showLongMessage(getApplicationContext(),"Введенный URL недоступен! Введите верный!");
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
        MessageUtils messageUtils = new MessageUtils();
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
                messageUtils.showMessage(getApplicationContext(),"Вы выбрали: " + label);
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
                messageUtils.showMessage(getApplicationContext(),"Вы выбрали: " + label);
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
                messageUtils.showMessage(getApplicationContext(), "Вы выбрали: " + mlabel);
                ids=-1;
                try {
                    loadSpinnerSotrData();
                    labelSotr = (TextView) findViewById(R.id.labelSotr);
                    labelSotr.setText(spinnerSotr.getItemAtPosition(0).toString());}
                catch (Exception e){
                    Log.d(TAG, "Error : " + e.getMessage());
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
                    // Showing selected spinner item
                    messageUtils.showMessage(getApplicationContext(),"Вы выбрали: " + slabel);
                    //mDBHelper.defs.descSotr = slabel;
                }else{
                    messageUtils.showMessage(getApplicationContext(),"Ошибка при поиске ID выбранного сотрудника!");
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
                            // Showing selected spinner item
                            messageUtils.showMessage(getApplicationContext(),"Вы выбрали: " + slabel);
                            //mDBHelper.defs.descSotr = slabel;
                        }else{
                            messageUtils.showMessage(getApplicationContext(),"Ошибка при поиске ID выбранного сотрудника!");
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
                        null, mDBHelper.defs.getDivision_code(), mDBHelper.defs.get_idUser());
            } else {
                mDBHelper.currentOutDoc.set_id("");
                mDBHelper.currentOutDoc.set_number(0);
                mDBHelper.currentOutDoc.set_comment("");
                mDBHelper.currentOutDoc.set_DT("01.01.2018 00:00:00");
                mDBHelper.currentOutDoc.set_Id_o(0);
                mDBHelper.currentOutDoc.set_sentToMasterDate(null);
                mDBHelper.currentOutDoc.setDivision_code("0");
                mDBHelper.currentOutDoc.setIdUser(0);
            }
        }
        MessageUtils messageUtils = new MessageUtils();

        if ((division_code == null)||(new String("0").equals(division_code))) {
            //division_code=mDBHelper.defs.getDivision_code();
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
            messageUtils.showMessage(getApplicationContext(),"Сохранено.");
        } else {
            messageUtils.showMessage(getApplicationContext(),"Ошибка при сохранении.");
        }
        mDBHelper.selectDefsTable();
        //loadSpinnerData();
        //loadSpinnerSotrData();
        checkConnection();
    }

    private class SyncIncoData extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getOrdersId(null, mDBHelper.defs.getDivision_code()).enqueue(new Callback<List<String>>() {
                    // TODO Обработать результат. Записать поле sent... если успешно
                    @Override
                    public void onResponse(Call<List<String>> call, Response<List<String>> response) {

                        if (response.isSuccessful()) {
                            assert response.body() != null;
                            publishProgress(response.body().size());
                            int totalChunks = (int)Math.ceil((double)response.body().size()/10);
                            int index = 0;
                            for (String o : response.body()) {

                                mDBHelper.saveOrderNotFound(o);
                                index +=1;

                                if ((index % totalChunks) == 0) publishProgress(index * 10 / totalChunks );
                            }
                            if (response.body().size() != 0)
                                Log.d("UpdateActivity", "Ок! Список заказов принят!");
                        }

                    }

                    @Override
                    public void onFailure(Call<List<String>> call, Throwable t) {
                        Log.d(TAG, "Ответ сервера на запрос новых операций: " + t.getMessage());
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error : " + e.getMessage());
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
            MessageUtils messageUtils = new MessageUtils();
            messageUtils.showLongMessage(getApplicationContext(), "Обновление продолжается... Выполнено "+values[0]+"%");
        }
    }
}