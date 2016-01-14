package com.learnandroid.lalataxi.bean;

import java.io.Serializable;

public class LoginResultBean implements Serializable{
    private boolean isSuccess;
    public LoginResultBean(boolean isSuccess){
        this.isSuccess = isSuccess;
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}
