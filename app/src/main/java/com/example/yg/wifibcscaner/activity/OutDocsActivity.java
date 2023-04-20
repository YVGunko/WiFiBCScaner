package com.example.yg.wifibcscaner.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
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

import com.example.yg.wifibcscaner.DataBaseHelper;
import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.Defs;
import com.example.yg.wifibcscaner.data.model.OutDocs;
import com.example.yg.wifibcscaner.data.repository.OutDocRepo;
import com.example.yg.wifibcscaner.data.service.OutDocService;
import com.example.yg.wifibcscaner.service.DataExchangeService;
import com.example.yg.wifibcscaner.utils.MessageUtils;
import com.example.yg.wifibcscaner.utils.MyStringUtils;

import static com.example.yg.wifibcscaner.data.service.OutDocService.makeOutDocDesc;
import static com.example.yg.wifibcscaner.utils.MyStringUtils.makeSotrDesc;

public class OutDocsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    OutDocService outDocService ;
    MyStringUtils myStringUtils;
    OutDocRepo outDocRepo = new OutDocRepo();

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
                        MessageUtils.showToast(AppController.getInstance().getApplicationContext(),outDocService.makeOutDocDesc(scAdapter.getCursor().getString(1),scAdapter.getCursor().getString(3),
                                outDocRepo.selectOutDocById(scAdapter.getCursor().getString(0))), true);
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

            final String[] outDocDesc = {outDocService.makeOutDocNumAndDateDesc(scAdapter.getCursor().getString(1), scAdapter.getCursor().getString(3))};

        adb.setTitle("Выбор накладной...");
        adb.setMessage("Выбираем накладную: " + outDocDesc[0]);
        adb.setNegativeButton("Нет", null);
        adb.setPositiveButton("Да", new AlertDialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

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
                }

                if (!(mDBHelper.defs.get_Id_o()==mDBHelper.defs.get_idOperFirst() || mDBHelper.defs.get_Id_o()==mDBHelper.defs.get_idOperLast())
                        && mDBHelper.defs.getDivision_code().equals(mDBHelper.tepDivision)) {
                    //Если не производство и ТЭП - установить бригаду из строки таблицы Prods c выбранной накладной
                    final int iDep = mDBHelper.getId_dByOutdoc(mDBHelper.currentOutDoc.get_id());
                    if ((iDep != 0)&&(iDep!=mDBHelper.defs.get_Id_d())){
                        mDBHelper.defs.set_Id_d(iDep);

                        final int iSotr = mDBHelper.getId_sByOutdoc(mDBHelper.currentOutDoc.get_id());
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
                    }
                }
                OutDocsActivity.this.setTitle(outDocDesc[0]);
                AppController.getInstance().getMainActivityViews().setDepartment(mDBHelper.getDeps_Name_by_id(mDBHelper.defs.get_Id_d()));
                AppController.getInstance().getMainActivityViews().setEmployee(makeSotrDesc(new String[] {mDBHelper.getSotr_Name_by_id(mDBHelper.defs.get_Id_s())}));
                AppController.getInstance().getMainActivityViews().setOutDoc(outDocDesc[0]);
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
        if (outDocRepo.outDocsAddRec()) {
            // получаем новый курсор с данными
            getSupportLoaderManager().getLoader(0).forceLoad();

            OutDocsActivity.this.setTitle(makeOutDocDesc(new String[]{String.valueOf(mDBHelper.currentOutDoc.get_number()), mDBHelper.currentOutDoc.get_DT()}));
            AppController.getInstance().getMainActivityViews().setOutDoc(makeOutDocDesc(new String[]{String.valueOf(mDBHelper.currentOutDoc.get_number()), mDBHelper.currentOutDoc.get_DT()}));
        } else {
            MessageUtils.showToast(getApplicationContext(),"Новая накладная не создана. Попробуйте позже.", false);
        }
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bndl) {
        return new OutDocsActivity.MyCursorLoader(this, outDocRepo);
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

        private OutDocRepo outDocRepo;

        public MyCursorLoader(Context context, OutDocRepo outDocRepo) {
            super(context);
            this.outDocRepo = outDocRepo;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = outDocRepo.listOutDocs();
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
