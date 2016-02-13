package com.learnandroid.lalataxi.bean;

import java.io.Serializable;

public class CodeBean implements Serializable{
    private String code;
    public CodeBean(String code){
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
