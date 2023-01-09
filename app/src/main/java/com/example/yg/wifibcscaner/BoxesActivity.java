package com.example.yg.wifibcscaner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.yg.wifibcscaner.service.ApiUtils;
import com.example.yg.wifibcscaner.service.MessageUtils;
import com.example.yg.wifibcscaner.service.PartBoxRequest;

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
        mDBHelper = DataBaseHelper.getInstance(this);


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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Date dt;
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();
        // Операции для выбранного пункта меню
        switch (id) {
            case R.id.action_sendboxes:
//ТУт отправляем коробки на сервер
                try {
//                    ArrayList<OutDocs> odList = mDBHelper.getOutDocNotSent();
//                    for (int i=0; i < odList.size(); i = i + 1) {
                        ApiUtils.getOrderService(mDBHelper.defs.getUrl()).
                                addOutDoc(mDBHelper.getOutDocNotSent(),mDBHelper.defs.getDeviceId()).enqueue(new Callback<List<OutDocs>>() {

                            // TODO Обработать результат. Записать поле sent... если успешно
                            @Override
                            public void onResponse(Call<List<OutDocs>> call, Response<List<OutDocs>> response) {
                                MessageUtils messageUtils = new MessageUtils();
                                //Log.d("getOrderService","Ответ сервера на запрос синхронизации коробок: " + response.body());
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
                                Log.d("getOrderService", "OutDocs Error: " + t.getMessage());
                            }
                        });
//                    }

                    ArrayList<Boxes> boxesList = mDBHelper.getBoxes();
                    ArrayList<BoxMoves> boxMovesList = mDBHelper.getBoxMoves();
                    ArrayList<Prods> prodsList = mDBHelper.getProds();

                        ApiUtils.getBoxesService(mDBHelper.defs.getUrl()).addBoxes(new PartBoxRequest(boxesList, boxMovesList, prodsList),
                                mDBHelper.defs.get_idUser(),mDBHelper.defs.getDeviceId()).enqueue(new Callback<PartBoxRequest>() {
                            // TODO Обработать результат. Записать поле sent... если успешно
                            @Override
                            public void onResponse(Call<PartBoxRequest> call, Response<PartBoxRequest> response) {
                                MessageUtils messageUtils = new MessageUtils();
                                //Log.d("getBoxesService", "PartBoxRequest : " + response.body());
                                if (response.isSuccessful()) {
                                    for (Boxes boxReq : response.body().boxReqList)
                                        if (!mDBHelper.updateBoxesSentDate(boxReq))
                                            Log.d("getBoxesService", "Ошибка при записи даты в Box.");
                                    for (BoxMoves pmReq : response.body().movesReqList) {
                                        if (!mDBHelper.updateBoxMovesSentDate(pmReq))
                                            Log.d("getBoxesService", "Ошибка при записи даты в BoxMoves.");
                                        if (pmReq.get_Id_o() == mDBHelper.defs.get_idOperLast())
                                            if (!mDBHelper.updateBoxesSetArchiveTrue(pmReq.get_Id_b()))
                                                Log.d("getBoxesService", "Ошибка при установке признака архива Box.");
                                    }
                                    for (Prods pReq : response.body().partBoxReqList)
                                        if (!mDBHelper.updateProdsSentDate(pReq))
                                            Log.d("getBoxesService", "Ошибка при записи даты в Prods.");

                                    messageUtils.showMessage(getApplicationContext(), "Ок! Данные успешно выгружены на сервер!");
                                } else {
                                    messageUtils.showLongMessage(getApplicationContext(), "Ошибка при выгрузке данных на сервер!!");
                                }
                            }

                            @Override
                            public void onFailure(Call<PartBoxRequest> call, Throwable t) {
                                Log.d("getBoxesService error :", t.getMessage());
                                MessageUtils messageUtils = new MessageUtils();
                                messageUtils.showMessage(getApplicationContext(),  "Ошибка. ТаймАут.");
                            }
                        });
                }catch (Exception e) {
                    MessageUtils messageUtils = new MessageUtils();
                    messageUtils.showMessage(getApplicationContext(), "Отправлено неудачно.");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}