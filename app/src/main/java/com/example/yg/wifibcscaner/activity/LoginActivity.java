package com.example.yg.wifibcscaner.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.yg.wifibcscaner.DataBaseHelper;
import com.example.yg.wifibcscaner.R;
import com.example.yg.wifibcscaner.controller.AppController;
import com.example.yg.wifibcscaner.data.model.Sotr;
import com.example.yg.wifibcscaner.data.model.user;
import com.example.yg.wifibcscaner.data.service.OutDocService;
import com.example.yg.wifibcscaner.utils.MessageUtils;

import java.util.List;

import static com.example.yg.wifibcscaner.utils.StringUtils.makeUserDesc;

public class LoginActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {

    OutDocService outDocService;
    private DataBaseHelper mDBHelper;
    private int idUser;
    Spinner spinnerName;
    EditText ePswd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mDBHelper = AppController.getInstance().getDbHelper();
        ePswd = (EditText) findViewById(R.id.ePswd);
        spinnerName = (Spinner) findViewById(R.id.spinnerName);
        spinnerName.setOnItemSelectedListener(this);
        loadSpinnerName();
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
        MessageUtils messageUtils = new MessageUtils();
        Spinner sp = (Spinner) parent;
        if(sp.getId() == R.id.spinnerName) {
            if (position >= 0) {
                idUser = mDBHelper.getIdByName(user.TABLE, parent.getItemAtPosition(position).toString());
                if (idUser == 0) messageUtils.showMessage(getApplicationContext(), "Пользователь не найден! Регистрация невозможна.");
            }
        }}
    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }
    private void loadSpinnerName() {
        // database handler

        // Spinner Drop down elements
        List<String> labels = mDBHelper.findAllIdNotZero(user.TABLE);

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinnerName.setAdapter(dataAdapter);
    }
    public void ocl_bLogin(View v) {
        MessageUtils messageUtils = new MessageUtils();
        if((idUser != 0)&(!ePswd.getText().toString().isEmpty())) {
            //check user's pswd checkUserPswdByName()
            if (mDBHelper.checkUserPswdById(idUser,ePswd.getText().toString())) { //pswd correct
                if (mDBHelper.defs.get_idUser()!=idUser) { //another user logged
                    mDBHelper.defs.set_idUser(idUser);      //set user as default
                    mDBHelper.defs.setDescUser(mDBHelper.getUserName(idUser));
                    AppController.getInstance().getMainActivityViews().setUser(makeUserDesc(mDBHelper.defs.getDescUser()));
                    messageUtils.showMessage(getApplicationContext(), "Вы вошли в систему как: "+mDBHelper.defs.getDescUser());

                    mDBHelper.currentOutDoc.set_id("");
                    mDBHelper.currentOutDoc.set_number(0); //clear currentOutdoc
                    AppController.getInstance().getMainActivityViews().setOutDoc(outDocService.makeOutDocDesc(new String[]{null}));

                    if (mDBHelper.getUserId_s(idUser)!=0) { //not a superuser
                        //select sotr(Employee), operation, division, department
                        mDBHelper.defs.set_Id_s(mDBHelper.getUserId_s(idUser)); //employee
                        Sotr sotr = mDBHelper.getSotrReq(mDBHelper.defs.get_Id_s());
                        mDBHelper.defs.setDescSotr(sotr.get_Sotr());
                        AppController.getInstance().getMainActivityViews().setEmployee(mDBHelper.defs.getDescUser());

                        if (sotr.get_Id_o()!=0) {
                            mDBHelper.defs.set_Id_o(sotr.get_Id_o()); //oper
                            mDBHelper.defs.setDescOper(mDBHelper.getOpers_Name_by_id(mDBHelper.defs.get_Id_o()));
                            AppController.getInstance().getMainActivityViews().setOperation(mDBHelper.defs.getDescOper());
                        }

                        if (sotr.get_Id_d()!=0) {
                            mDBHelper.defs.set_Id_d(sotr.get_Id_d()); //deps
                            mDBHelper.defs.setDescDep(mDBHelper.getDeps_Name_by_id(mDBHelper.defs.get_Id_d()));
                            AppController.getInstance().getMainActivityViews().setDepartment(mDBHelper.defs.getDescDep());
                        }
                        if (!sotr.getDivision_code().isEmpty()) {
                            mDBHelper.defs.setDivision_code(sotr.getDivision_code()); //oper
                            mDBHelper.defs.setDescDivision(mDBHelper.getDivisionsNameByCode(mDBHelper.defs.getDivision_code()));
                            AppController.getInstance().getMainActivityViews().setDivision(mDBHelper.defs.getDescDivision());
                        }
                    }
                    if (mDBHelper.updateDefsTable(mDBHelper.defs) == 0) {
                        messageUtils.showMessage(getApplicationContext(),"Ошибка при сохранении.");
                    } else mDBHelper.selectDefsTable();
                } else { //same user logged

                }

            } else {
                messageUtils.showMessage(getApplicationContext(), "Пароль не верен! Вход в систему не возможен!");
            }
        } else {
            if (idUser == 0) messageUtils.showMessage(getApplicationContext(), "Выберите пользователя!");
            if (ePswd.getText().toString().isEmpty()) messageUtils.showMessage(getApplicationContext(), "Введите пароль!");
        }
    }
}
