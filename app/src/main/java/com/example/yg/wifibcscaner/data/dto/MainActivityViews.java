package com.example.yg.wifibcscaner.data.dto;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.example.yg.wifibcscaner.BR;

public class MainActivityViews extends BaseObservable {
    private String order ;
    private String box;
    private String user;
    private String division;
    private String operation;
    private String department;
    private String employee;
    private String outDoc;

    public MainActivityViews() {
        super();
    }

    public MainActivityViews(String order, String box, String user, String division,
                             String operation, String department, String employee, String outDoc) {
        super();

        this.order = order;
        this.box = box;
        this.user = user;
        this.division = division;
        this.operation = operation;
        this.department = department;
        this.employee = employee;
        this.outDoc = outDoc;
    }

    @Bindable
    public String getBox() {
        return box;
    }
    public void setBox(String box) {
        this.box = box;
        notifyPropertyChanged(BR.box);
    }

    @Bindable
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
        notifyPropertyChanged(BR.user);
    }

    @Bindable
    public String getDivision() {
        return division;
    }
    public void setDivision(String division) {
        this.division = division;
        notifyPropertyChanged(BR.division);
    }

    @Bindable
    public String getOutDoc() {
        return outDoc;
    }
    public void setOutDoc(String outDoc) {
        this.outDoc = outDoc;
        notifyPropertyChanged(BR.outDoc);
    }

    @Bindable
    public String getOrder() {
        return order;
    }
    public void setOrder(String order) {
        this.order = order;
        notifyPropertyChanged(BR.order);
    }
    @Bindable
    public String getOperation() {
        return operation;
    }
    public void setOperation(String operation) {
        this.operation = operation;
        notifyPropertyChanged(BR.operation);
    }
    @Bindable
    public String getDepartment() {
        return department;
    }
    public void setDepartment(String department) {
        this.department = department;
        notifyPropertyChanged(BR.department);
    }

    @Bindable
    public String getEmployee() {
        return employee;
    }
    public void setEmployee(String employee) {
        this.employee = employee;
        notifyPropertyChanged(BR.employee);
    }
}
