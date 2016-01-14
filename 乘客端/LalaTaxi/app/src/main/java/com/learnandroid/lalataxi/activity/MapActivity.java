package com.learnandroid.lalataxi.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.learnandroid.lalataxi.IoHandler.MyIoHandler;
import com.learnandroid.lalataxi.IoHandler.ReLocation;
import com.learnandroid.lalataxi.R;
import com.learnandroid.lalataxi.bean.CustomBean;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        Data data = (Data) getApplication();
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
    private EditText etStart;
    private EditText etEnd;
    private TextView tvState;
    private Button btnSearch;
    private Button btnOrder;

    private void initView() {
        tvCity = (TextView) findViewById(R.id.tv_city);
        etStart = (EditText) findViewById(R.id.et_start);
        etEnd = (EditText) findViewById(R.id.et_end);
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

    private String startPlace;
    private String endPlace;
    private int price;
    private boolean isScan = false;
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_search:
                String city = tvCity.getText().toString();
                startPlace = etStart.getText().toString();
                endPlace = etEnd.getText().toString();
                PlanNode startNode = null;
                if (endPlace.equals("") || startPlace.equals("")) {
                    Toast.makeText(MapActivity.this, "请输入完整线路信息", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (startPlace.equals("我的位置")){
                    startPlace = myLocation.getDistrict()+myLocation.getStreet()+myLocation.getStreetNumber();
                    startNode = PlanNode.withLocation(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
                }else {
                    startPlace = etStart.getText().toString();
                    startNode = PlanNode.withCityNameAndPlaceName(city,startPlace);
                }
                PlanNode endNode = PlanNode.withCityNameAndPlaceName(city, endPlace);
                DrivingRoutePlanOption option = new DrivingRoutePlanOption();
                option.from(startNode);
                option.to(endNode);
                search.drivingSearch(option);
                break;
            case R.id.btn_order:
                if (!isScan) {
                    Toast.makeText(MapActivity.this, "请先查看路线", Toast.LENGTH_SHORT).show();
                    return;
                }
                new Thread() {
                    @Override
                    public void run() {
                        session.write(new CustomBean(phone, startPlace, endPlace, price));
                    }
                }.start();
                tvState.setText("等待司机接单，请稍候......");
                break;
        }
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
        List<DrivingRouteLine> lines = drivingRouteResult.getRouteLines();
        if (lines == null) {
            Toast.makeText(MapActivity.this, "没有合适的线路", Toast.LENGTH_SHORT).show();
        }else {
            DrivingRouteLine line = lines.get(0);
            DrivingRouteOverlay drivingRoute = new DrivingRouteOverlay(baiduMap);
            drivingRoute.setData(line);
            drivingRoute.addToMap();
            price = line.getDistance()/20;
            tvState.setText("路线规划成功！大约需要" + price + "元");
            isScan = true;
        }
    }

    private BDLocation myLocation = null;
    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation location) {
            myLocation = location;
            tvCity.setText(location.getCity());
            etStart.setText("我的位置");
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(
                    new LatLng(location.getLatitude(), location.getLongitude()));
            baiduMap.animateMapStatus(update);
            new Thread() {
                @Override
                public void run() {
                    session.write(new LocationBean(true, phone, location.getLongitude(), location.getLatitude()));
                }
            }.start();
        }
    }

    private final Handler handlerLocationBean = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            LocationBean bean = (LocationBean) msg.getData().getSerializable("locationCar");
            assert bean != null;
            MarkerOptions marker = new MarkerOptions()
                    .icon(car)
                    .position(new LatLng(bean.getLatitude(), bean.getLongitude()));
            boolean isExists = false;
            for (HashMap<String, Object> map : drivers) {
                if (map.get("phone").equals(bean.getPhone())) {
                    isExists = true;
                    ((MarkerOptions) map.get("marker")).position(new LatLng(bean.getLatitude(), bean.getLongitude()));
                    map.put("marker", marker);
                }
            }
            if (!isExists) {
                baiduMap.addOverlay(marker);
                HashMap<String, Object> newMap = new HashMap<>();
                newMap.put("phone", bean.getPhone());
                newMap.put("marker", marker);
                drivers.add(newMap);
            }
        }
    };
}
