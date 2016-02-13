package com.learnandroid.laladriver.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.learnandroid.laladriver.global.Data;
import com.learnandroid.laladriver.iohandler.MyIoHandler;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;

public class SocketService extends Service {
    public SocketService() {
    }

    private InetSocketAddress remoteAddress = new InetSocketAddress("192.168.1.101",1234);
    @Override
    public void onCreate() {
        new Thread(){
            @Override
            public void run() {
                NioSocketConnector connector = new NioSocketConnector();
                connector.setConnectTimeoutMillis(3000);
                connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
                connector.setHandler(new MyIoHandler());

                ConnectFuture future = connector.connect(remoteAddress);
                future.awaitUninterruptibly();
                ((Data)getApplication()).setSession(future.getSession());
            }
        }.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}