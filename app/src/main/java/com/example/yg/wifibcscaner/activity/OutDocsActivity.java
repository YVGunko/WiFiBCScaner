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
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.yg.wifibcscaner.DataBaseHelper;
import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.Defs;
import com.example.yg.wifibcscaner.data.model.OutDocs;
import com.example.yg.wifibcscaner.data.repository.CurrentDocDetailsRepository;
import com.example.yg.wifibcscaner.service.DataExchangeService;
import com.example.yg.wifibcscaner.utils.ApiUtils;
import com.example.yg.wifibcscaner.utils.MessageUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.yg.wifibcscaner.data.service.OutDocService.makeOutDocDesc;
import static com.example.yg.wifibcscaner.data.repository.OutDocRepo.*;

public class OutDocsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    //Переменная для работы с БД
    private DataBaseHelper mDBHelper = AppController.getInstance().getDbHelper();
    ListView lvData;
    SimpleCursorAdapter scAdapter;
    String strTitle= "Выберите накладную";
    //String selectedTitle = "Выбрана Накл.№";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_out_docs, menu);
        return true;
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

        String[] from = new String[]{OutDocs.COLUMN_NUMBER, OutDocs.COLUMN_comment};
        int[] to = new int[]{R.id.tvNumber, R.id.tvText};

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
                        strTitle = makeOutDocDesc(scAdapter.getCursor().getString(1),scAdapter.getCursor().getString(3),
                                selectOutDocById(scAdapter.getCursor().getString(0)));
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
                            null,scAdapter.getCursor().getString(5),scAdapter.getCursor().getInt(6));

                } else {
                    mDBHelper.currentOutDoc.set_id(scAdapter.getCursor().getString(0));
                    mDBHelper.currentOutDoc.set_number(scAdapter.getCursor().getInt(1));
                    mDBHelper.currentOutDoc.set_comment(scAdapter.getCursor().getString(2));
                    mDBHelper.currentOutDoc.set_DT(scAdapter.getCursor().getString(3));
                    mDBHelper.currentOutDoc.set_Id_o(scAdapter.getCursor().getInt(4));
                    mDBHelper.currentOutDoc.set_sentToMasterDate(null);
                    mDBHelper.currentOutDoc.setDivision_code(scAdapter.getCursor().getString(5));
                    mDBHelper.currentOutDoc.setIdUser(scAdapter.getCursor().getInt(6));
                }

                result = makeOutDocDesc(new String[]{scAdapter.getCursor().getString(1)});

                if ((mDBHelper.defs.get_Id_o()==mDBHelper.defs.get_idOperFirst())&&(mDBHelper.defs.getDivision_code().equals(mDBHelper.puDivision))){
                    //Если прием производства и ПУ - установить бригаду из строки таблицы Prods c выбранной накладной
                    int iDep = mDBHelper.getId_dByOutdoc(mDBHelper.currentOutDoc.get_id());
                    if ((iDep != 0)&&(iDep!=mDBHelper.defs.get_Id_d())){

                        mDBHelper.defs.set_Id_d(iDep);
                        Defs defs = new Defs(iDep, mDBHelper.defs.get_Id_o(), mDBHelper.defs.get_Id_s(),
                                mDBHelper.defs.get_Host_IP(), mDBHelper.defs.get_Port(),
                                mDBHelper.defs.getDivision_code(),  mDBHelper.defs.get_idUser(), mDBHelper.defs.getDeviceId());
                        if (mDBHelper.updateDefsTable(defs) != 0) {
                            mDBHelper.selectDefsTable();
                            MessageUtils.showToast(getApplicationContext(),"Сохранено."+mDBHelper.defs.descDep, false);
                        } else {
                            MessageUtils.showToast(getApplicationContext(),"Ошибка при сохранении.",false);
                        }
                    }
                    result = makeOutDocDesc(new String[]{scAdapter.getCursor().getString(1)+" "+mDBHelper.defs.descDep});
                }
                OutDocsActivity.this.setTitle(result);
                AppController.getInstance().getMainActivityViews().setOutDoc(result);
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
        int docNum = outDocsAddRec();
        // добавляем запись
        if (docNum!=0) {
            // получаем новый курсор с данными
            getSupportLoaderManager().getLoader(0).forceLoad();

            OutDocsActivity.this.setTitle(makeOutDocDesc(new String[]{String.valueOf(docNum)}));
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
        try {
            scAdapter.swapCursor(cursor);
        }catch (IllegalArgumentException e){
            Log.e("","",e);
        }
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
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_out_docs:
                new DataExchangeService().call();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

}
