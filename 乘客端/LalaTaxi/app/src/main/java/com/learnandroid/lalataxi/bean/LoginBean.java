package com.learnandroid.lalataxi.bean;

import java.io.Serializable;

public class LoginBean implements Serializable{
    private boolean isCustom;
    private String phone;
    private String code;

    public LoginBean(boolean isCustom, String phone, String code){
        this.isCustom = isCustom;
        this.phone = phone;
        this.code = code;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public String getPhone() {
        return phone;
    }

    public String getCode() {
        return code;
    }
}
