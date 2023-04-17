package com.example.yg.wifibcscaner;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yg.wifibcscaner.activity.BaseActivity;
import com.example.yg.wifibcscaner.activity.BoxesActivity;
import com.example.yg.wifibcscaner.activity.LoginActivity;
import com.example.yg.wifibcscaner.activity.OutDocsActivity;
import com.example.yg.wifibcscaner.activity.ProdsActivity;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.dto.OrderWithOutDocWithBoxWithMovesWithPartsResponce;
import com.example.yg.wifibcscaner.data.model.BoxMoves;
import com.example.yg.wifibcscaner.data.model.Boxes;
import com.example.yg.wifibcscaner.data.model.OutDocs;
import com.example.yg.wifibcscaner.data.model.Prods;
import com.example.yg.wifibcscaner.databinding.ActivityMainBinding;
import com.example.yg.wifibcscaner.receiver.SyncDataBroadcastReceiver;
import com.example.yg.wifibcscaner.service.DataExchangeService;
import com.example.yg.wifibcscaner.utils.ApiUtils;
import com.example.yg.wifibcscaner.utils.MessageUtils;
import com.example.yg.wifibcscaner.utils.SharedPreferenceManager;
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
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.text.TextUtils.substring;
import static com.example.yg.wifibcscaner.DataBaseHelper.*;
import static com.example.yg.wifibcscaner.data.service.OutDocService.makeOutDocDesc;
import static com.example.yg.wifibcscaner.utils.StringUtils.makeOrderDesc;
import static com.example.yg.wifibcscaner.utils.StringUtils.makeUserDesc;
import static com.example.yg.wifibcscaner.utils.StringUtils.filter;


public class MainActivity extends BaseActivity implements BarcodeReader.BarcodeListener, DatePickerDialog.OnDateSetListener {
    private static final String TAG = "MainActivity";

    private static BarcodeReader barcodeReader; //honeywell
    private AidcManager manager;
    boolean bCancelFlag;
    UsbManager mUsbManager = null;
    UsbDevice mdevice;


    private DataBaseHelper mDBHelper;
    TextView currentUser;
    static EditText editTextRQ;
    foundbox fb;
    foundorder fo;

