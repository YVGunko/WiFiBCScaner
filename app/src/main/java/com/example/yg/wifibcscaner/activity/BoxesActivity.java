package com.example.yg.wifibcscaner.activity;

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

import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.BoxMoves;
import com.example.yg.wifibcscaner.data.model.Boxes;
import com.example.yg.wifibcscaner.DataBaseHelper;
import com.example.yg.wifibcscaner.data.model.Defs;
import com.example.yg.wifibcscaner.data.model.OutDocs;
import com.example.yg.wifibcscaner.data.model.Prods;
import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.data.repo.BoxRepo;
import com.example.yg.wifibcscaner.data.repo.DefsRepo;
import com.example.yg.wifibcscaner.data.repo.DepartmentRepo;
import com.example.yg.wifibcscaner.data.repo.DivisionRepo;
import com.example.yg.wifibcscaner.data.repo.OperRepo;
import com.example.yg.wifibcscaner.data.repo.OutDocRepo;
import com.example.yg.wifibcscaner.data.repo.SotrRepo;
import com.example.yg.wifibcscaner.data.repo.UserRepo;
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
    private static final String TAG = "sProject -> BoxesActivity.";

    private final OutDocRepo outDocRepo = new OutDocRepo();
    private final BoxRepo boxRepo = new BoxRepo();

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


//Создаем адаптер
        adapter = new SimpleAdapter(this, AppController.getInstance().getDbHelper().listboxes(), R.layout.adapter_item, from, to);
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
                        if (AppController.getInstance().getDbHelper().deleteFromTable(Prods.TABLE_prods,Prods.COLUMN_ID,sPdId)){
                        //удалили подошву. проверить есть ли еще подошва по этому движению.
                        //если нет удалить движение
                            if (AppController.getInstance().getDbHelper().deleteFromTable(BoxMoves.TABLE_bm,BoxMoves.COLUMN_ID,sBmId)){
                                if (!AppController.getInstance().getDbHelper().deleteFromTable(Boxes.TABLE_boxes,Boxes.COLUMN_ID,sBId)){
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
                    ApiUtils.getOrderService(AppController.getInstance().getDefs().getUrl()).
                            addOutDoc(outDocRepo.getOutDocNotSent(),AppController.getInstance().getDefs().getDeviceId()).enqueue(new Callback<List<OutDocs>>() {
                        @Override
                        public void onResponse(Call<List<OutDocs>> call, Response<List<OutDocs>> response) {
                            if(response.isSuccessful()) {
                                outDocRepo.updateOutDocsetSentToMasterDate(response.body());

                                if (response.body().size()!=0) {
                                    MessageUtils.showToast(getApplicationContext(), "Ок! Накладные выгружены!", false);
                                }
                                try {
                                    ArrayList<Boxes> boxesList = AppController.getInstance().getDbHelper().getBoxes();
                                    ArrayList<BoxMoves> boxMovesList = AppController.getInstance().getDbHelper().getBoxMoves();
                                    ArrayList<Prods> prodsList = AppController.getInstance().getDbHelper().getProds();
                                    ApiUtils.getOrderService(AppController.getInstance().getDefs().getUrl()).addBoxes(new PartBoxRequest(boxesList, boxMovesList, prodsList),
                                            AppController.getInstance().getDefs().get_idUser(),AppController.getInstance().getDefs().getDeviceId()).enqueue(new Callback<PartBoxRequest>() {
                                        @Override
                                        public void onResponse(Call<PartBoxRequest> call, Response<PartBoxRequest> response) {
                                            if (response.isSuccessful()) {
                                                boxRepo.updateWithResponse(response.body());
                                            } else {
                                                MessageUtils.showToast(getApplicationContext(), "Ошибка при выгрузке данных на сервер!", true);
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<PartBoxRequest> call, Throwable t) {
                                            Log.d(TAG, t.getMessage());
                                            MessageUtils.showToast(getApplicationContext(),  "Ошибка. ТаймАут.", true);
                                        }
                                    });
                                }catch (Exception e) {
                                   MessageUtils.showToast(getApplicationContext(),"Ошибка при выгрузке коробок.", true);
                                }
                            }else {
                                MessageUtils.showToast(getApplicationContext(), "Ошибка при выгрузке накладных!", true);
                            }
                        }
                        @Override
                        public void onFailure(Call<List<OutDocs>> call, Throwable t) {
                            MessageUtils messageUtils = new MessageUtils();
                            messageUtils.showLongMessage(getApplicationContext(), t.getMessage() + ". Ошибка при выгрузке накладных!");
                            Log.d("getOrderService", "OutDocs Error: " + t.getMessage());
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