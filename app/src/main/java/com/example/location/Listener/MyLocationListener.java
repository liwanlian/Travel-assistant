package com.example.location.Listener;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;


import androidx.annotation.NonNull;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MyLocationData;
import com.example.location.Configure.GetWeather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.UnsupportedEncodingException;
import java.util.List;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyLocationListener extends  BDAbstractLocationListener  {
    private static final String TAG = "LocationActivity";
    private BaiduMap baiduMap;

//    String msg_location;
//    String addr;
//    TextView t1;
//    TextView t2;
//    TextView tv_weather;


    Context context;
    public MyLocationListener(BaiduMap baiduMap) {
        this.baiduMap=baiduMap;
    }

    @Override
    public void onReceiveLocation(BDLocation location) {
        if (location != null) {
            MyLocationData.Builder builder = new MyLocationData.Builder();
            builder.accuracy(location.getRadius());//设置精度
            builder.direction(location.getDirection());//设置方向
            builder.latitude(location.getLatitude());//设置纬度
            builder.longitude(location.getLongitude());//设置经度
            MyLocationData locationData = builder.build();
           baiduMap.setMyLocationData(locationData);//把定位数据显示到地图上
        }
        //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
        //以下只列举部分获取经纬度相关（常用）的结果信息
        //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

        double latitude = location.getLatitude();    //获取纬度信息
        double longitude = location.getLongitude();    //获取经度信息
        float radius = location.getRadius();    //获取定位精度，默认值为0.0f

        String coorType = location.getCoorType();
        //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准

        int errorCode = location.getLocType();
        //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
        Log.e(TAG, "经度=" + longitude + ",纬度=" + latitude + ",错误码=" + errorCode);

        //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
        //以下只列举部分获取地址相关的结果信息
        //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

       String  addr = location.getAddrStr();    //获取详细地址信息
        String country = location.getCountry();    //获取国家
        String province = location.getProvince();    //获取省份
         String city = location.getCity();    //获取城市
        String district = location.getDistrict();    //获取区县
        String street = location.getStreet();    //获取街道信息
        Log.e(TAG, "addr:" + addr + ",country:" + country
                + ",province:" + province + ",city:" + city
                + ",district:" + district + ",street:" + street);

        String locationDescribe = location.getLocationDescribe();    //获取位置描述信息
        Log.e(TAG, "位置描述：" + locationDescribe);
//
//        t1.setText(addr);
//        msg_location=addr;
//        t2.setText(locationDescribe);

       String msg=city.substring(0,city.length()-1);
     //  tv_weather.setText(msg);

       // getWeatherDatafromNet("广州");




        System.out.println("hh=" + locationDescribe);
        List<Poi> poiList = location.getPoiList();
        //获取周边POI信息
        //POI信息包括POI ID、名称等，具体信息请参照类参考中POI类的相关说明
        for (int i = 0; i < poiList.size(); i++) {
            Log.e(TAG, "兴趣点：" + poiList.get(i).getName() + "\n");
        }
    }




}

