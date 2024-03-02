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
import com.example.yg.wifibcscaner.data.model.Defs;
import com.example.yg.wifibcscaner.data.model.Sotr;
import com.example.yg.wifibcscaner.data.repo.DefsRepo;
import com.example.yg.wifibcscaner.data.repo.DepartmentRepo;
import com.example.yg.wifibcscaner.data.repo.DivisionRepo;
import com.example.yg.wifibcscaner.data.repo.OperRepo;
import com.example.yg.wifibcscaner.data.repo.OrderRepo;
import com.example.yg.wifibcscaner.data.repo.SotrRepo;
import com.example.yg.wifibcscaner.data.repo.UserRepo;
import com.example.yg.wifibcscaner.service.MessageUtils;

import java.util.List;

public class LoginActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {
    private DataBaseHelper mDBHelper = AppController.getInstance().getDbHelper();
    private Defs defs = AppController.getInstance().getDefs();
    private int idUser;
    private final OperRepo operRepo = new OperRepo();
    private final DivisionRepo divRepo = new DivisionRepo();
    private final DepartmentRepo depRepo = new DepartmentRepo();
    private final OrderRepo orderRepo = new OrderRepo();
    private final SotrRepo sotrRepo = new SotrRepo();
    private final UserRepo userRepo = new UserRepo();
    private final DefsRepo defsRepo = new DefsRepo();
    Spinner spinnerName;
    EditText ePswd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
                idUser = userRepo.getUserIdByName(parent.getItemAtPosition(position).toString());
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
        List<String> labels = userRepo.getAllUserName();

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
        if((idUser != 0)&(!ePswd.getText().toString().isEmpty())) {
            //check user's pswd checkUserPswdByName()
            if (userRepo.checkUserPswdById(idUser,ePswd.getText().toString())) { //pswd correct
                if (defs.get_idUser()!=idUser) { //another user logged
                    defs.set_idUser(idUser);      //set user as default
                    defs.setDescUser(userRepo.getUserName(idUser));
                    AppController.getInstance().getCurrentOutDoc().set_id("");
                    AppController.getInstance().getCurrentOutDoc().set_number(0); //clear currentOutdoc

                    MessageUtils.showToast(getApplicationContext(), "Вы вошли в систему как: "+defs.getDescUser(), false);

                    if (userRepo.getUserSotrById(idUser)!=0) { //not a superuser
                        //select operation, division, department
                        defs.set_Id_s(userRepo.getUserSotrById(idUser)); //employee
                        Sotr sotr = sotrRepo.getSotrReq(defs.get_Id_s());
                        if (sotr.get_Id_o()!=0) defs.set_Id_o(sotr.get_Id_o()); //oper
                        if (sotr.get_Id_d()!=0) defs.set_Id_d(sotr.get_Id_d()); //deps
                        if (!sotr.getDivision_code().isEmpty()) defs.setDivision_code(sotr.getDivision_code()); //oper
                    }
                    if (defsRepo.updateDefsTable(defs) == 0) {
                        MessageUtils.showToast(getApplicationContext(),"Ошибка при сохранении.", true);
                    } else AppController.getInstance().setDefs(defs);
                } else { //same user logged

                }

            } else {
                MessageUtils.showToast(getApplicationContext(), "Пароль не верен! Вход в систему не возможен!", true);
            }
        } else {
            if (idUser == 0) MessageUtils.showToast(getApplicationContext(), "Выберите пользователя!", false);
            if (ePswd.getText().toString().isEmpty()) MessageUtils.showToast(getApplicationContext(), "Введите пароль!", false);
        }
    }
}
