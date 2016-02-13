package com.learnandroid.lalataxi.bean;

import java.io.Serializable;

public class DriverBean implements Serializable{
    private String phoneCustom;
    private String phoneDriver;
    public DriverBean(String phoneCustom, String phoneDriver){
        this.phoneCustom = phoneCustom;
        this.phoneDriver = phoneDriver;
    }

    public String getPhoneCustom() {
        return phoneCustom;
    }

    public String getPhoneDriver() {
        return phoneDriver;
    }
}
