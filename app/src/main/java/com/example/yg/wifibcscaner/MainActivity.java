package com.example.yg.wifibcscaner;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.yg.wifibcscaner.activity.BoxesActivity;
import com.example.yg.wifibcscaner.activity.LoginActivity;
import com.example.yg.wifibcscaner.activity.OrdersActivity;
import com.example.yg.wifibcscaner.activity.OutDocsActivity;
import com.example.yg.wifibcscaner.activity.ProdsActivity;
import com.example.yg.wifibcscaner.activity.SettingsActivity;
import com.example.yg.wifibcscaner.activity.UpdateActivity;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.Defs;
import com.example.yg.wifibcscaner.data.model.OutDocs;
import com.example.yg.wifibcscaner.data.repo.BoxRepo;
import com.example.yg.wifibcscaner.data.repo.DefsRepo;
import com.example.yg.wifibcscaner.data.repo.DepartmentRepo;
import com.example.yg.wifibcscaner.data.repo.DivisionRepo;
import com.example.yg.wifibcscaner.data.repo.OperRepo;
import com.example.yg.wifibcscaner.data.repo.OrderRepo;
import com.example.yg.wifibcscaner.data.repo.OutDocRepo;
import com.example.yg.wifibcscaner.data.repo.SotrRepo;
import com.example.yg.wifibcscaner.data.repo.UserRepo;
import com.example.yg.wifibcscaner.service.MessageUtils;
import com.example.yg.wifibcscaner.service.foundBox;
import com.example.yg.wifibcscaner.service.foundOrder;
import com.example.yg.wifibcscaner.utils.AppUtils;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.ScannerNotClaimedException;
import com.honeywell.aidc.ScannerUnavailableException;
import org.apache.commons.lang3.StringUtils;
import me.drakeet.support.toast.ToastCompat;

import static android.text.TextUtils.substring;
import static com.example.yg.wifibcscaner.utils.AppUtils.isDepAndSotrOper;
import static com.example.yg.wifibcscaner.utils.AppUtils.isOneOfFirstOper;
import static com.example.yg.wifibcscaner.utils.AppUtils.isOneScanOnlyOper;
import static com.example.yg.wifibcscaner.utils.AppUtils.isOutDocOnlyOper;
import static com.example.yg.wifibcscaner.utils.MyStringUtils.completeOrderDef;


public class MainActivity extends AppCompatActivity implements BarcodeReader.BarcodeListener {
    private static final String TAG = "sProject -> MainActivity.";

    private final UserRepo userRepo = new UserRepo();
    private final DefsRepo defsRepo = new DefsRepo();
    private final OutDocRepo outDocRepo = new OutDocRepo();
    private final OrderRepo orderRepo = new OrderRepo();
    private final BoxRepo boxRepo = new BoxRepo();

    private static BarcodeReader barcodeReader;
    private AidcManager manager;

    private DataBaseHelper mDBHelper;

    TextView tVDBInfo, currentDocDetails, currentUser;
    EditText editTextRQ;

    private foundBox fb = new foundBox();
    private foundOrder fo = new foundOrder();

