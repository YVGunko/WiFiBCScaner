package com.example.yg.wifibcscaner;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yg.wifibcscaner.activity.BaseActivity;
import com.example.yg.wifibcscaner.activity.BoxesActivity;
import com.example.yg.wifibcscaner.activity.LoginActivity;
import com.example.yg.wifibcscaner.activity.OutDocsActivity;
import com.example.yg.wifibcscaner.activity.ProdsActivity;
import com.example.yg.wifibcscaner.activity.UpdateActivity;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.BoxMoves;
import com.example.yg.wifibcscaner.data.model.Boxes;
import com.example.yg.wifibcscaner.data.model.OrdersActivity;
import com.example.yg.wifibcscaner.data.model.OutDocs;
import com.example.yg.wifibcscaner.data.model.Prods;
import com.example.yg.wifibcscaner.receiver.Config;
import com.example.yg.wifibcscaner.receiver.SyncDataBroadcastReceiver;
import com.example.yg.wifibcscaner.utils.ApiUtils;
import com.example.yg.wifibcscaner.utils.MessageUtils;
import com.example.yg.wifibcscaner.data.dto.OrderWithOutDocWithBoxWithMovesWithPartsResponce;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.ScannerNotClaimedException;
import com.honeywell.aidc.ScannerUnavailableException;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import me.drakeet.support.toast.ToastCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.text.TextUtils.substring;


public class MainActivity extends BaseActivity implements BarcodeReader.BarcodeListener {
    private static final String TAG = "MainActivity";

    private static BarcodeReader barcodeReader; //honeywell
    private AidcManager manager;
    boolean bCancelFlag;
    UsbManager mUsbManager = null;
    UsbDevice mdevice;
    //IntentFilter filterAttached_and_Detached = null;

    public static final long LOAD_TIMEOUT = 60000; // 1 min = 1 * 60 * 1000 ms


    //

    private int sId_o = 1;
    private int sId_d = 1;
    private DataBaseHelper mDBHelper;
    TextView tVDBInfo, currentDocDetails, currentUser;
    ListView lvCurrentDoc;
    EditText editTextRQ, barCodeInput;
    Button bScan;
    DataBaseHelper.foundbox fb;
    DataBaseHelper.foundorder fo;

