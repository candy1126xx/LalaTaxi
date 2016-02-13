package com.learnandroid.laladriver.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.learnandroid.laladriver.R;
import com.learnandroid.laladriver.global.Data;
import com.learnandroid.laladriver.iohandler.MyIoHandler;
import com.learnandroid.laladriver.iohandler.ReCustom;
import com.learnandroid.laladriver.iohandler.ReDriver;
import com.learnandroid.lalataxi.bean.CustomBean;
import com.learnandroid.lalataxi.bean.DriverBean;

import org.apache.mina.core.session.IoSession;

import java.util.ArrayList;
import java.util.HashMap;

public class OrderActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static ArrayList<HashMap<String, Object>> listOrder = new ArrayList<>();
    private ListView lvOrder;
    private SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initGlobal();
        setContentView(R.layout.activity_order);

        lvOrder = (ListView) findViewById(R.id.lv_order);
        lvOrder.setOnItemClickListener(this);
        adapter = new SimpleAdapter(OrderActivity.this, listOrder, R.layout.list_item,
                new String[]{"startName", "endName", "price"},
                new int[]{R.id.tv_start, R.id.tv_end, R.id.tv_price});
        lvOrder.setAdapter(adapter);
    }

    //初始化全局变量
    private IoSession session;
    private String phoneDriver;
    private void initGlobal(){
        phoneDriver = ((Data)getApplication()).getPhone();
        session = ((Data)getApplication()).getSession();
        MyIoHandler ioHandler = (MyIoHandler)session.getHandler();
        ioHandler.setReCustomListener(new ReCustom() {
            @Override
            public void showCustom(CustomBean custom) {
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putSerializable("custom", custom);
                message.setData(bundle);
                handlerCustomBean.sendMessage(message);
            }
        });
        ioHandler.setReDriverListener(new ReDriver() {
            @Override
            public void deleteCustom(DriverBean driver) {
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putSerializable("driver", driver);
                message.setData(bundle);
                handlerDriverBean.sendMessage(message);
            }
        });
        new Thread(){
            @Override
            public void run() {
                session.write(1);
            }
        }.start();
    }

    //处理接收到的订单信息的Handler
    private HandlerCustomBean handlerCustomBean = new HandlerCustomBean();
    private class HandlerCustomBean extends Handler {
        @Override
        public void handleMessage(Message msg) {
            CustomBean custom = (CustomBean)msg.getData().getSerializable("custom");
            HashMap<String, Object> map = new HashMap<>();
            assert custom != null;
            map.put("phoneCustom", custom.getPhoneCustom());
            map.put("startName", "起点："+custom.getStartName());
            map.put("startLat", custom.getStartLat());
            map.put("startLong", custom.getStartLong());
            map.put("endName", "终点："+custom.getEndName());
            map.put("endLat", custom.getEndLat());
            map.put("endLong", custom.getEndLong());
            map.put("price", "￥"+custom.getPrice());
            listOrder.add(map);
            adapter.notifyDataSetChanged();
        }
    }

    //处理接收到的订单被抢信息的Handler
    private HandlerDriverBean handlerDriverBean = new HandlerDriverBean();
    private class HandlerDriverBean extends Handler {
        @Override
        public void handleMessage(Message msg) {
            DriverBean driver = (DriverBean)msg.getData().getSerializable("driver");
            for (HashMap<String, Object> map : listOrder){
                assert driver != null;
                if (map.get("phoneCustom").equals(driver.getPhoneCustom())){
                    listOrder.remove(map);
                }
            }
            adapter.notifyDataSetChanged();
        }
    }
    //点击列表项的处理函数
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(OrderActivity.this, MapActivity.class);
        intent.putExtra("custom", (HashMap<String, Object>)parent.getItemAtPosition(position));
        startActivity(intent);
    }
}
