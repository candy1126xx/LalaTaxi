package com.learnandroid.lalataxi.bean;

import java.io.Serializable;

public class CustomBean implements Serializable {
    private String phoneCustom;
    private String startName;
    private double startLat;
    private double startLong;
    private String endName;
    private double endLat;
    private double endLong;
    private int price;
    public CustomBean(String phoneCustom, String startName, double startLat, double startLong,
                      String endName, double endLat, double endLong, int price){
        this.phoneCustom = phoneCustom;
        this.startName = startName;
        this.startLat = startLat;
        this.startLong = startLong;
        this.endName = endName;
        this.endLat = endLat;
        this.endLong = endLong;
        this.price = price;
    }

    public String getPhoneCustom() {
        return phoneCustom;
    }

    public String getStartName() {
        return startName;
    }

    public double getStartLong() {
        return startLong;
    }

    public String getEndName() {
        return endName;
    }

    public double getStartLat() {
        return startLat;
    }

    public double getEndLat() {
        return endLat;
    }

    public double getEndLong() {
        return endLong;
    }

    public int getPrice() {
        return price;
    }
}