package com.example.location;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;

public  abstract class BaseActivity extends Activity {
    private static final String TAG = "BaseActivity";
    protected LatLng hmPos = new LatLng(40.050513, 116.30361);
    protected MapView mMapView;
    protected BaiduMap baiduMap;

    //这里加final是为了不让子类覆盖，原因是为了预防这里的一些类还没初始化的时候就被子类调用
    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.mapView);
        baiduMap = mMapView.getMap();//获取地图控制器
        //1、隐藏缩放按钮，比例尺
//        mMapView.showScaleControl(false);//隐藏比例按钮，默认是显示的
//        mMapView.showZoomControls(false);//隐藏缩放按钮，默认是显示的
        //2、获取最小（3）、最大缩放级别（22）
        float maxZoomLevel = baiduMap.getMaxZoomLevel();//获取地图最大缩放级别
        float minZoomLevel = baiduMap.getMinZoomLevel();//获取地图最小缩放级别
        Log.e(TAG,"minZoomLevel ="+minZoomLevel+",maxZoomLevel="+maxZoomLevel);//最小为3，最大为22
        //3、设置地图中心为
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(hmPos);
        baiduMap.setMapStatus(mapStatusUpdate);
        //4、设置地图缩放为20
        mapStatusUpdate = MapStatusUpdateFactory.zoomTo(20);
        baiduMap.setMapStatus(mapStatusUpdate);
        //6.获取地图Ui控制器：隐藏指南针
//        UiSettings uiSettings = baiduMap.getUiSettings();
//        uiSettings.setCompassEnabled(false);//不显示指南针
        init();
    }

    /**
     * 子类实现此方法
     */
    public abstract void init();

    /**
     * 在屏幕中央显示一个Toast
     * @param text
     */
    public void showToast(CharSequence text){
       // Utils.showToast(this,text);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

}