    private static String getDeviceUniqueID(Activity activity){
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

        mDBHelper = AppController.getInstance().getDbHelper();

        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setMac(AppController.getInstance().getMainActivityViews());

        editTextRQ = (EditText) findViewById(R.id.editTextRQ);
        editTextRQ.setEnabled(false);

        //registerReceiver
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        AidcManager.create(this, new AidcManager.CreatedCallback() {
            @Override
            public void onCreated(AidcManager aidcManager) {
                manager = aidcManager;
                barcodeReader = manager.createBarcodeReader();
                try {
                    if(barcodeReader!=null) {
                        Log.d(TAG, "barcodereader not claimed in OnCreate()");
                        barcodeReader.claim();
                    }
                }
                catch (ScannerUnavailableException e) {
                    MessageUtils.showToast(MainActivity.this, "Невозможно включить встроенный сканер.",true);
                    //e.printStackTrace();
                }
                // register bar code event listener
                barcodeReader.addBarcodeListener(MainActivity.this);
            }
        });
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

        /*String snum = "Накл.???";

        if (mDBHelper.currentOutDoc.get_number() != 0) snum = "Накл.№"+mDBHelper.currentOutDoc.get_number();
        snum = mDBHelper.defs.descOper+", "+snum;
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(Html.fromHtml("<font color='#FFBF00'>"+snum+"</font>"));
        }

        actionBar.setTitle(mDBHelper.defs.descDivision);*/

        if (AppController.getInstance().getMainActivityViews().getOrder() == null ||
                AppController.getInstance().getMainActivityViews().getOrder().isEmpty()) {
            if (SharedPreferenceManager.getInstance().getLastScannedBoxDescription()
                    .equals(AppController.getInstance().getResourses().getString(R.string.no_data_to_view))){
                Log.d(TAG, "SetOrderInfo called onResume");
                SetOrderInfo setOrderInfo = new SetOrderInfo();
                setOrderInfo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }else {
                Log.d(TAG, "Last order info fetched from sharedPrefs");
                AppController.getInstance().getMainActivityViews().setOrder(SharedPreferenceManager.getInstance().getLastScannedBoxDescription());
            }
        }
        if (AppController.getInstance().getMainActivityViews().getOutDoc() == null ||
                AppController.getInstance().getMainActivityViews().getOutDoc().isEmpty()) {
            Log.d(TAG, "setCurOutDocInfo called onResume");
            SetCurOutDocInfo setCurOutDocInfo = new SetCurOutDocInfo();
            setCurOutDocInfo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        if (AppController.getInstance().getMainActivityViews().getUser() == null ||
                AppController.getInstance().getMainActivityViews().getUser().isEmpty()) {
            Log.d(TAG, "setUser called onResume");
            AppController.getInstance().getMainActivityViews().setUser(makeUserDesc(mDBHelper.getUserName(mDBHelper.defs.get_idUser())));
        }
        //AppController.getInstance().getCurrentDocDetails().setName("onResume");

        /*            Log.d(TAG, "setOrderInfo called onResume");
            SetOrderInfo setOrderInfo = new SetOrderInfo();
            setOrderInfo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        */
        if (barcodeReader != null) {
            try {
                barcodeReader.claim();
                Log.d(TAG, "Device scanner claimed");
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                MessageUtils.showToast(MainActivity.this, "Невозможно включить встроенный сканер.",true);
            }
        }
        Button bScan = (Button) findViewById(R.id.bScan);
        bScan.requestFocus();
    }


    public void ocl_scan(View v) { //Вызов активности Сканирования
        if (mDBHelper.defs.get_idUser()==0){
            MessageUtils.showToast(MainActivity.this, "Нужно войти в систему...",true);
            startActivity(new Intent(this, LoginActivity.class));
            //if not a superuser check for current user today's outdoc and add new one if not exist.
            return;
        }
        if ((mDBHelper.defs.get_Id_o()==0)||("0".equals(mDBHelper.defs.getDivision_code())))
        {   //Операция не выбрана
            MessageUtils.showToast(MainActivity.this, "Нужно зайти в настройки и выбрать операцию, подразделение...",true);
            startActivity(new Intent(this,SettingsActivity.class));
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
                MessageUtils.showToast(MainActivity.this, "Нужно зайти в настройки и выбрать бригаду и сотрудника...",true);
                startActivity(new Intent(this,SettingsActivity.class));
            }
            return;
        }
        /*
        String snum = "Накл.???";
        if (mDBHelper.currentOutDoc.get_number() != 0) snum = "Накл.№"+mDBHelper.currentOutDoc.get_number();
        snum = mDBHelper.defs.descOper+", "+snum;
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(Html.fromHtml("<font color='#FFBF00'>" + snum + "</font>"));
        }
        if (actionBar != null) {
            actionBar.setTitle("Подразделение: "+mDBHelper.defs.descDivision);
        }*/
        //====this.setTitle(mDBHelper.defs.descOper+", "+snum);

        /* Если сканер подключен - вызывать обработчик для него*/
        final     EditText            input = (EditText) findViewById(R.id.barCodeInput);
        if (editTextRQ.isEnabled()){
            //input.setEnabled(false);
            ocl_bOk(v);
        }else {
        if (mdevice != null){//внешний usb сканер
            MessageUtils.showToast(MainActivity.this, "Режим работы с внешним сканером.",false);
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
                        MessageUtils.showToast(MainActivity.this, currentbarcode,false);
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
                } catch (ScannerNotClaimedException | ScannerUnavailableException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            else{
                MessageUtils.showToast(MainActivity.this, "Режим работы с камерой.",true);
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.initiateScan();
            }
        }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
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
            case R.id.action_prods:
                startActivity(new Intent(this, ProdsActivity.class));
                return true;
            case R.id.actOutDoc:
                startActivity(new Intent(this,OutDocsActivity.class));
                return true;
            case R.id.actBox:
                startActivity(new Intent(this, BoxesActivity.class));
                return true;
            case R.id.action_update:
                new DataExchangeService().call();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem setUpdateDate = menu.findItem(R.id.setUpdateDate);
        setUpdateDate.setOnMenuItemClickListener(item -> {
            showDatePickerDialog();
            return false;
        });
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean checkSuper= mDBHelper.checkSuperUser(mDBHelper.defs.get_idUser());
        invalidateOptionsMenu();
        menu.findItem(R.id.action_settings).setVisible(checkSuper);

        return super.onPrepareOptionsMenu(menu);
    }


    private void scanResultHandler (String currentbarcode) {
            /*---Тут нужно данные коробки вывести и дать отредактировать количество. Есть код в currentbarcode.
            Нужно его обработать, выбрать данные новой коробки для вывода tOrderInfo и в editTextRQ
            * Если данные новой коробки не нашли в заказах - сообщить и ничего не выводить*/
            //поискать символ с кодом 194
        currentbarcode = filter(currentbarcode);
        if ((currentbarcode.length()<20)&&(StringUtils.countMatches(currentbarcode,'.')!=5)) {
            MessageUtils.showToast(MainActivity.this, "QR-код не распознан.",true);
            return;
        }
        fo = mDBHelper.searchOrder(currentbarcode);
        if (!fo.archive) { // архив
            if (fo._id != 0) {                                      //Заказ найден, ищем коробку
                fb = mDBHelper.searchBox(fo._id, currentbarcode);
                //---Получаем строку данных о коробке для вывода в tOrderInfo и количество для редактирования

                if (!fb.boxdef.equals(""))
                    fo.orderdef += fb.boxdef+ "\n";
                if (!fb.depSotr.equals(""))
                    fo.orderdef += fb.depSotr+ "\n";
                if (!fb.outDocs.equals(""))
                    fo.orderdef += fb.outDocs;
                //TextView tOrderInfo = (TextView) findViewById(R.id.tOrderInfo);
                //tOrderInfo.setText(fo.orderdef);
                SharedPreferenceManager.getInstance().setLastScannedBoxDescription(fo.orderdef);
                Log.d(TAG, "scanResultHandler has made lastbox orderdef as -> "+fo.orderdef);
                if (!fb._archive){
                    if ((fb._id != null) && !fb._id.equals("")) {                                  //Коробка есть
                        if (fb.QB == fb.RQ) {//Коробка заполнена

                            editTextRQ = (EditText) findViewById(R.id.editTextRQ);
                            editTextRQ.setText(String.valueOf(fb.QB - fb.RQ));
                            editTextRQ.setEnabled(false);
                            if (mDBHelper.defs.get_Id_o()==mDBHelper.defs.get_idOperFirst())
                                MessageUtils.showToast(MainActivity.this, "Эта коробка уже принята полной!",false);
                            else
                                MessageUtils.showToast(MainActivity.this, "Эта коробка уже отгружена!",false);
                        } else {
                            Button bScan = (Button) findViewById(R.id.bScan);
                            bScan.setText(R.string.bOk);
                            editTextRQ = (EditText) findViewById(R.id.editTextRQ);
                            editTextRQ.setText(String.valueOf(fb.QB - fb.RQ));
                            editTextRQ.setEnabled(true);
                            editTextRQ.setSelection(editTextRQ.getText().length());
                            if (fb.RQ != 0) {
                                MessageUtils.showToast(MainActivity.this, "Эта коробка ранее принималась неполной!",false);
                                }
                        }
                    } else {                                                //Коробки нет , подставить колво в поле редактирования колва и дожаться ОК.
                        if (mDBHelper.defs.get_Id_o()==mDBHelper.defs.get_idOperFirst()){ //Добавить коробку если это операция приемки baseOper = 1
                            Button bScan = (Button) findViewById(R.id.bScan);
                            bScan.setText(R.string.bOk);
                            editTextRQ = (EditText) findViewById(R.id.editTextRQ);
                            editTextRQ.setText(String.valueOf(fb.QB - fb.RQ));
                            editTextRQ.setEnabled(true);
                            editTextRQ.setSelection(editTextRQ.getText().length());
                        }else{
                            MessageUtils.showToast(MainActivity.this, "Эта коробка не принималась на производстве!",false);
                        }
                    }
                }else {
                    MessageUtils.showToast(MainActivity.this, "Эта коробка уже в архиве! Никакие операции невозможны!",false);
                }
            } else {
                MessageUtils.showToast(MainActivity.this, "Заказ для этой коробки не загружен! Будет загружен автоматически при подключении к WiFi.",false);
                //TODO 1. make request to server to load the order if not save the order and load it later
                SaveOrderNotFoundAsync save = new SaveOrderNotFoundAsync();
                try {
                    save.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                            AppController.getInstance().getDbHelper().getOrder_id(currentbarcode)).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                LoadOrderAsync task = new LoadOrderAsync();
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        AppController.getInstance().getDbHelper().getOrder_id(currentbarcode));
            }
        } else {
            MessageUtils.showToast(MainActivity.this, "Этот заказ уже в архиве! Никакие операции невозможны!",false);
       }
    }

