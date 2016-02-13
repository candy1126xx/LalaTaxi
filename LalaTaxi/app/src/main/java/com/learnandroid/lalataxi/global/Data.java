package com.learnandroid.lalataxi.global;

import android.app.Application;

import org.apache.mina.core.session.IoSession;

public class Data extends Application{
    private String phone;
    private IoSession session;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    private String city;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public IoSession getSession() {
        return session;
    }

    public void setSession(IoSession session) {
        this.session = session;
    }
}