    private String getDeviceUniqueID(Activity activity){
        String device_unique_id = Settings.Secure.getString(activity.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return device_unique_id;
    }
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        AppController.getInstance().getDbHelper().openDataBase();
        mDBHelper = AppController.getInstance().getDbHelper();
        defsRepo.selectDefsTable().ifPresent(d -> AppController.getInstance().setDefs(d));

        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        tVDBInfo = (TextView) findViewById(R.id.tVDBInfo);
        currentDocDetails  = (TextView) findViewById(R.id.currentDocDetails);
        currentUser  = (TextView) findViewById(R.id.currentUser);
        editTextRQ = (EditText) findViewById(R.id.editTextRQ);
        editTextRQ.setEnabled(false);

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

    @Override
    public void onStop(){
        super.onStop();
        if(barcodeReader!=null)
            barcodeReader.release();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!editTextRQ.isEnabled()){ // if enabled we are waiting for quantity to be entered
            String snum = "Накл.№ не выбрана";
            if (AppController.getInstance().getCurrentOutDoc().get_number() != 0) {
                snum = "Накл.№" + AppController.getInstance().getCurrentOutDoc().get_number();
            }
            snum = AppController.getInstance().getDefs().getDescOper()+", "+snum;
            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            actionBar.setSubtitle(Html.fromHtml("<font color='#FFBF00'>"+snum+"</font>"));
            actionBar.setTitle("Подразделение: "+AppController.getInstance().getDefs().getDescDivision());

            setTextViews();

            currentUser  = (TextView) findViewById(R.id.currentUser);
            currentUser.setText("Пользователь: " +userRepo.getUserName(AppController.getInstance().getDefs().get_idUser()));
        }

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


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void ocl_scan(View v) { //Вызов активности Сканирования
        if (AppController.getInstance().getDefs().get_idUser()==0){
            showLongMessage("Нужно войти в систему...");
            startActivity(new Intent(this, LoginActivity.class));
            //if not a superuser check for current user today's outdoc and add new one if not exist.
            return;
        }
        if ((AppController.getInstance().getDefs().get_Id_o()<=0) || AppUtils.isEmpty(AppController.getInstance().getDefs().getDivision_code()))
        {   //Операция не выбрана
            showLongMessage("Нужно зайти в настройки и выбрать подразделение и операцию");
            startActivity(new Intent(this, SettingsActivity.class));  //Вызов активности Коробки
            return;
        }
        if (isOutDocOnlyOper(AppController.getInstance().getDefs().get_Id_o())&(
                (AppController.getInstance().getCurrentOutDoc().get_id()==null)||
                        (AppController.getInstance().getCurrentOutDoc().get_number()==0)))
        {
            //Отгрузка, накладная не выбрана
            startActivity(new Intent(this, OutDocsActivity.class));
            return;
        }
        if (isDepAndSotrOper(AppController.getInstance().getDefs().get_Id_o())&(
                (AppController.getInstance().getDefs().get_Id_d()<=0)||
                (AppController.getInstance().getDefs().get_Id_s()<=0)||
                (AppController.getInstance().getCurrentOutDoc().get_id()==null)||
                (AppController.getInstance().getCurrentOutDoc().get_number()==0)))
        {
            if ((AppController.getInstance().getCurrentOutDoc().get_id()==null)||(AppController.getInstance().getCurrentOutDoc().get_number()==0))
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
        if (AppController.getInstance().getCurrentOutDoc().get_number() != 0) snum = "Накл.№"+AppController.getInstance().getCurrentOutDoc().get_number();
        snum = AppController.getInstance().getDefs().getDescOper()+", "+snum;
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(Html.fromHtml("<font color='#FFBF00'>"+snum+"</font>"));
        actionBar.setTitle("Подразделение: "+AppController.getInstance().getDefs().getDescDivision());
        //====this.setTitle(AppController.getInstance().getDefs().descOper+", "+snum);

        final     EditText            input = (EditText) findViewById(R.id.barCodeInput);
        if (editTextRQ.isEnabled()){
            ocl_bOk(v);
        }else {//камера устройства
            input.setEnabled(false);
            if(barcodeReader!=null){
                //showMessage("Режим работы со встроенным сканером.");
                try {
                    barcodeReader.softwareTrigger(true);
                } catch (ScannerNotClaimedException e) {
                    Log.e( TAG, "ocl_scan exception ".concat(e.getMessage()) );
                } catch (ScannerUnavailableException e) {
                    Log.e( TAG, "ocl_scan exception ".concat(e.getMessage()) );
                }
            }
            else{
                // Режим работы с камерой.
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.initiateScan();
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean checkSuper= userRepo.checkSuperUser(AppController.getInstance().getDefs().get_idUser());
        invalidateOptionsMenu();
        menu.findItem(R.id.action_settings).setVisible(checkSuper);
        menu.findItem(R.id.action_orders).setVisible(checkSuper);
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
            Log.d(TAG, "scanResultHandler -> barcode mismatch -> return");
            MessageUtils.showToast(MainActivity.this, getString(R.string.QR_invalid),true);
            return;
        }
        fo = orderRepo.searchOrder(currentbarcode);
        if (fo.getDivision_code() != null && !fo.getDivision_code().equals(AppController.getInstance().getDefs().getDivision_code())) {
            Log.d(TAG, "scanResultHandler -> division mismatch -> return");
            MessageUtils.showToast(MainActivity.this, getString(R.string.wrong_division_order),true);
            return;
        }
        if (!fo.isArchive()) { // архив
            if (fo.get_id() != 0) {                                      //Заказ найден, ищем коробку
                fb = mDBHelper.searchBox(fo.get_id(), currentbarcode);
                //---Получаем строку данных о коробке для вывода в tVDBInfo и количество для редактирования
                tVDBInfo = (TextView) findViewById(R.id.tVDBInfo);
                if (StringUtils.isNotEmpty(fb.getBoxdef()))
                    completeOrderDef(fb);
                tVDBInfo.setText(fo.getOrderdef());
                if (!fb.is_archive()){
                    if (StringUtils.isNotEmpty(fb.get_id())) {                                  //Коробка есть
                        //if it isOneScanOnlyOper and there is another outDoc record, set Quantity equal, bcs it can be only one shot
                        if (isOneScanOnlyOper(AppController.getInstance().getDefs().get_Id_o()) & StringUtils.isNotEmpty(fb.getOutDocs()))
                            fb.setQB( fb.getRQ() );

                        if (fb.getQB() == fb.getRQ()) {//Коробка заполнена

                            editTextRQ = (EditText) findViewById(R.id.editTextRQ);
                            editTextRQ.setText(String.valueOf(fb.getQB() - fb.getRQ()));
                            editTextRQ.setEnabled(false);

                            MessageUtils.showToast(this, "Эта коробка уже принята на "+AppController.getInstance().getDefs().getDescOper(), false);
                        } else {
                            Button bScan = (Button) findViewById(R.id.bScan);
                            bScan.setText("OK!");
                            editTextRQ = (EditText) findViewById(R.id.editTextRQ);
                            editTextRQ.setText(String.valueOf(fb.getQB() - fb.getRQ()));
                            final boolean isSetEnabled = true; //!AppUtils.isOutComeOper(AppController.getInstance().getDefs().get_Id_o());
                            editTextRQ.setEnabled(isSetEnabled);
                            editTextRQ.setSelection(editTextRQ.getText().length());
                            if (fb.getRQ() != 0) {showMessage("Эта коробка ранее принималась неполной!");}
                        }
                    } else {                                                //Коробки нет , подставить колво в поле редактирования колва и дожаться ОК.
                        if (isOneOfFirstOper(AppController.getInstance().getDefs().get_Id_o())){ //Добавить коробку если это операция приемки baseOper = 1
                            Button bScan = (Button) findViewById(R.id.bScan);
                            bScan.setText("OK!");
                            editTextRQ = (EditText) findViewById(R.id.editTextRQ);
                            editTextRQ.setText(String.valueOf(fb.getQB() - fb.getRQ())); //устанавливаетяся количество как разница
                            editTextRQ.setEnabled(true);
                            editTextRQ.setSelection(editTextRQ.getText().length());
                        }else{
                            showLongMessage("Эта коробка не принималась на "+AppController.getInstance().getDefs().getDescFirstOperForCurrent());
                        }
                    }
                }else {
                    showLongMessage("Эта коробка уже в архиве! Никакие операции невозможны!");
                }
            } else {
                showLongMessage("Заказ для этой коробки не загружен! Нужно синхронизировать данные.");
            }
        } else {
            showLongMessage("Этот заказ уже в архиве! Никакие операции невозможны!");
        }
    }

    public void ocl_bOk(View v) { //Вызов активности Сканирования

        final int enteredNumber;
        if (TextUtils.isEmpty(editTextRQ.getText().toString())) {
            MessageUtils.showToast(this,"Ошибка! Введите количество верно!", false);
            return;
        }else{ //check is it number
            try {
                enteredNumber = Integer.valueOf(editTextRQ.getText().toString());
            }catch (NumberFormatException e){
                Log.e(TAG, AppController.getInstance().getDefs().getDescOper().concat(". Ошибка при получении количества в коробке!"), e);
                MessageUtils.showToast(this,AppController.getInstance().getDefs().getDescOper().concat(". Ошибка! Введите количество верно!"), false);
                return;
            }
        }

        if (!AppUtils.isIncomeOper(AppController.getInstance().getDefs().get_Id_o())) { //entered number should be checked
            if (AppUtils.isOutComeOper(AppController.getInstance().getDefs().get_Id_o())) {//entered number should be equal
                if (enteredNumber != fb.getQB()) {
                    MessageUtils.showToast(this,"Ошибка! Количество должно быть равно оприходованному!", false);
                    return;
                }
            }else{
                if (enteredNumber > (fb.getQB() - fb.getRQ())){
                    MessageUtils.showToast(this,"Ошибка! Введите количество верно!", false);
                    return;
                }
            }
        }

        try {
            Button bScan = (Button) findViewById(R.id.bScan);
            bScan.setText("Scan!");
            tVDBInfo = (TextView) findViewById(R.id.tVDBInfo);
            editTextRQ = (EditText) findViewById(R.id.editTextRQ);
            editTextRQ.setEnabled(false);
            if (StringUtils.isNotEmpty(fb.get_id())) {                                            //коробка есть и не полная, добавить в prods
                //новая операция по существующей коробке
                boolean newBM = (fb.getRQ() != 0);
                fb.setRQ( enteredNumber );
                if (mDBHelper.addProds(fb)) {
                    if (newBM)
                        MessageUtils.showToast(AppController.getInstance().getApplicationContext(),
                                AppController.getInstance().getDefs().getDescOper().concat(". В коробку добавлено ").concat(String.valueOf(enteredNumber)),
                                true);

                    setTextViews();
                    boxRepo.lastBoxCheck(fo);
                } else
                    MessageUtils.showToast(AppController.getInstance().getApplicationContext(),
                        AppController.getInstance().getDefs().getDescOper().concat(". Повторный прием коробки в смену! Повторный прием возможен в другую смену."),
                        true);
            } else {
                if (!mDBHelper.addBox(fo,enteredNumber)) {
                    MessageUtils.showToast(AppController.getInstance().getApplicationContext(),
                        AppController.getInstance().getDefs().getDescOper().concat(". Ошибка! Коробка не добавлена в БД!"),
                        true);
                } else {
                    setTextViews();
                    boxRepo.lastBoxCheck(fo);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, AppController.getInstance().getDefs().getDescOper().concat(". Ошибка при получении количества в коробке!"), e);
            MessageUtils.showToast(AppController.getInstance().getApplicationContext(),
                    AppController.getInstance().getDefs().getDescOper().concat(". Ошибка! Невозможно получить введенное количество!"),
                    true);
        }
    }

    private void setTextViews (){
        tVDBInfo = (TextView) findViewById(R.id.tVDBInfo);
        tVDBInfo.setText(mDBHelper.lastBox());
        currentDocDetails  = (TextView) findViewById(R.id.currentDocDetails);
        currentDocDetails.setText("Накл.№" +AppController.getInstance().getCurrentOutDoc().getNumberString() + outDocRepo.selectCurrentOutDocDetails(AppController.getInstance().getCurrentOutDoc().get_id()));
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
        // TODO Auto-generated method stub
        super.onStart();

        if (AppController.getInstance().getDefs().getDeviceId().isEmpty() || AppController.getInstance().getDefs().getDeviceId() == null || AppController.getInstance().getDefs().getDeviceId().contentEquals("0")) {
            String DeviceId = getDeviceUniqueID(this);
            AppController.getInstance().getDefs().setDeviceId(DeviceId);
        }

        if (userRepo.checkIfUserTableEmpty()) {
            startActivity(new Intent(this,SettingsActivity.class));
            return;
        } else {
            if (AppController.getInstance().getDefs().get_idUser()==0) {
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
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
        super.onDestroy();

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
                // TODO Auto-generated method stub
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
                // TODO Auto-generated method stub
                Button bScan = (Button) findViewById(R.id.bScan);
                bScan.setText("Scan!");
                editTextRQ = (EditText) findViewById(R.id.editTextRQ);
                editTextRQ.setEnabled(false);
            }
        });

        quitDialog.setNegativeButton("Нет.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                editTextRQ.requestFocus();
            }
        });
        quitDialog.show();
    }
}