    public void ocl_bOk(View v) { //Вызов активности Сканирования
        int iRQ = 0;
        boolean newBM = false;

        final String _RQ = editTextRQ.getText().toString();
        if ((!TextUtils.isEmpty(_RQ))&(Integer.valueOf(_RQ)<=(fb.QB - fb.RQ))) {//колво  не пустое
            try {
                Button bScan = (Button) findViewById(R.id.bScan);
                bScan.setText("Scan!");
                bScan.requestFocus();
                iRQ = Integer.valueOf(_RQ);

                editTextRQ = (EditText) findViewById(R.id.editTextRQ);
                editTextRQ.setEnabled(false);
                if ((!fb._id.equals("")&&(fb._id != null))) {                                            //коробка есть и не полная, добавить в prods
                    if (fb.RQ == 0) {
                        newBM = true;                                           //новая операция по существующей коробке
                    }
                    fb.RQ = iRQ;
                    if (mDBHelper.addProds(fb)) {
                        if (!newBM) {
                            MessageUtils.showToast(MainActivity.this,
                                    mDBHelper.defs.descOper+". В коробку добавлено "+ iRQ,false);
                        }
                        mDBHelper.lastBoxCheck(fo, MainActivity.this);

                        SetOrderInfo setOrderInfo = new SetOrderInfo();
                        setOrderInfo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        Log.d(TAG, "setOrderInfo was called from ocl_bOk");

                        /*SetCurOutDocInfo setCurOutDocInfo = new SetCurOutDocInfo();
                        setCurOutDocInfo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        Log.d(TAG, "setCurOutDocInfo was called from ocl_bOk");*/
                    }else {
                        MessageUtils.showToast(MainActivity.this,
                                mDBHelper.defs.descOper+". Повторный прием коробки в смену! Повторный прием возможен в другую смену.",true);
                    }

                }else {
                    if (!mDBHelper.addBoxes(fo,iRQ)) {            //---Вызов метода добавления коробки и продс
                        MessageUtils.showToast(MainActivity.this,
                        mDBHelper.defs.descOper+". Ошибка! Коробка не добавлена в БД!",true);
                    } else {
                        mDBHelper.lastBoxCheck(fo, MainActivity.this);

                        SetOrderInfo setOrderInfo = new SetOrderInfo();
                        setOrderInfo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        Log.d(TAG, "setOrderInfo was called from ocl_bOk");

                        /*SetCurOutDocInfo setCurOutDocInfo = new SetCurOutDocInfo();
                        setCurOutDocInfo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        Log.d(TAG, "setCurOutDocInfo was called from ocl_bOk");   */
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, mDBHelper.defs.descOper+". Ошибка при получении количества в коробке!", e);
                MessageUtils.showToast(MainActivity.this,
                        mDBHelper.defs.descOper+". Ошибка! Невозможно получить введенное количество!",true);
            }
        }else {
            MessageUtils.showToast(MainActivity.this,
                    "Ошибка! Введите количество верно!",true);
        }
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
    //Crushial for camera scann
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult.getContents() != null) {
            // handle scan result
            String currentbarcode = scanResult.getContents();
            scanResultHandler(currentbarcode);
        }else{
            MessageUtils.showToast(MainActivity.this,
                    "Ошибка сканера !",true);
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
                    MessageUtils.showToast(MainActivity.this,
                            "Ошибка сканера !",true);
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
        quitDialog.setTitle("Отменить? Вы уверены?");

        quitDialog.setPositiveButton("Да!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editTextRQ = (EditText) findViewById(R.id.editTextRQ);
                editTextRQ.setEnabled(false);
                Button bScan = (Button) findViewById(R.id.bScan);
                bScan.setText("Scan!");
                bScan.requestFocus();
            }
        });