    private String getDeviceUniqueID(Activity activity){
        String device_unique_id = Settings.Secure.getString(activity.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return device_unique_id;
    }

    @Override
    protected void onNetworkChange(boolean isConnected) {

    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        if (!checkFirstRun()) mDBHelper = DataBaseHelper.getInstance(this);

        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        tVDBInfo = (TextView) findViewById(R.id.tVDBInfo);
        currentDocDetails  = (TextView) findViewById(R.id.currentDocDetails);
        currentUser  = (TextView) findViewById(R.id.currentUser);
        tVDBInfo.setText(mDBHelper.lastBox());
        editTextRQ = (EditText) findViewById(R.id.editTextRQ);
        editTextRQ.setEnabled(false);
        //registerReceiver
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        /*
        filterAttached_and_Detached = new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        filterAttached_and_Detached.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filterAttached_and_Detached.addAction(ACTION_USB_PERMISSION);
        //
        registerReceiver(barcodeDataReceiver, filterAttached_and_Detached);
        //scaner detect
        extScanerDetect();
        */
        AidcManager.create(this, new AidcManager.CreatedCallback() {
            @Override
            public void onCreated(AidcManager aidcManager) {
                manager = aidcManager;
                barcodeReader = manager.createBarcodeReader();
                try {
                    if(barcodeReader!=null) {
                        Log.d("honeywellscanner: ", "barcodereader not claimed in OnCreate()");
                        barcodeReader.claim();
                    }
                }
                catch (ScannerUnavailableException e) {
                    showMessage("Невозможно включить встроенный сканер.");
                    //e.printStackTrace();
                }
                // register bar code event listener
                barcodeReader.addBarcodeListener(MainActivity.this);
            }
        });

    }

    private boolean checkFirstRun() {
        // Get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;

        // Get saved version code
        SharedPreferences prefs = getSharedPreferences(SharedPrefs.PREFS_NAME, MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(SharedPrefs.PREF_VERSION_CODE_KEY, SharedPrefs.DOESNT_EXIST);
        boolean savedDbNeedReplace = prefs.getBoolean(SharedPrefs.PREF_DB_NEED_REPLACE, SharedPrefs.DOESNT_EXIST==-1);

        // Check for first run or upgrade
        if (!savedDbNeedReplace & currentVersionCode == savedVersionCode) {
            // This is just a normal run
            return false;

        } else {
            mDBHelper = DataBaseHelper.getInstance(this, currentVersionCode, true);
            prefs.edit().putInt(SharedPrefs.PREF_VERSION_CODE_KEY, currentVersionCode).apply();
            prefs.edit().putBoolean(SharedPrefs.PREF_DB_NEED_REPLACE, !savedDbNeedReplace).apply();
            return true;
        }
    }

    @Override
    public void onStop(){
        super.onStop();

        //stopLoadDataTimer();

        if(barcodeReader!=null)
            barcodeReader.release();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //resetLoadDataTimer();

        String snum = "Накл.???";
        if (mDBHelper.currentOutDoc.get_number() != 0) snum = "Накл.№"+mDBHelper.currentOutDoc.get_number();
        snum = mDBHelper.defs.descOper+", "+snum;
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(Html.fromHtml("<font color='#FFBF00'>"+snum+"</font>"));
        actionBar.setTitle("Подразделение: "+mDBHelper.defs.descDivision);

        tVDBInfo = (TextView) findViewById(R.id.tVDBInfo);
        tVDBInfo.setText(mDBHelper.lastBox());

        currentDocDetails  = (TextView) findViewById(R.id.currentDocDetails);
        currentDocDetails.setText("Накл.№" +mDBHelper.currentOutDoc.get_number() + ", " + mDBHelper.selectCurrentOutDocDetails(mDBHelper.currentOutDoc.get_id()));

        currentUser  = (TextView) findViewById(R.id.currentUser);
        currentUser.setText("Пользователь: " +mDBHelper.getUserName(mDBHelper.defs.get_idUser()));

        if (barcodeReader != null) {
            try {
                barcodeReader.claim();
                Log.d("honeywellscanner: ", "scanner claimed");
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                showMessage("Встроенный сканер не включается!");
            }
        }
    }


    public void ocl_scan(View v) { //Вызов активности Сканирования
        if (mDBHelper.defs.get_idUser()==0){
            showLongMessage("Нужно войти в систему...");
            startActivity(new Intent(this, LoginActivity.class));
            //if not a superuser check for current user today's outdoc and add new one if not exist.
            return;
        }
        if ((mDBHelper.defs.get_Id_o()==0)||(new String("0").equals(mDBHelper.defs.getDivision_code())))
        {   //Операция не выбрана
            showLongMessage("Нужно зайти в настройки и выбрать операцию, подразделение...");
            startActivity(new Intent(this,SettingsActivity.class));  //Вызов активности Коробки
            return;
        }
        if ((mDBHelper.defs.get_Id_o()==mDBHelper.defs.get_idOperLast())&(
                (mDBHelper.currentOutDoc.get_id()==null)||
                        (mDBHelper.currentOutDoc.get_number()==0)))
        {
            //Отгрузка, накладная не выбрана
            startActivity(new Intent(this, OutDocsActivity.class));
            return;
        }
        if ((mDBHelper.defs.get_Id_o()!=mDBHelper.defs.get_idOperLast())&(
                (mDBHelper.defs.get_Id_d()==0)||
                (mDBHelper.defs.get_Id_s()==0)||
                (mDBHelper.currentOutDoc.get_id()==null)||
                (mDBHelper.currentOutDoc.get_number()==0)))
        {
            if ((mDBHelper.currentOutDoc.get_id()==null)||(mDBHelper.currentOutDoc.get_number()==0))
            {
                //showLongMessage("Нужно выбрать или создать накладную...");
                startActivity(new Intent(this,OutDocsActivity.class));

            } else {
                showLongMessage("Нужно зайти в настройки и выбрать бригаду и сотрудника...");
                startActivity(new Intent(this,SettingsActivity.class));  //Вызов активности Коробки
            }
            return;
        }

        String snum = "Накл.???";
        if (mDBHelper.currentOutDoc.get_number() != 0) snum = "Накл.№"+mDBHelper.currentOutDoc.get_number();
        snum = mDBHelper.defs.descOper+", "+snum;
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(Html.fromHtml("<font color='#FFBF00'>"+snum+"</font>"));
        actionBar.setTitle("Подразделение: "+mDBHelper.defs.descDivision);
        //====this.setTitle(mDBHelper.defs.descOper+", "+snum);

        /* Если сканер подключен - вызывать обработчик для него*/
        final     EditText            input = (EditText) findViewById(R.id.barCodeInput);
        if (editTextRQ.isEnabled()==true){
            //input.setEnabled(false);
            ocl_bOk(v);
        }else {
        if (mdevice != null){//внешний usb сканер
            showMessage("Режим работы с внешним сканером.");
            input.setEnabled(true);
            input.requestFocus();
            //input.setInputType(InputType.TYPE_NULL);
            input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // nothing
                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String currentbarcode = input.getText().toString();
                    if( currentbarcode.indexOf("\n") > 0) {
                        currentbarcode = substring(currentbarcode,0,currentbarcode.indexOf("\n"));
                        showMessage(currentbarcode);
                        scanResultHandler(currentbarcode);
                        input.setText("");
                        input.requestFocus();
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {
                    // nothing
                }
            });
        }else {//камера устройства
            input.setEnabled(false);
            if(barcodeReader!=null){
                //showMessage("Режим работы со встроенным сканером.");
                try {
                    barcodeReader.softwareTrigger(true);
                } catch (ScannerNotClaimedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (ScannerUnavailableException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            else{
                showMessage("Режим работы с камерой.");
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.initiateScan();
            }
        }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Операции для выбранного пункта меню
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this,SettingsActivity.class));  //Вызов активности
                return true;
            case R.id.action_login:
                startActivity(new Intent(this,LoginActivity.class));  //Вызов активности
                return true;
            case R.id.action_boxes:
                startActivity(new Intent(this, BoxesActivity.class)); //Вызов активности Коробки
                return true;
            case R.id.action_orders:
                startActivity(new Intent(this, OrdersActivity.class));
                return true;
            case R.id.action_prods:
                startActivity(new Intent(this, ProdsActivity.class));
                return true;
            case R.id.action_test:
                startActivity(new Intent(this,OutDocsActivity.class));
                return true;
            case R.id.action_update:
                startActivity(new Intent(this, UpdateActivity.class));
                return true;
            case R.id.action_db_need_replace:
                try {
                    openDbReplaceDialog();
                } catch (Exception e) {
                    Log.d(mDBHelper.LOG_TAG, "Запрос на очистку БД: " + e.getMessage());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void openDbReplaceDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                MainActivity.this);
        final boolean curState = SharedPrefs.getDefaults(SharedPrefs.PREF_DB_NEED_REPLACE, MainActivity.this);
        quitDialog.setTitle("Очистить базу данных: Вы уверены? CurState ".concat(String.valueOf(curState)));

        quitDialog.setPositiveButton("Да!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPrefs.setDefaults(SharedPrefs.PREF_DB_NEED_REPLACE, true, MainActivity.this);
            }
        });

        quitDialog.setNegativeButton("Нет.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });
        quitDialog.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(Build.VERSION.SDK_INT > 11) {
            boolean checkSuper= mDBHelper.checkSuperUser(mDBHelper.defs.get_idUser());
            invalidateOptionsMenu();
            menu.findItem(R.id.action_settings).setVisible(checkSuper);
            menu.findItem(R.id.action_orders).setVisible(checkSuper);
        }
        return super.onPrepareOptionsMenu(menu);
    }
private static String filter (String str){
        StringBuilder filtered = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char current = str.charAt(i);
            if (current >= 0x2E && current <= 0x39) {
                filtered.append(current);
            }
        }
        return filtered.toString();
}
    private void scanResultHandler (String currentbarcode) {
            /*---Тут нужно данные коробки вывести и дать отредактировать количество. Есть код в currentbarcode.
            Нужно его обработать, выбрать данные новой коробки для вывода tVDBInfo и в editTextRQ
            * Если данные новой коробки не нашли в заказах - сообщить и ничего не выводить*/
            //поискать символ с кодом 194
        currentbarcode = filter(currentbarcode);
        if ((currentbarcode.length()<20)&&(StringUtils.countMatches(currentbarcode,'.')!=5)) {
            showMessage("QR-код не распознан.");
            return;
        }
        fo = mDBHelper.searchOrder(currentbarcode);
        if (!fo.archive) { // архив
            if (fo._id != 0) {                                      //Заказ найден, ищем коробку
                fb = mDBHelper.searchBox(fo._id, currentbarcode);
                //---Получаем строку данных о коробке для вывода в tVDBInfo и количество для редактирования
                tVDBInfo = (TextView) findViewById(R.id.tVDBInfo);
                if (!fb.boxdef.equals(""))
                    fo.orderdef += fb.boxdef+ "\n";
                if (!fb.depSotr.equals(""))
                    fo.orderdef += fb.depSotr+ "\n";
                if (!fb.outDocs.equals(""))
                    fo.orderdef += fb.outDocs;
                tVDBInfo.setText(fo.orderdef);
                if (!fb._archive){
                    if ((!fb._id.equals("")&&(fb._id != null))) {                                  //Коробка есть
                        if (fb.QB == fb.RQ) {//Коробка заполнена

                            editTextRQ = (EditText) findViewById(R.id.editTextRQ);
                            editTextRQ.setText(String.valueOf(fb.QB - fb.RQ));
                            editTextRQ.setEnabled(false);
                            if (mDBHelper.defs.get_Id_o()==mDBHelper.defs.get_idOperFirst()) showLongMessage("Эта коробка уже принята полной!");
                            else showLongMessage("Эта коробка уже отгружена!");
                        } else {
                            Button bScan = (Button) findViewById(R.id.bScan);
                            bScan.setText("OK!");
                            editTextRQ = (EditText) findViewById(R.id.editTextRQ);
                            editTextRQ.setText(String.valueOf(fb.QB - fb.RQ));
                            editTextRQ.setEnabled(true);
                            if (fb.RQ != 0) {showMessage("Эта коробка ранее принималась неполной!");}
                        }
                    } else {                                                //Коробки нет , подставить колво в поле редактирования колва и дожаться ОК.
                        if (mDBHelper.defs.get_Id_o()==mDBHelper.defs.get_idOperFirst()){ //Добавить коробку если это операция приемки baseOper = 1
                            Button bScan = (Button) findViewById(R.id.bScan);
                            bScan.setText("OK!");
                            editTextRQ = (EditText) findViewById(R.id.editTextRQ);
                            editTextRQ.setText(String.valueOf(fb.QB - fb.RQ));
                            editTextRQ.setEnabled(true);
                        }else{
                            showLongMessage("Эта коробка не принималась на производстве!");
                        }
                    }
                }else {
                    showLongMessage("Эта коробка уже в архиве! Никакие операции невозможны!");
                }
            } else {
                showLongMessage("Заказ для этой коробки не загружен! Будет загружен автоматически при подключении к WiFi.");
                //TODO 1. make request to server to load the order if not save the order and load it later
                saveOrderNotFoundAsync save = new saveOrderNotFoundAsync();
                String response = null;
                try {
                    response = save.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                            mDBHelper.getOrder_id(currentbarcode)).get();
                    Toast.makeText(getApplicationContext(), "response = " + response, Toast.LENGTH_LONG).show();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                loadOrderAsync task = new loadOrderAsync();
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        mDBHelper.getOrder_id(currentbarcode));
            }
        } else {
            showLongMessage("Этот заказ уже в архиве! Никакие операции невозможны!");
        }
    }

