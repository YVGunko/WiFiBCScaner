package com.example.yg.wifibcscaner.data.dto;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.example.yg.wifibcscaner.BR;

public class CurrentDocDetails extends BaseObservable {
    private String name = "";
    public CurrentDocDetails(String name) {
        setName(name);
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }
}
