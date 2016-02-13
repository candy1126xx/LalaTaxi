package com.texiservlet.main;


import com.learnandroid.lalataxi.bean.*;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        startLoginConnection();
    }

    private static void startLoginConnection() {
        SocketAcceptor acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
        acceptor.setHandler(new MyIoHandlerAdapter());
        try {
            acceptor.bind(new InetSocketAddress(1234));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HashMap<String, String> map = new HashMap<>();
    private static ArrayList<IoSession> customs = new ArrayList<>();
    private static ArrayList<IoSession> drivers = new ArrayList<>();
    private static ArrayList<LocationBean> driverLocation = new ArrayList<>();
    private static ArrayList<CustomBean> customOrder = new ArrayList<>();

    private static class MyIoHandlerAdapter extends IoHandlerAdapter {
        @Override
        public void messageReceived(IoSession session, Object message) throws Exception {
            //获取验证码
            if (message.getClass() == GetCodeBean.class) {
                Random random = new Random();
                int i = random.nextInt(9999) % (9999 - 1000 + 1) + 1000;
                session.write(new CodeBean(String.valueOf(i)));
                map.put(((GetCodeBean) message).getPhone(), String.valueOf(i));
                return;
            }
            //登录
            if (message.getClass() == LoginBean.class) {
                LoginBean bean = (LoginBean) message;
                if (map.get(bean.getPhone()).equals(bean.getCode())) {
                    session.write(new LoginResultBean(true));
                    if (bean.isCustom()) {
                        if (customs.contains(session)) {
                            customs.remove(session);
                        }
                        customs.add(session);
                    } else {
                        if (drivers.contains(session)) {
                            drivers.remove(session);
                        }
                        drivers.add(session);
                    }
                }
                return;
            }
            //定位信息
            if (message.getClass() == LocationBean.class) {
                LocationBean bean = (LocationBean) message;
                for (IoSession custom : customs) {
                    custom.write(bean);
                }
                driverLocation.add(bean);
                return;
            }
            //乘客下单
            if (message.getClass() == CustomBean.class) {
                CustomBean bean = (CustomBean) message;
                for (IoSession driver : drivers) {
                    driver.write(bean);
                }
                customOrder.add(bean);
                return;
            }
            //司机接单
            if (message.getClass() == DriverBean.class) {
                DriverBean bean = (DriverBean) message;
                for (IoSession driver : drivers) {
                    driver.write(bean);
                }
                for (IoSession custom : customs) {
                    custom.write(bean);
                }
                return;
            }
            //乘客端读取driverLocation
            if ((int)message == 0){
                driverLocation.forEach(session::write);
                return;
            }
            //司机端读取customOrder
            if ((int)message == 1){
                customOrder.forEach(session::write);
            }
        }

        @Override
        public void sessionClosed(IoSession session) throws Exception {
            if (customs.contains(session)) {
                customs.remove(session);
            }
            if (drivers.contains(session)) {
                drivers.remove(session);
            }
        }
    }
}
