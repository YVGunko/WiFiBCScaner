package com.example.yg.wifibcscaner.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.yg.wifibcscaner.DataBaseHelper;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.OutDocs;
import com.example.yg.wifibcscaner.data.model.Prods;
import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.data.model.BoxMoves;
import com.example.yg.wifibcscaner.data.model.Boxes;
import com.example.yg.wifibcscaner.service.DataExchangeService;
import com.example.yg.wifibcscaner.utils.ApiUtils;
import com.example.yg.wifibcscaner.utils.MessageUtils;
import com.example.yg.wifibcscaner.data.dto.PartBoxRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class BoxesActivity extends AppCompatActivity {
    //Переменная для работы с БД
    private DataBaseHelper mDBHelper;
    SimpleAdapter adapter = null;
    ListView listView = null;
    String[] from = {"Ord", "Cust"};
    int[] to = {R.id.textView, R.id.textView2};

    @Override
    protected void onResume() {
        super.onResume();
        this.setTitle("Коробки.");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boxes);
        mDBHelper = AppController.getInstance().getDbHelper();


//Создаем адаптер
        adapter = new SimpleAdapter(this, mDBHelper.listboxes(), R.layout.adapter_item, from, to);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> var1, View var2, final int position, long id) {
                String sTmp = adapter.getItem(position).toString();
                sTmp = sTmp.substring(sTmp.indexOf("Ord=")+4,sTmp.indexOf("Ord=")+4+20)+"...\n"+
                        sTmp.substring(sTmp.indexOf("Cust=")+5,sTmp.indexOf("Cust=")+5+40)+"...";
                AlertDialog.Builder adb=new AlertDialog.Builder(BoxesActivity.this);
                adb.setTitle("Удалить запись?");
                adb.setMessage("Удаляем запись " + sTmp);
                final int positionToRemove = position;
                adb.setNegativeButton("Отменить", null);
                adb.setPositiveButton("Удалить", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        MessageUtils messageUtils = new MessageUtils();
                        //MyDataObject.remove(positionToRemove);
                        String sTmp = adapter.getItem(position).toString();
                        //("bId=",cursor.getString(10)+"/bId");
                        String sBId = sTmp.substring(sTmp.indexOf("bId=")+4,sTmp.indexOf("/bId"));
                        //readBox.put("bmId",cursor.getString(11));
                        String sBmId = sTmp.substring(sTmp.indexOf("bmId=")+5,sTmp.indexOf("/bmId"));
                        String sPdId = sTmp.substring(sTmp.indexOf("pdId=")+5,sTmp.indexOf("/pdId"));
                        //Проверить нет ли других операций по этой коробке. Если это расходная операция -
                        //приходная есть по умолчанию. Если это приходная операция - проверить наличие других.
                        if (mDBHelper.deleteFromTable(Prods.TABLE_prods,Prods.COLUMN_ID,sPdId)){
                        //удалили подошву. проверить есть ли еще подошва по этому движению.
                        //если нет удалить движение
                            if (mDBHelper.deleteFromTable(BoxMoves.TABLE_bm,BoxMoves.COLUMN_ID,sBmId)){
                                if (!mDBHelper.deleteFromTable(Boxes.TABLE_boxes,Boxes.COLUMN_ID,sBId)){
                                    Log.d("1","Коробка не может быть удалена из-за ссылок других операций! Id= "+sBId );
                                }
                                messageUtils.showMessage(getApplicationContext(), "Ок! Успешно!");
                            }
                            else {
                                Log.d("1","Движения Подошвы не можгут быть удалены из-за ссылок других операций! Id= "+sBmId );
                                messageUtils.showMessage(getApplicationContext(), "Движения Подошвы не могут быть удалены!");
                            }
                        }
                        else {
                            Log.d("1","Ошибка при удалении Подошвы! Id= "+sPdId );
                            messageUtils.showMessage(getApplicationContext(),  "Ошибка при удалении Подошвы!");
                        }
                    }});
                adb.show();
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.boxes_menu, menu);
        return true;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Date dt;
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();
        // Операции для выбранного пункта меню
        switch (id) {
            case R.id.action_sendboxes:
                new DataExchangeService().call();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}