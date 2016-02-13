package com.learnandroid.laladriver.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.learnandroid.laladriver.R;
import com.learnandroid.laladriver.global.Data;
import com.learnandroid.laladriver.iohandler.MyIoHandler;
import com.learnandroid.laladriver.iohandler.ReCustom;
import com.learnandroid.lalataxi.bean.CustomBean;
import com.learnandroid.lalataxi.bean.DriverBean;

import org.apache.mina.core.session.IoSession;

import java.util.HashMap;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnGetRoutePlanResultListener, View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initGlobal();
        setContentView(R.layout.activity_map);
        initView();
    }

    //初始化全局变量
    private IoSession session;
    private String phoneDriver;
    private CustomBean custom;
    private void initGlobal() {
        SDKInitializer.initialize(getApplicationContext());
        phoneDriver = ((Data) getApplication()).getPhone();
        session = ((Data) getApplication()).getSession();
        Intent intent = getIntent();
        HashMap<String, Object> map = (HashMap<String, Object>) intent.getSerializableExtra("custom");
        String phoneCustom = (String) map.get("phoneCustom");
        String startName = (String) map.get("startName");
        double startLat = (double) map.get("startLat");
        double startLong = (double) map.get("startLong");
        String endName = (String) map.get("endName");
        double endLat = (double) map.get("endLat");
        double endLong = (double) map.get("endLong");
        int price = 0;
        custom =new CustomBean(phoneCustom,startName,startLat,startLong,endName,endLat,endLong,price);
    }

    //初始化界面
    private BaiduMap mBaiduMap;
    private RoutePlanSearch search;
    private Button btnReceive;
    private void initView() {
        MapView mapView = (MapView) findViewById(R.id.baidumap);
        btnReceive = (Button) findViewById(R.id.btn_receive);
        btnReceive.setOnClickListener(this);
        mBaiduMap = mapView.getMap();
        search = RoutePlanSearch.newInstance();
        search.setOnGetRoutePlanResultListener(this);
        PlanNode startNode = PlanNode.withLocation(new LatLng(custom.getStartLat(), custom.getStartLong()));
        PlanNode endNode = PlanNode.withLocation(new LatLng(custom.getEndLat(), custom.getEndLong()));
        TransitRoutePlanOption option = new TransitRoutePlanOption();
        option.city(((Data) getApplication()).getCity());
        option.from(startNode);
        option.to(endNode);
        search.transitSearch(option);
    }

    //监听路线搜索结果的接口
    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
        List<TransitRouteLine> lines = transitRouteResult.getRouteLines();
        if (lines == null) {
            Toast.makeText(MapActivity.this, "没有合适的线路", Toast.LENGTH_SHORT).show();
        } else {
            TransitRouteLine line = lines.get(0);
            TransitRouteOverlay transitRoute = new TransitRouteOverlay(mBaiduMap);
            transitRoute.setData(line);
            transitRoute.addToMap();
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(
                    new LatLng(custom.getStartLat(), custom.getStartLong()));
            mBaiduMap.animateMapStatus(update);
        }
    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

    }

    //监听按钮点击事件的接口
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_receive:
                new Thread() {
                    @Override
                    public void run() {
                        session.write(new DriverBean(custom.getPhoneCustom(), phoneDriver));
                    }
                }.start();
                AlertDialog dialog = new AlertDialog.Builder(MapActivity.this)
                        .setCancelable(false)
                        .setMessage("乘客手机号是：\n" + custom.getPhoneCustom()+"\n" +
                                "是否要拨打电话？")
                        .setPositiveButton("打电话", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_CALL);
                                intent.setData(Uri.parse("tel:" + custom.getPhoneCustom()));
                                if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    Toast.makeText(MapActivity.this, "用户未启用拨打电话权限", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                startActivity(intent);
                            }
                        }).setNegativeButton("不打电话", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                btnReceive.setText("订单已交易");
                                btnReceive.setClickable(false);
                            }
                        }).create();
                dialog.show();
                break;
        }
    }
}
