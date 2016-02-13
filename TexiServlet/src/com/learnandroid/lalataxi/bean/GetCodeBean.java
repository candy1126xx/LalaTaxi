package com.learnandroid.lalataxi.bean;

import java.io.Serializable;

public class GetCodeBean implements Serializable{
    private String phone;
    public GetCodeBean(String phone){
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }
}
