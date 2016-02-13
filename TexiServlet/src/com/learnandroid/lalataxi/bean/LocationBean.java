package com.learnandroid.lalataxi.bean;

import java.io.Serializable;

public class LocationBean implements Serializable {
    private String phoneDriver;
    private double longitude;
    private double latitude;

    public LocationBean(String phoneDriver, double longitude, double latitude) {
        this.phoneDriver = phoneDriver;
        this.longitude = longitude;
        this.latitude = latitude;
    }


    public String getPhoneDriver() {
        return phoneDriver;
    }

    public double getLongitude() {
        return longitude;
    }


    public double getLatitude() {
        return latitude;
    }
}