    public void ocl_bOk(View v) { //Вызов активности Сканирования
        int idar = 0;
        int iRQ = 0;
        boolean newBM = false;

        String _RQ = editTextRQ.getText().toString();
        if ((!TextUtils.isEmpty(_RQ))&(Integer.valueOf(_RQ)<=(fb.QB - fb.RQ))) {//колво  не пустое
            try {
                Button bScan = (Button) findViewById(R.id.bScan);
                bScan.setText("Scan!");
                iRQ = Integer.valueOf(_RQ);
                tVDBInfo = (TextView) findViewById(R.id.tVDBInfo);
                editTextRQ = (EditText) findViewById(R.id.editTextRQ);
                editTextRQ.setEnabled(false);
                if ((!fb._id.equals("")&&(fb._id != null))) {                                            //коробка есть и не полная, добавить в prods
                    if (fb.RQ == 0) {
                        newBM = true;                                           //новая операция по существующей коробке
                    }
                    fb.RQ = iRQ;
                    if (mDBHelper.addProds(fb)) {
                        if (newBM){
                            //showMessage(mDBHelper.defs.descOper+". Обработана новая коробка.");
                            newBM=false;
                        }else {
                            showMessage(mDBHelper.defs.descOper+". В коробку добавлено "+String.valueOf(iRQ));
                        }
                        tVDBInfo = (TextView) findViewById(R.id.tVDBInfo);
                        tVDBInfo.setText(mDBHelper.lastBox());
                        currentDocDetails  = (TextView) findViewById(R.id.currentDocDetails);
                        currentDocDetails.setText("Накл.№" +mDBHelper.currentOutDoc.get_number() + ", " + mDBHelper.selectCurrentOutDocDetails(mDBHelper.currentOutDoc.get_id()));
                        if (mDBHelper.lastBoxCheck(fo)) showLongMessage("Это последняя коробка из заказа!");
                    }else {
                        showLongMessage(mDBHelper.defs.descOper+". Повторный прием коробки в смену! Повторный прием возможен в другую смену.");
                    }

                }else {
                    if (!mDBHelper.addBoxes(fo,iRQ)) {            //---Вызов метода добавления коробки и продс
                        showLongMessage(mDBHelper.defs.descOper+". Ошибка! Коробка не добавлена в БД!");
                    } else {
                        tVDBInfo = (TextView) findViewById(R.id.tVDBInfo);
                        tVDBInfo.setText(mDBHelper.lastBox());
                        currentDocDetails  = (TextView) findViewById(R.id.currentDocDetails);
                        currentDocDetails.setText("Накл.№" +mDBHelper.currentOutDoc.get_number() + ", " + mDBHelper.selectCurrentOutDocDetails(mDBHelper.currentOutDoc.get_id()));

                        //showMessage(mDBHelper.defs.descOper+". Принята новая коробка.");
                        if (mDBHelper.lastBoxCheck(fo)) showLongMessage("Это последняя коробка из заказа!");
                    }
                }
                if (mdevice != null){//внешний usb сканер
                    final     EditText            input = (EditText) findViewById(R.id.barCodeInput);
                    input.requestFocus();
                }
            } catch (Exception e) {
                Log.e(mDBHelper.LOG_TAG, mDBHelper.defs.descOper+". Ошибка при получении количества в коробке!", e);
                showMessage(mDBHelper.defs.descOper+". Ошибка! Невозможно получить введенное количество!");
            }
        }else {
            showLongMessage("Ошибка! Введите количество верно!");
        }
    }

