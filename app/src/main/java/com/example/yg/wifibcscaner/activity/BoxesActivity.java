package com.example.yg.wifibcscaner.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.BoxMoves;
import com.example.yg.wifibcscaner.data.model.Boxes;
import com.example.yg.wifibcscaner.data.model.Prods;
import com.example.yg.wifibcscaner.data.repo.BoxRepo;
import com.example.yg.wifibcscaner.data.repo.DataSendRepo;
import com.example.yg.wifibcscaner.data.repo.OutDocRepo;
import com.example.yg.wifibcscaner.service.MessageUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

import static com.example.yg.wifibcscaner.data.model.OutDocs.COLUMN_number;
import static com.example.yg.wifibcscaner.data.model.Prods.COLUMN_Id_d;
import static com.example.yg.wifibcscaner.data.model.Prods.COLUMN_idOutDocs;


public class BoxesActivity extends AppCompatActivity {
    private static final String TAG = "sProject -> BoxesActivity.";

    private final BoxRepo boxRepo = new BoxRepo();
    private final OutDocRepo outDocRepo = new OutDocRepo();

    SimpleAdapter adapter = null;
    ListView listView = null;
    String[] from = {"Ord", "Cust"};
    int[] to = {R.id.textView, R.id.textView2};
    boolean sentToMasterDate;
    String strTitle = "";
    String boxTotal = "";
    String outDocId = "";
    String outDocNumber = "";
    int depId = 0;

    @Override
    protected void onResume() {
        super.onResume();
        if (StringUtils.isBlank(strTitle))
            this.setTitle("Коробки. Приняты, не отправлены.");
        else {
            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle(strTitle);
            actionBar.setSubtitle(Html.fromHtml("<font color='#FFBF00'>"+boxTotal+"</font>"));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getIntent().getExtras();

        if (b != null) {
            outDocId = b.getString(COLUMN_idOutDocs);
            if (StringUtils.isNotBlank(outDocId)) boxTotal = outDocRepo.selectCurrentOutDocDetails(outDocId);
        }
        if (b != null) {
            outDocNumber = b.getString(COLUMN_number);
            if (StringUtils.isNotBlank(outDocNumber)) strTitle = "Коробки накладной №".concat(outDocNumber);
        }
        if (b != null) depId = b.getInt(COLUMN_Id_d, 0);

        setContentView(R.layout.activity_boxes);


//Создаем адаптер
        adapter = new SimpleAdapter(this, boxRepo.listboxes(outDocId, depId, StringUtils.isBlank(outDocId)), R.layout.adapter_item, from, to);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> var1, View var2, final int position, long id) {
                String sTmp = adapter.getItem(position).toString();
                // if @sent, omit delete dialog and get user informed that deletion is impossible
                try {
                    if (sTmp.substring(sTmp.indexOf("sent=")+5,sTmp.indexOf("sent=")+6).equals("Y")
                            && sTmp.substring(sTmp.indexOf("arch=")+5,sTmp.indexOf("arch=")+6).equals("Y")){
                        Log.d(TAG,"The Box is already sent or archived. Impossible to delete." );
                        MessageUtils.showToast(getApplicationContext(),  "Нет возможности удалить! Коробка либо уже в архиве либо отправлена на сервер.", false);
                    } else {
                        sTmp = sTmp.substring(sTmp.indexOf("Cust=")+5,sTmp.indexOf("Cust=")+5+40).concat("...") ;
                        AlertDialog.Builder adb=new AlertDialog.Builder(BoxesActivity.this);
                        adb.setTitle("Удалить запись?");
                        adb.setMessage("Удаляем запись " + sTmp);
                        final int positionToRemove = position;
                        adb.setNegativeButton("Отменить", null);
                        adb.setPositiveButton("Удалить", new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String sTmp = adapter.getItem(position).toString();
                                //("bId=",cursor.getString(10)+"/bId");
                                String sBId = sTmp.substring(sTmp.indexOf("bId=")+4,sTmp.indexOf("/bId"));
                                //readBox.put("bmId",cursor.getString(11));
                                String sBmId = sTmp.substring(sTmp.indexOf("bmId=")+5,sTmp.indexOf("/bmId"));
                                String sPdId = sTmp.substring(sTmp.indexOf("pdId=")+5,sTmp.indexOf("/pdId"));
                                //Проверить нет ли других операций по этой коробке. Если это расходная операция -
                                //приходная есть по умолчанию. Если это приходная операция - проверить наличие других.
                                if (AppController.getInstance().getDbHelper().deleteFromTable(Prods.TABLE_prods,Prods.COLUMN_ID,sPdId)){
                                    //удалили подошву. проверить есть ли еще подошва по этому движению.
                                    //если нет удалить движение
                                    if (AppController.getInstance().getDbHelper().deleteFromTable(BoxMoves.TABLE_bm,BoxMoves.COLUMN_ID,sBmId)){
                                        if (!AppController.getInstance().getDbHelper().deleteFromTable(Boxes.TABLE_boxes,Boxes.COLUMN_ID,sBId)){
                                            Log.d(TAG,"Коробка не может быть удалена из-за ссылок других операций! Id= "+sBId );
                                        }
                                        MessageUtils.showToast(getApplicationContext(), "Ок! Успешно!", false);
                                    }
                                    else {
                                        Log.d(TAG,"Движения Подошвы не можгут быть удалены из-за ссылок других операций! Id= "+sBmId );
                                        MessageUtils.showToast(getApplicationContext(), "Нет возможности удалить!", false);
                                    }
                                    adapter = new SimpleAdapter(BoxesActivity.this, boxRepo.listboxes(outDocId, depId, StringUtils.isBlank(outDocId)), R.layout.adapter_item, from, to);

                                    listView.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                }
                                else {
                                    Log.d(TAG,"Ошибка при удалении Подошвы! Id= "+sPdId );
                                    MessageUtils.showToast(getApplicationContext(),  "Нет возможности удалить!", false);
                                }
                            }});
                        adb.show();
                    }
                } catch (IndexOutOfBoundsException ex) {
                    Log.e(TAG,"No sent marker found" );
                    MessageUtils.showToast(getApplicationContext(),  "Нет возможности удалить!", false);
                }
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.boxes_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Date dt;
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();
        // Операции для выбранного пункта меню
        switch (id) {
            case R.id.action_sendboxes:
                DataSendRepo dsRepo = new DataSendRepo();
                dsRepo.sendData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}