package com.example.location;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.baidu.location.LocationClient;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.example.location.Adapter.Adapter_myview;
import com.example.location.Configure.Configure_location;
import com.example.location.Listener.MyLocationListener;


import java.util.ArrayList;

public class MainActivity extends GuideActivity   {
//    public LocationClient mLocationClient = null;
//    private MyLocationListener myListener = new MyLocationListener(baiduMap);
//    Configure_location configure_location;





//        @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        switch (keyCode){
//            case KeyEvent.KEYCODE_1://罗盘态：显示定位方向圈，保持定位图标在地图中心
//                configure_location.setMyLocationConfigeration(MyLocationConfiguration.LocationMode.COMPASS);
//                break;
//            case KeyEvent.KEYCODE_2://跟随态：保持定位图标在地图中心
//               configure_location.setMyLocationConfigeration(MyLocationConfiguration.LocationMode.FOLLOWING);
//                break;
//            case KeyEvent.KEYCODE_3://普通态：更新定位数据时不对地图做任何操作
//                configure_location.setMyLocationConfigeration(MyLocationConfiguration.LocationMode.NORMAL);
//                break;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

//    @Override
//    public void regester_broadcast(){
//        IntentFilter intentFilter=new IntentFilter();
//        intentFilter.addAction("com.example.action.receivedata");
//        Myreceiver myreceiver=new Myreceiver();
//        registerReceiver(myreceiver,intentFilter);
//    }

    @Override
    protected void onDestroy() {
   //     mLocationClient.stop();
        super.onDestroy();
    }

    public void init() {
//        mLocationClient = new LocationClient(getApplicationContext());//声明LocationClient类
//        mLocationClient.registerLocationListener(myListener);//注册监听函数
//        configure_location=new Configure_location(baiduMap,mLocationClient);
////        initLocation();
////        baiduMap.setMyLocationEnabled(true);//开启定位图层
////        MyLocationConfiguration.LocationMode mode = MyLocationConfiguration.LocationMode.COMPASS;
////        setMyLocationConfigeration(mode);
//        mLocationClient.start();


    }
}