    public void ocl_boxes(View v) {
        startActivity(new Intent(this,BoxesActivity.class)); //Вызов активности Коробки
    }
    public void ocl_orders(View v) {
        startActivity(new Intent(this,OrdersActivity.class)); //Вызов активности Коробки
    }
    public void ocl_prods(View v) {
        startActivity(new Intent(this, ProdsActivity.class)); //Вызов активности Коробки
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mDBHelper.defs.getDeviceId().isEmpty() || mDBHelper.defs.getDeviceId() == null || mDBHelper.defs.getDeviceId().contentEquals("0")) {
            String DeviceId = getDeviceUniqueID(this);
            mDBHelper.defs.setDeviceId(DeviceId);
        }

        if (mDBHelper.checkIfUserTableEmpty()) {
            startActivity(new Intent(this,SettingsActivity.class));
            return;
        } else {
            if (mDBHelper.defs.get_idUser()==0) {
                startActivity(new Intent(this, LoginActivity.class));
            }
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult.getContents() != null) {
            // handle scan result
            String currentbarcode = scanResult.getContents();
            scanResultHandler(currentbarcode);
        }else{
            showMessage("Ошибка сканера !");
        }
    }

    @Override
    public void onBarcodeEvent(final BarcodeReadEvent event) {
        try {
            barcodeReader.softwareTrigger(false);
        } catch (ScannerNotClaimedException e) {
            e.printStackTrace();
        } catch (ScannerUnavailableException e) {
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (event.getBarcodeData() != null) {
                    // handle scan result
                    String currentbarcode = event.getBarcodeData();
                    scanResultHandler(currentbarcode);
                }else{
                    showMessage("Ошибка сканера !");
                }
            }
        });
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent arg0) {
        try {
            barcodeReader.softwareTrigger(false);
        } catch (ScannerNotClaimedException e) {
            e.printStackTrace();
        } catch (ScannerUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(barcodeDataReceiver);

        if (barcodeReader != null) {
            // close BarcodeReader to clean up resources.
            barcodeReader.close();
            barcodeReader = null;
        }

        if (manager != null) {
            // close AidcManager to disconnect from the scanner service.
            // once closed, the object can no longer be used.
            manager.close();
        }

    }

    private void showMessage (String s){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        if (android.os.Build.VERSION.SDK_INT >= 25) {
            ToastCompat.makeText(context, s, duration)
                        .setBadTokenListener(toast -> {
                            Log.e("failed toast",s);
                        }).show();
        } else {
            Toast.makeText(context, s, duration).show();
        }
    }
    private void showLongMessage (String s){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;
        if (android.os.Build.VERSION.SDK_INT >= 25) {
            ToastCompat.makeText(context, s, duration)
                    .setBadTokenListener(toast -> {
                        Log.e("failed toast",s);
                    }).show();
        } else {
            Toast.makeText(context, s, duration).show();
        }
    }
    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        if (editTextRQ.isEnabled()){
            openCancelDialog();
        }
        else {
            openQuitDialog();
        }
    }
    private void openQuitDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                MainActivity.this);
        quitDialog.setTitle("Выход: Вы уверены?");

        quitDialog.setPositiveButton("Да!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        quitDialog.setNegativeButton("Нет.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });
        quitDialog.show();
    }
    private void openCancelDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                MainActivity.this);
        quitDialog.setTitle("Отменить: Вы уверены?");

        quitDialog.setPositiveButton("Да!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Button bScan = (Button) findViewById(R.id.bScan);
                bScan.setText("Scan!");
                editTextRQ = (EditText) findViewById(R.id.editTextRQ);
                editTextRQ.setEnabled(false);
            }
        });

        quitDialog.setNegativeButton("Нет.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editTextRQ.requestFocus();
            }
        });
        quitDialog.show();
    }
    // version 3.5.22
    private class loadOrderAsync extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                ArrayList<String> OrdersId = new ArrayList<String>();
                bCancelFlag = false;
                Log.i("loadOrderAsync", " bCancelFlag was set to: "+bCancelFlag);
                if (strings !=null && strings.length != 0)
                    OrdersId.add(strings[0]);
                else
                    OrdersId.addAll(mDBHelper.getOrdersNotFound());
                for (String s : OrdersId) {
                    if (isCancelled() || bCancelFlag) {
                        Log.i("loadOrderAsync", "Breaked. isCanceled="+isCancelled()+" bCancelFlag"+bCancelFlag);
                        break;
                    }
                    ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getOrder(s)
                            .enqueue(new Callback<OrderWithOutDocWithBoxWithMovesWithPartsResponce>() {
                                @Override
                                public void onResponse(Call<OrderWithOutDocWithBoxWithMovesWithPartsResponce> call,
                                                       Response<OrderWithOutDocWithBoxWithMovesWithPartsResponce> response) {
                                    if (response.isSuccessful()) {
                                        //save order, boxes, boxMoves, partBox
                                        if (response.body().getOrder() != null) {
                                            mDBHelper.insertOrders(response.body().getOrder());
                                            for (OutDocs od : response.body().getOutDocReqList())
                                                mDBHelper.insertOrUpdateOutDocs(od);
                                            for (Boxes boxes : response.body().getBoxReqList())
                                                mDBHelper.insertBoxes(boxes);
                                            for (BoxMoves bm : response.body().getMovesReqList())
                                                mDBHelper.insertBoxMoves(bm);
                                            for (Prods pb : response.body().getPartBoxReqList())
                                                mDBHelper.insertProds(pb);
                                            Log.d("loadOrderAsync", "Order and stuff have already been saved.");
                                            //delete from orderNotFound
                                            if (mDBHelper.deleteFromOrderNotFound(s))
                                                Log.d("loadOrderAsync", "OrderNotFound Record deleted: " + s);
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<OrderWithOutDocWithBoxWithMovesWithPartsResponce> call, Throwable t) {
                                    cancel(true);
                                    //resetLoadDataTimer();
                                    Log.w("loadOrderAsync", "Request failed: " + t.getMessage());
                                }
                            });
                }
            } catch (Exception e) {
                cancel(true);
                //resetLoadDataTimer();
                Log.e("loadOrderAsync", "Exception : " + e.getMessage());
                return null;
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            MessageUtils messageUtils = new MessageUtils();
            messageUtils.showMessage(getApplicationContext(), "onProgressUpdate. "+values[0]);
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            MessageUtils messageUtils = new MessageUtils();
            messageUtils.showMessage(getApplicationContext(), "onPostExecute.  "+result);
        }
    }
    // version 3.5.22
    private class saveOrderNotFoundAsync extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                mDBHelper.saveOrderNotFound(strings[0]);
            } catch (Exception e) {
                Log.d("saveOrderNotFoundAsync", "save exception : " + e.getMessage());
                return "Ошибка при записи";
            }

            return "Записано успешно";
        }
    }
    /*@Override
    protected void onPause() {
        super.onPause();
        Thread myThread = new Thread(new DownloadDataThread());
        myThread.start();
    }
    public void downloadData(View view) {
        Thread myThread = new Thread(new DownloadDataThread());
        myThread.start();
    }
    private class DownloadDataThread implements Runnable {

        @Override
        public void run() {

            if (isNetworkAvailable()) {

                if (mDBHelper.defs.getUrl() != null) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showLongMessage("Пытаюсь обновиться...");
                        }
                    });
                    downloadDataUsingThread();
                }
            }
        }
    }
    public boolean downloadDataUsingThread() {

        boolean isSuccessful = false;
        try {
            //TODO
            ApiUtils.getOrderService(mDBHelper.defs.getUrl()).getOrderV35(mDBHelper.defs.getDivision_code(),null)
                    .enqueue(new Callback<OrderOutDocBoxMovePart>() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onResponse(Call<OrderOutDocBoxMovePart> call,
                                               Response<OrderOutDocBoxMovePart> response) {
                            if (response.isSuccessful()) {
                                if (response.code() != 200) return ;
                                //save order, boxes, boxMoves, partBox
                                if (response.body() != null &&
                                        response.body().orderReqList != null &&
                                        !response.body().orderReqList.isEmpty()) {
                                    response.body().orderReqList.stream().forEach(item -> mDBHelper.insertOrders(item));

                                    if (response.body().outDocReqList != null &&
                                            !response.body().outDocReqList.isEmpty())
                                        response.body().outDocReqList.stream().forEach(item -> mDBHelper.insertOrUpdateOutDocs(item));

                                    if (response.body().boxReqList != null &&
                                            !response.body().boxReqList.isEmpty())
                                        response.body().boxReqList.stream().forEach(item -> mDBHelper.insertBoxes(item));

                                    if (response.body().movesReqList != null &&
                                            !response.body().movesReqList.isEmpty())
                                        response.body().movesReqList.stream().forEach(item -> mDBHelper.insertBoxMovesNoSelect(item));

                                    if (response.body().partBoxReqList != null &&
                                            !response.body().partBoxReqList.isEmpty())
                                        response.body().partBoxReqList.stream().forEach(item -> mDBHelper.insertProds(item));
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<OrderOutDocBoxMovePart> call, Throwable t) {
                            Log.w("loadOrderAsync", "Request failed: " + t.getMessage());
                        }

                    });

            isSuccessful = true;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLongMessage("Вроде обновились...");
                }
            });
        }

        return isSuccessful;
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private Handler loadDataHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            // todo
            showMessage("loadDataHandler");
            return true;
        }
    });

    private Runnable loadDataCallback = new Runnable() {
        @Override
        public void run() {
            showMessage("loadDataCallback’s started to run.");

            Thread myThread = new Thread(new DownloadDataThread());
            myThread.start();

        }
    };
    public void resetLoadDataTimer(){
        bCancelFlag = true;
        loadDataHandler.removeCallbacks(loadDataCallback);
        loadDataHandler.postDelayed(loadDataCallback, LOAD_TIMEOUT);
    }
    public void stopLoadDataTimer(){
        loadDataHandler.removeCallbacks(loadDataCallback);
    }
    @Override
    public void onUserInteraction(){
        resetLoadDataTimer();
    }
    */
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setSyncRepeatingAlarm();
    }

    /**
     * sets repeating alarm for syncing
     */
    public void setSyncRepeatingAlarm() {
        Intent intent = new Intent(this, SyncDataBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                Config.SYNC_ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AppController.getInstance().getAlarmManager().setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + BuildConfig.CACHE_TIMEOUT,
                BuildConfig.CACHE_TIMEOUT,
                pendingIntent);
    }
}