        quitDialog.setNegativeButton("Нет.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                editTextRQ.requestFocus();
                editTextRQ.setSelection(editTextRQ.getText().length());
            }
        });
        quitDialog.show();
    }
    /* set Update Date */
    public void showDatePickerDialog(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String spMonth = "";
        String spDay = "";

        if (String.format("%d", dayOfMonth).length()==1) {
            spDay = "0"+String.format("%d", dayOfMonth);
        }else{
            spDay = String.format("%d", dayOfMonth);
        };
        if (String.format("%d", (month + 1)).length()==1) {
            spMonth = "0"+String.format("%d", (month + 1));
        }else{
            spMonth = String.format("%d", (month + 1));
        };
        AppController.getInstance().getDbHelper().globalUpdateDate = spDay+"."+ spMonth+"."+year+" 00:00:00";
        SharedPreferenceManager.getInstance().setUpdateDate(spDay+"."+ spMonth+"."+year+" 00:00:00");
    }

    // version 3.5.22
    private class LoadOrderAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                ArrayList<String> OrdersId = new ArrayList<String>();
                bCancelFlag = false;
                Log.i(TAG, " bCancelFlag was set to: "+bCancelFlag);
                if (strings !=null && strings.length != 0)
                    OrdersId.add(strings[0]);
                else
                    OrdersId.addAll(mDBHelper.getOrdersNotFound());
                for (String s : OrdersId) {
                    if (isCancelled() || bCancelFlag) {
                        Log.i(TAG, "Breaked. isCanceled="+isCancelled()+" bCancelFlag"+bCancelFlag);
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
                                            mDBHelper.insertOrder(response.body().getOrder());
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
                                            MessageUtils.showToast(getApplicationContext(),
                                                    "Заказ "+makeOrderDesc(new String[] {response.body().getOrder().get_Ord(),
                                                            response.body().getOrder().get_Cust(),
                                                            response.body().getOrder().get_Nomen(),
                                                            response.body().getOrder().get_Attrib(),
                                                            String.valueOf(response.body().getOrder().get_Q_ord()),
                                                            String.valueOf(response.body().getOrder().get_Q_box())})+
                                                    " загружен. Отсканируйте коробку еще раз.",
                                                    true);
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<OrderWithOutDocWithBoxWithMovesWithPartsResponce> call, Throwable t) {
                                    cancel(true);
                                    //resetLoadDataTimer();
                                    Log.w(TAG, "Request failed: " + t.getMessage());
                                }
                            });
                }
            } catch (Exception e) {
                cancel(true);
                //resetLoadDataTimer();
                Log.e(TAG, "Exception : " + e.getMessage());
                return null;
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    /**
     * sets repeating alarm for data download
     */
    public void setSyncRepeatingAlarm() {
        Intent intent = new Intent(this, SyncDataBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                Config.SYNC_ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AppController.getInstance().getAlarmManager().setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + BuildConfig.NEXT_DOWNLOAD_ATTEMPT_TIMEOUT,
                BuildConfig.NEXT_DOWNLOAD_ATTEMPT_TIMEOUT,
                pendingIntent);
    }
    // version 4.0.
    private static class SaveOrderNotFoundAsync extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... strings) {
            try {
                AppController.getInstance().getDbHelper().saveOrderNotFound(strings[0]);
            } catch (Exception e) {
                Log.d(TAG, "saveOrderNotFoundAsync : " + e.getMessage());
                return "Ошибка при записи";
            }

            return "Записано успешно";
        }
        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);
        }
    }
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setSyncRepeatingAlarm();
    }
    private class SetOrderInfo extends AsyncTask<Void, Void, String> {
        Exception e;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(Void... params) {
            String result = "";
            try {
                result = AppController.getInstance().getDbHelper().lastBox();
                Log.d(TAG, "getDbHelper().lastBox() has returned -> "+result);
            } catch (Exception e) {
                this.e = e;
                Log.e(TAG, e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);
        }
    }
    private class SetCurOutDocInfo extends AsyncTask<Void, Void, String> {

        Exception e;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AppController.getInstance().getMainActivityViews().setOutDoc(makeOutDocDesc(new String[]{null}));
        }

        protected String doInBackground(Void... params) {
            String result = "";
            try {
                Log.d(TAG, "getDbHelper().selectCurrentOutDocDetails() id -> "+AppController.getInstance().getDbHelper()
                        .currentOutDoc.get_id());
                AppController.getInstance().getMainActivityViews().setOutDoc(AppController.getInstance().getDbHelper()
                        .selectCurrentOutDocDetails());
                Log.d(TAG, "getDbHelper().selectCurrentOutDocDetails() has returned -> "+result);
            } catch (Exception e) {
                this.e = e;
                Log.e(TAG, e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);
        }
    }
}
