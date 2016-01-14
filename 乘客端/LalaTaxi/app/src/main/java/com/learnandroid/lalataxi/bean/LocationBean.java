package com.learnandroid.lalataxi.bean;

import java.io.Serializable;

public class LocationBean implements Serializable {
    private boolean isCustom;
    private String phone;
    private double longitude;
    private double latitude;

    public LocationBean(boolean isCustom, String phone, double longitude, double latitude) {
        this.isCustom = isCustom;
        this.phone = phone;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public String getPhone() {
        return phone;
    }

    public double getLongitude() {
        return longitude;
    }


    public double getLatitude() {
        return latitude;
    }
}
