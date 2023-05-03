package com.example.yg.wifibcscaner;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
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
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.yg.wifibcscaner.service.ApiUtils;
import com.example.yg.wifibcscaner.service.MessageUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutDocsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    //Переменная для работы с БД
    private DataBaseHelper mDBHelper;
    ListView lvData;
    SimpleCursorAdapter scAdapter;
    String strTitle= "Выберите накладную";
    String selectedTitle = "Выбрана Накл.№";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_out_docs, menu);
        return true;
    }

    private class SyncIncoData extends AsyncTask<String, Integer, String> {
        ProgressBar pbar;
        Integer counter;

        @Override
        protected String doInBackground(String... urls) {
            counter = 0;
            try {
                ApiUtils.getOrderService(mDBHelper.defs.getUrl()).
                        addOutDoc(mDBHelper.getOutDocNotSent(),mDBHelper.defs.getDeviceId()).enqueue(new Callback<List<OutDocs>>() {

                    // TODO Обработать результат. Записать поле sent... если успешно
                    @Override
                    public void onResponse(Call<List<OutDocs>> call, Response<List<OutDocs>> response) {
                        MessageUtils messageUtils = new MessageUtils();
                        Log.d("OutDoc","Ответ сервера на запрос синхронизации накладных: " + response.body().size());
                        if(response.isSuccessful()) {
                            for(OutDocs boxes : response.body())
                                mDBHelper.updateOutDocsetSentToMasterDate(boxes);

                            if (response.body().size()!=0) {
                                messageUtils.showMessage(getApplicationContext(), "Ок! Накладные выгружены!");
                            }
                            //Запросить синхронизацию коробок и из частей
                        }else {
                            messageUtils.showLongMessage(getApplicationContext(), "Ошибка при выгрузке накладных!");
                        }
                    }
                    @Override
                    public void onFailure(Call<List<OutDocs>> call, Throwable t) {
                        MessageUtils messageUtils = new MessageUtils();
                        messageUtils.showLongMessage(getApplicationContext(), t.getMessage() + ". Ошибка при выгрузке накладных!");
                        Log.d("OutDoc", "OutDocs Error: " + t.getMessage());
                    }
                });
            } catch (Exception e) {
                //messageUtils.showLongMessage(getApplicationContext(), "Ошибка при приеме заказов!");
                Log.d("OutDoc","Ответ сервера на запрос новых заказов: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MessageUtils messageUtils = new MessageUtils();
            messageUtils.showLongMessage(getApplicationContext(), "Синхронизация данных начата.");
        }

        @Override
        protected void onPostExecute(String result) {
            MessageUtils messageUtils = new MessageUtils();
            messageUtils.showLongMessage(getApplicationContext(), "Синхронизация данных окончена.");
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
        mDBHelper = DataBaseHelper.getInstance(this);

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
                                + mDBHelper.selectCurrentOutDocDetails(scAdapter.getCursor().getString(0));
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

                if (mDBHelper.currentOutDoc == null) {
                    mDBHelper.currentOutDoc = new OutDocs(scAdapter.getCursor().getString(0), scAdapter.getCursor().getInt(4),
                            scAdapter.getCursor().getInt(1), scAdapter.getCursor().getString(2), scAdapter.getCursor().getString(3),
                            null,scAdapter.getCursor().getString(5),scAdapter.getCursor().getInt(6),
                            scAdapter.getCursor().getInt(7), scAdapter.getCursor().getInt(8));

                } else {
                    mDBHelper.currentOutDoc.set_id(scAdapter.getCursor().getString(0));
                    mDBHelper.currentOutDoc.set_number(scAdapter.getCursor().getInt(1));
                    mDBHelper.currentOutDoc.set_comment(scAdapter.getCursor().getString(2));
                    mDBHelper.currentOutDoc.set_DT(scAdapter.getCursor().getString(3));
                    mDBHelper.currentOutDoc.set_Id_o(scAdapter.getCursor().getInt(4));
                    mDBHelper.currentOutDoc.set_sentToMasterDate(null);
                    mDBHelper.currentOutDoc.setDivision_code(scAdapter.getCursor().getString(5));
                    mDBHelper.currentOutDoc.setIdUser(scAdapter.getCursor().getInt(6));
                    mDBHelper.currentOutDoc.setIdSotr(scAdapter.getCursor().getInt(7));
                    mDBHelper.currentOutDoc.setIdDeps(scAdapter.getCursor().getInt(8));
                }

                result = selectedTitle +scAdapter.getCursor().getString(1);

                if ((mDBHelper.defs.get_Id_o()==mDBHelper.defs.get_idOperFirst())&&(mDBHelper.defs.getDivision_code().equals(mDBHelper.puDivision))){
                    //Если прием производства и ПУ - установить бригаду из строки таблицы Prods c выбранной накладной
                    int iDep = mDBHelper.getId_dByOutdoc(mDBHelper.currentOutDoc.get_id());
                    if ((iDep != 0)&&(iDep!=mDBHelper.defs.get_Id_d())){
                        MessageUtils messageUtils = new MessageUtils();
                        mDBHelper.defs.set_Id_d(iDep);
                        Defs defs = new Defs(iDep, mDBHelper.defs.get_Id_o(), mDBHelper.defs.get_Id_s(),
                                mDBHelper.defs.get_Host_IP(), mDBHelper.defs.get_Port(),
                                mDBHelper.defs.getDivision_code(),  mDBHelper.defs.get_idUser(), mDBHelper.defs.getDeviceId());
                        if (mDBHelper.updateDefsTable(defs) != 0) {
                            mDBHelper.selectDefsTable();
                            messageUtils.showMessage(getApplicationContext(),"Сохранено."+mDBHelper.defs.descDep);
                        } else {
                            messageUtils.showMessage(getApplicationContext(),"Ошибка при сохранении.");
                        }
                    }
                    result = selectedTitle +scAdapter.getCursor().getString(1)+" "+mDBHelper.defs.descDep;
                }

                if (!(mDBHelper.defs.get_Id_o()==mDBHelper.defs.get_idOperFirst() || mDBHelper.defs.get_Id_o()==mDBHelper.defs.get_idOperLast())
                        && mDBHelper.defs.getDivision_code().equals(mDBHelper.tepDivision)) {
                    //Если не производство и ТЭП - установить бригаду из строки таблицы Prods c выбранной накладной
                    final int iDep = mDBHelper.getId_dByOutdoc(mDBHelper.currentOutDoc.get_id());
                    if ((iDep != 0)&&(iDep!=mDBHelper.defs.get_Id_d())){
                        mDBHelper.defs.set_Id_d(iDep);

                        final int iSotr = mDBHelper.getId_sByOutDocId(mDBHelper.currentOutDoc.get_id());
                        if ((iSotr != 0)&&(iSotr!=mDBHelper.defs.get_Id_d())) {
                            mDBHelper.defs.set_Id_s(iSotr);
                            Defs defs = new Defs(iDep, mDBHelper.defs.get_Id_o(), mDBHelper.defs.get_Id_s(),
                                    mDBHelper.defs.get_Host_IP(), mDBHelper.defs.get_Port(),
                                    mDBHelper.defs.getDivision_code(), mDBHelper.defs.get_idUser(), mDBHelper.defs.getDeviceId());
                            if (mDBHelper.updateDefsTable(defs) != 0) {
                                mDBHelper.selectDefsTable();
                                MessageUtils.showToast(getApplicationContext(), "Сохранено." + mDBHelper.defs.descDep, false);
                            } else {
                                MessageUtils.showToast(getApplicationContext(), "Ошибка при сохранении.", false);
                            }
                        }
                    }else {
                        mDBHelper.currentOutDoc.set_comment(mDBHelper.defs.descDep+", "+mDBHelper.defs.descUser);
                        mDBHelper.updateOutDocCommentById(mDBHelper.currentOutDoc.get_comment(), mDBHelper.currentOutDoc.get_id());
                    }
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
    public void onButtonClick(View view) {
        int docNum = mDBHelper.outDocsAddRec();
        // добавляем запись
        if (docNum!=0) {
            // получаем новый курсор с данными
            getSupportLoaderManager().getLoader(0).forceLoad();

            OutDocsActivity.this.setTitle(selectedTitle +String.valueOf(docNum));
        } else {
            MessageUtils messageUtils = new MessageUtils();
            messageUtils.showMessage(getApplicationContext(),"Ошибка при добавлении записи.");
        }
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bndl) {
        return new OutDocsActivity.MyCursorLoader(this, mDBHelper);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        scAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
    static class MyCursorLoader extends CursorLoader {

        private DataBaseHelper db;

        public MyCursorLoader(Context context, DataBaseHelper db) {
            super(context);
            this.db = db;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = db.listOutDocs();
            return cursor;
        }

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
            default:
                return super.onOptionsItemSelected(item);
        }

    }

}
