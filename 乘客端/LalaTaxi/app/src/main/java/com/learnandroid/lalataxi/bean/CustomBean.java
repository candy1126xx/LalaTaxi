package com.learnandroid.lalataxi.bean;

import java.io.Serializable;

public class CustomBean implements Serializable {
    private String phoneCustom;
    private String portStart;
    private String portEnd;
    private int price;
    public CustomBean(String phoneCustom, String portStart, String portEnd, int price){
        this.phoneCustom = phoneCustom;
        this.portStart = portStart;
        this.portEnd = portEnd;
        this.price = price;
    }

    public String getPhoneCustom() {
        return phoneCustom;
    }

    public String getPortStart() {
        return portStart;
    }

    public String getPortEnd() {
        return portEnd;
    }

    public int getPrice(){
        return price;
    }
}
