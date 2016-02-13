package com.learnandroid.lalataxi.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.learnandroid.lalataxi.IoHandler.MyIoHandler;
import com.learnandroid.lalataxi.IoHandler.ReDriver;
import com.learnandroid.lalataxi.IoHandler.ReLocation;
import com.learnandroid.lalataxi.R;
import com.learnandroid.lalataxi.bean.CustomBean;
import com.learnandroid.lalataxi.bean.DriverBean;
import com.learnandroid.lalataxi.bean.LocationBean;
import com.learnandroid.lalataxi.global.Data;

import org.apache.mina.core.session.IoSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapActivity extends AppCompatActivity implements View.OnClickListener, OnGetRoutePlanResultListener {
    private MapView mMapView = null;
    private BaiduMap baiduMap = null;
    private LocationClient mLocationClient = null;
    private BDLocationListener myListener = new MyLocationListener();
    private IoSession session;
    private String phone;
    private BitmapDescriptor car = null;
    private static ArrayList<HashMap<String, Object>> drivers = new ArrayList<>();
    private RoutePlanSearch search = null;
    private Data data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        data = (Data) getApplication();
        session = data.getSession();
        phone = data.getPhone();
        car = BitmapDescriptorFactory.fromResource(R.drawable.car);
        search = RoutePlanSearch.newInstance();
        search.setOnGetRoutePlanResultListener(this);

        MyIoHandler ioHandler = (MyIoHandler) session.getHandler();
        ioHandler.setReLocationListener(new ReLocation() {
            @Override
            public void showDriver(LocationBean location) {
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putSerializable("locationCar", location);
                message.setData(bundle);
                handlerLocationBean.sendMessage(message);
            }
        });
        ioHandler.setReDriverListener(new ReDriver() {
            @Override
            public void showDialog(final DriverBean driver) {
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
                session.write(0);
            }
        }.start();

        setContentView(R.layout.activity_map);
        initView();
        mMapView = (MapView) findViewById(R.id.bmapView);
        baiduMap = mMapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        MapStatus mapStatus = new MapStatus.Builder().zoom(16).build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        baiduMap.setMapStatus(mapStatusUpdate);
        mLocationClient = new LocationClient(getApplicationContext());
        initLocation();
        mLocationClient.registerLocationListener(myListener);
        mLocationClient.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    private TextView tvCity;
    private Button btnStart;
    private Button btnEnd;
    private TextView tvState;
    private Button btnSearch;
    private Button btnOrder;
    private void initView() {
        tvCity = (TextView) findViewById(R.id.tv_city);
        btnStart = (Button) findViewById(R.id.btn_start);
        btnStart.setOnClickListener(this);
        btnEnd = (Button) findViewById(R.id.btn_end);
        btnEnd.setOnClickListener(this);
        tvState = (TextView) findViewById(R.id.tv_state);
        tvState.setText("空闲");
        btnSearch = (Button) findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(this);
        btnOrder = (Button) findViewById(R.id.btn_order);
        btnOrder.setOnClickListener(this);
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        option.setScanSpan(0);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    private int price;
    private boolean isScan = false;
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                Intent intentStart = new Intent(MapActivity.this, SearcherActivity.class);
                intentStart.putExtra("isStartPort", true);
                startActivityForResult(intentStart, 0);
                break;
            case R.id.btn_end:
                Intent intentEnd = new Intent(MapActivity.this, SearcherActivity.class);
                intentEnd.putExtra("isStartPort", false);
                startActivityForResult(intentEnd, 1);
                break;
            case R.id.btn_search:
                if (startLocation == null || endLocation == null) {
                    Toast.makeText(MapActivity.this, "请输入完整线路信息", Toast.LENGTH_SHORT).show();
                    return;
                }
                PlanNode startNode = PlanNode.withLocation(startLocation);
                PlanNode endNode = PlanNode.withLocation(endLocation);
                TransitRoutePlanOption option = new TransitRoutePlanOption();
                option.city(tvCity.getText().toString());
                option.from(startNode);
                option.to(endNode);
                search.transitSearch(option);
                break;
            case R.id.btn_order:
                if (!isScan) {
                    Toast.makeText(MapActivity.this, "请先查看路线", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (startName == null){
                    startName = startAddress;
                }
                new Thread() {
                    @Override
                    public void run() {
                        session.write(new CustomBean(phone, startName, startLocation.latitude, startLocation.longitude,
                                endName, endLocation.latitude, endLocation.longitude, price));
                    }
                }.start();
                tvState.setText("等待司机接单，请稍候......");
                break;
        }
    }

    //监听路线规划结果的接口
    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
        List<TransitRouteLine> lines = transitRouteResult.getRouteLines();
        if (lines == null) {
            Toast.makeText(MapActivity.this, "没有合适的线路", Toast.LENGTH_SHORT).show();
        }else {
            TransitRouteLine line = lines.get(0);
            TransitRouteOverlay transitRoute = new TransitRouteOverlay(baiduMap);
            transitRoute.setData(line);
            transitRoute.addToMap();
            price = (int)(line.getDistance()*0.01f);
            tvState.setText("路线规划成功！大约需要" + price + "元");
            isScan = true;
        }
    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
    }

    //接收到自己定位信息的处理函数
    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation location) {
            tvCity.setText(location.getCity());
            data.setCity(location.getCity());
            btnStart.setText("我的位置");
            startAddress = location.getAddrStr();
            startLocation = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(
                    new LatLng(location.getLatitude(), location.getLongitude()));
            baiduMap.animateMapStatus(update);
        }
    }

    //接收到司机端定位信息的Handler
    private HandlerLocationBean handlerLocationBean = new HandlerLocationBean();
    private class HandlerLocationBean extends Handler {
        @Override
        public void handleMessage(Message msg) {
            LocationBean bean = (LocationBean) msg.getData().getSerializable("locationCar");
            assert bean != null;
            MarkerOptions marker = new MarkerOptions()
                    .icon(car)
                    .position(new LatLng(bean.getLatitude(), bean.getLongitude()));
            boolean isExists = false;
            for (HashMap<String, Object> map : drivers) {
                if (map.get("phone").equals(bean.getPhoneDriver())) {
                    isExists = true;
                    ((MarkerOptions) map.get("marker")).position(new LatLng(bean.getLatitude(), bean.getLongitude()));
                    map.put("marker", marker);
                }
            }
            if (!isExists) {
                baiduMap.addOverlay(marker);
                HashMap<String, Object> newMap = new HashMap<>();
                newMap.put("phone", bean.getPhoneDriver());
                newMap.put("marker", marker);
                drivers.add(newMap);
            }
        }
    }

    //接收到订单被抢的信息的Handler
    private HandlerDriverBean handlerDriverBean = new HandlerDriverBean();
    private class HandlerDriverBean extends Handler{
        @Override
        public void handleMessage(Message msg) {
            final DriverBean bean = (DriverBean)msg.getData().getSerializable("driver");
            assert bean != null;
            if (!bean.getPhoneCustom().equals(phone)){
                return;
            }
            tvState.setText("订单已交易！");
            AlertDialog dialog = new AlertDialog.Builder(MapActivity.this)
                    .setCancelable(false)
                    .setMessage("接单司机手机号是：\n" + bean.getPhoneDriver()+"\n" +
                            "是否拨打电话？")
                    .setPositiveButton("打电话", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_CALL);
                            intent.setData(Uri.parse("tel:" + bean.getPhoneDriver()));
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
                            btnOrder.setClickable(false);
                        }
                    }).create();
            dialog.show();
        }
    }

    //从SearcherActivity返回结果的处理函数
    private String startName;
    private String startAddress;
    private LatLng startLocation;
    private String endName;
    private String endAddress;
    private LatLng endLocation;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        HashMap<String, Object> map = (HashMap<String, Object>) data.getSerializableExtra("result");
        if (requestCode == 0){
            startName = (String) map.get("name");
            startAddress = (String) map.get("address");
            startLocation = (LatLng) map.get("location");
            btnStart.setText(startName);
        }else if (requestCode == 1){
            endName = (String) map.get("name");
            endAddress = (String) map.get("address");
            endLocation = (LatLng) map.get("location");
            btnEnd.setText(endName);
        }
    }
}
