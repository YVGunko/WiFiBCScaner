package com.example.yg.wifibcscaner.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.yg.wifibcscaner.DataBaseHelper;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.Defs;
import com.example.yg.wifibcscaner.data.model.OutDocs;
import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.data.repo.DefsRepo;
import com.example.yg.wifibcscaner.data.repo.DepartmentRepo;
import com.example.yg.wifibcscaner.data.repo.DivisionRepo;
import com.example.yg.wifibcscaner.data.repo.OperRepo;
import com.example.yg.wifibcscaner.data.repo.OutDocRepo;
import com.example.yg.wifibcscaner.data.repo.SotrRepo;
import com.example.yg.wifibcscaner.data.repo.UserRepo;
import com.example.yg.wifibcscaner.service.SharedPrefs;
import com.example.yg.wifibcscaner.service.ApiUtils;
import com.example.yg.wifibcscaner.service.MessageUtils;
import com.example.yg.wifibcscaner.utils.executors.DefaultExecutorSupplier;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.yg.wifibcscaner.utils.DateTimeUtils.getDayTimeString;

public class OutDocsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "sProject -> OutDocsActivity.";

    private final DepartmentRepo depRepo = new DepartmentRepo();
    private final OutDocRepo outDocRepo = new OutDocRepo();
    private final UserRepo userRepo = new UserRepo();
    private final DefsRepo defsRepo = new DefsRepo();

    ListView lvData;
    SimpleCursorAdapter scAdapter;
    String strTitle= "Выберите накладную";
    String selectedTitle = "Выбрана Накл.№";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_out_docs, menu);
        return true;
    }

    private void showToast (String message, boolean duration) {
        DefaultExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
            MessageUtils.showToast(getApplicationContext(), message, duration);
        });
    }
    private class SyncIncoData extends AsyncTask<String, Integer, Integer> {
        Integer counter;

        @Override
        protected Integer doInBackground(String... urls) {
            counter = 0;
            try {
                ApiUtils.getOrderService(AppController.getInstance().getDefs().getUrl()).
                        addOutDoc(outDocRepo.getOutDocNotSent(),AppController.getInstance().getDefs().getDeviceId()).enqueue(new Callback<List<OutDocs>>() {
                    @Override
                    public void onResponse(Call<List<OutDocs>> call, Response<List<OutDocs>> response) {
                        Log.d(TAG,"Ответ сервера на запрос синхронизации накладных: " + response.body().size());
                        if(response.isSuccessful()) {
                            for(OutDocs boxes : response.body())
                                outDocRepo.updateOutDocsetSentToMasterDate(boxes);

                            counter = response.body().size();
                        }else {
                            showToast("Ошибка при выгрузке накладных!", true);
                        }
                    }
                    @Override
                    public void onFailure(Call<List<OutDocs>> call, Throwable t) {
                        Log.d(TAG, "OutDocs Error: " + t.getMessage());
                        showToast("Ошибка при выгрузке накладных!", true);
                    }
                });
            } catch (Exception e) {
                Log.d(TAG,"Ответ сервера на запрос новых заказов: " + e.getMessage());
                showToast("Ошибка при выгрузке накладных!", true);
            }
            return counter;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MessageUtils.showToast(getApplicationContext(), "Синхронизация данных начата.", false);
        }

        @Override
        protected void onPostExecute(Integer result) {
            MessageUtils.showToast(getApplicationContext(), "Синхронизация окончена. Отпрвлено накладных: ".concat(String.valueOf(result)), true);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_out_docs);

        Button btnAdd = (Button) findViewById(R.id.addOutDoc);
        btnAdd.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder adb = new AlertDialog.Builder(OutDocsActivity.this);
                adb.setTitle("Создать накладные...");
                adb.setMessage("Хотите добавить накладные для всех бригад " +AppController.getInstance().getDefs().getDescDep());
                adb.setNegativeButton("Нет", null);
                adb.setPositiveButton("Да", new AlertDialog.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    public void onClick(DialogInterface dialog, int which) {
                        int nextOutDocNumber = outDocRepo.getNextOutDocNumber();
                        if (nextOutDocNumber == 0) {
                            Log.e(TAG, "outDocRepo.getNextOutDocNumber() returned 0");
                            MessageUtils.showToast(getApplicationContext(), "Ошибка нумерации. Накладные не будут созданы!", true);
                            return ;
                        }

                        if (!outDocRepo.createOutDocsForCurrentOper(nextOutDocNumber)) {
                            Log.e(TAG, "outDocRepo.createOutDocsForCurrentOper(nextOutDocNumber) returned 0");
                            MessageUtils.showToast(getApplicationContext(), "Ошибка при создании документов. Накладные не будут созданы!", true);
                            return ;
                        };
                        getSupportLoaderManager().getLoader(0).forceLoad();
                    }
                });
                adb.show();
                return true;
            }
        });

        Button btnDays = (Button) findViewById(R.id.daysToView);
        if (!userRepo.checkSuperUser(AppController.getInstance().getDefs().get_idUser())) {
            btnDays.findViewById(R.id.daysToView).setVisibility(View.INVISIBLE);
            if (SharedPrefs.getInstance() != null) {
                SharedPrefs.getInstance().setOutDocsDays(1);
            }

        } else {
            if (SharedPrefs.getInstance() != null) {
                setDaysButtonState ();
            }
        }

        String[] from = new String[]{OutDocs.COLUMN_number, OutDocs.COLUMN_DT, OutDocs.COLUMN_comment};
        int[] to = new int[]{R.id.tvNumber, R.id.tvNumBox, R.id.tvText};

        // создаем адаптер и настраиваем список
        scAdapter =new

        SimpleCursorAdapter(this,R.layout.content_out_doc, null,from, to, 0);

        lvData =(ListView)

        findViewById(R.id.lvData);
        lvData.setAdapter(scAdapter);

        lvData.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                try {
                    if (scAdapter.getCount() > 0) {
                        strTitle = "№" +scAdapter.getCursor().getString(1)
                                + outDocRepo.selectCurrentOutDocDetails(scAdapter.getCursor().getString(0));
                        OutDocsActivity.this.setTitle(strTitle);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                return true;
            }
        });

    lvData.setOnItemClickListener(new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick (AdapterView < ? > var1, View var2,final int position, long id){
        scAdapter.getCursor().getString(0);

        AlertDialog.Builder adb = new AlertDialog.Builder(OutDocsActivity.this);
        adb.setTitle("Выбор накладной...");
        adb.setMessage("Выбираем накладную №" + scAdapter.getCursor().getString(1) + " от " + scAdapter.getCursor().getString(3));
        adb.setNegativeButton("Нет", null);
        adb.setPositiveButton("Да", new AlertDialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                String result = "";

                AppController.getInstance().setCurrentOutDoc( new OutDocs(scAdapter.getCursor().getString(0),
                            scAdapter.getCursor().getInt(4),
                            scAdapter.getCursor().getInt(1),
                            scAdapter.getCursor().getString(2),
                            scAdapter.getCursor().getString(3),
                            scAdapter.getCursor().getString(5),
                            scAdapter.getCursor().getInt(6),
                            scAdapter.getCursor().getInt(7),
                            scAdapter.getCursor().getInt(8)));

                result = selectedTitle +scAdapter.getCursor().getString(1);

                if (AppController.getInstance().getDefs().get_Id_o()==AppController.getInstance().getDefs().get_idOperFirst()
                        && AppController.getInstance().getDefs().getDivision_code().equals(DefsRepo.getPuDivisionCode())){
                    //Если прием производства и ПУ - установить бригаду из строки таблицы Prods c выбранной накладной
                    int iDep = depRepo.getIdByOutDocCode(AppController.getInstance().getCurrentOutDoc().get_id());
                    if ( iDep != 0 && iDep!=AppController.getInstance().getDefs().get_Id_d() ){
                        AppController.getInstance().getDefs().set_Id_d(iDep);
                        Defs defs = new Defs(iDep, AppController.getInstance().getDefs().get_Id_o(), AppController.getInstance().getDefs().get_Id_s(),
                                AppController.getInstance().getDefs().get_Host_IP(), AppController.getInstance().getDefs().get_Port(),
                                AppController.getInstance().getDefs().getDivision_code(),
                                AppController.getInstance().getDefs().get_idUser(),
                                AppController.getInstance().getDefs().getDeviceId());
                        if (defsRepo.updateDefsTable(defs) != 0) {
                            AppController.getInstance().setDefs(defs);
                            MessageUtils.showToast(getApplicationContext(),"Сохранено."+AppController.getInstance().getDefs().getDescDep(), false);
                        } else {
                            MessageUtils.showToast(getApplicationContext(),"Ошибка при сохранении.", true);
                        }
                    }
                    result = selectedTitle +scAdapter.getCursor().getString(1)+" "+AppController.getInstance().getDefs().getDescDep();
                }
                OutDocsActivity.this.setTitle(result);
            }
        });
        adb.show();
    }
    });

    // добавляем контекстное меню к списку
    registerForContextMenu(lvData);

    // создаем лоадер для чтения данных
    getSupportLoaderManager().initLoader(0,null,this);
    }
    // обработка нажатия кнопки
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onButtonClick(View view) {
        int docNum = outDocRepo.outDocsAddRec();
        // добавляем запись
        if (docNum!=0) {
            // получаем новый курсор с данными
            getSupportLoaderManager().getLoader(0).forceLoad();

            OutDocsActivity.this.setTitle(selectedTitle +String.valueOf(docNum));
        } else {
            MessageUtils.showToast(getApplicationContext(),"Ошибка при добавлении записи. Проверьте правильность настроек.", false);
        }
    }
    private void setDaysButtonState (){
        Button btn = (Button) findViewById(R.id.daysToView);
        if (SharedPrefs.getInstance().getOutDocsDays() == 1){
            btn.setText(R.string.seven_days);
        } else {
            btn.setText(R.string.one_day);
        }
    }
    private void invertSharedPrefsDaysState (){
        if (SharedPrefs.getInstance().getOutDocsDays() == 1){
            SharedPrefs.getInstance().setOutDocsDays(7);
        } else {
            SharedPrefs.getInstance().setOutDocsDays(1);
        }
    }
    // обработка нажатия кнопки
    public void onButtonDaysClick(View view) {
        if (SharedPrefs.getInstance() != null) {
            invertSharedPrefsDaysState ();
            setDaysButtonState();
            getSupportLoaderManager().getLoader(0).forceLoad();
        }
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bndl) {
        return new OutDocsActivity.MyCursorLoader(this, outDocRepo);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        scAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
    static class MyCursorLoader extends CursorLoader {

        private OutDocRepo repo;

        public MyCursorLoader(Context context, OutDocRepo repo) {
            super(context);
            this.repo = repo;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = repo.listOutDocs();
            return cursor;
        }

    }
    private boolean addOutDocForAllDeps (){
        AlertDialog.Builder adb = new AlertDialog.Builder(OutDocsActivity.this);
        adb.setTitle("Создать накладные...");
        adb.setMessage("Хотите добавить накладные для всех бригад " +AppController.getInstance().getDefs().getDescDep());
        adb.setNegativeButton("Нет", null);
        adb.setPositiveButton("Да", new AlertDialog.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onClick(DialogInterface dialog, int which) {
                int nextOutDocNumber = outDocRepo.getNextOutDocNumber();
                if (nextOutDocNumber == 0) {
                    Log.e(TAG, "outDocRepo.getNextOutDocNumber() returned 0");
                    MessageUtils.showToast(getApplicationContext(), "Ошибка нумерации. Накладные не будут созданы!", true);
                    return ;
                }

                if (!outDocRepo.createOutDocsForCurrentOper(nextOutDocNumber)) {
                    Log.e(TAG, "outDocRepo.createOutDocsForCurrentOper(nextOutDocNumber) returned 0");
                    MessageUtils.showToast(getApplicationContext(), "Ошибка при создании документов. Накладные не будут созданы!", true);
                    return ;
                };
                getSupportLoaderManager().getLoader(0).forceLoad();
            }
        });
        adb.show();
        return true;
    }
    private boolean addOutDocForAllSotr (){
        AlertDialog.Builder adb = new AlertDialog.Builder(OutDocsActivity.this);
        adb.setTitle("Создать накладные...");
        adb.setMessage("Хотите добавить накладные для всех сотрудников бригады " +AppController.getInstance().getDefs().getDescDep());
        adb.setNegativeButton("Нет", null);
        adb.setPositiveButton("Да", new AlertDialog.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onClick(DialogInterface dialog, int which) {
                int nextOutDocNumber = outDocRepo.getNextOutDocNumber();
                if (nextOutDocNumber == 0) {
                    Log.e(TAG, "outDocRepo.getNextOutDocNumber() returned 0");
                    MessageUtils.showToast(getApplicationContext(), "Ошибка нумерации. Накладные не будут созданы!", true);
                    return ;
                }

                if (!outDocRepo.createOutDocsForCurrentDep(nextOutDocNumber)) {
                    Log.e(TAG, "outDocRepo.createOutDocsForCurrentDep(nextOutDocNumber) returned 0");
                    MessageUtils.showToast(getApplicationContext(), "Ошибка при создании документов. Накладные не будут созданы!", true);
                    return ;
                };
                getSupportLoaderManager().getLoader(0).forceLoad();
            }
        });
        adb.show();
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();
        // Операции для выбранного пункта меню
        switch (id) {
            case R.id.action_out_docs:
                SyncIncoData task = new SyncIncoData();
                task.execute(new String[] { null });
                return true;
            case R.id.add_outdocs_for_all_sotr:
                return addOutDocForAllSotr ();
            case R.id.add_outdocs_for_all_deps:
                return addOutDocForAllDeps();
            default:
                return super.onOptionsItemSelected(item);
        }

    }

}
