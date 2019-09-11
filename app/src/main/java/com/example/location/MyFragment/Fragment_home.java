package com.example.location.MyFragment;


import android.os.Bundle;
import android.os.Handler;

import android.os.Message;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;

import com.baidu.location.Poi;
import com.baidu.mapapi.map.BaiduMap;

import com.baidu.mapapi.map.MapBaseIndoorMapInfo;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;

import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.example.location.Configure.Configure_location;

import com.example.location.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


//首页的fragment
public class Fragment_home extends Fragment  {
    private View rootView=null;
    private static final String TAG="location";

    //首页的控件
    TextView tv_location;
    TextView tv_weather;
    BaiduMap baiduMap;
    TextureMapView mMapView;
    TextView tv_nearby;

    Button bt_normal,bt_reli,bt_weixin,bt_trafffic;

    protected LatLng hmPos = new LatLng(29.563757, 106.466343);
    protected  LatLng ll;

    private boolean mIsInit = false;//数据是否加载完成
    private boolean mIsPrepared = false;//UI是否准备完成

    private LocationClient mLocationClient = null;
    private MLocationListener myListener ;
    private  Configure_location configure_location;

    //解析回调回来的天气数据
    //第一层
    JSONObject object_data;

    //第二层
    JSONArray array_forecast;

    //第三层
    JSONObject object_weatherdata;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView=inflater.inflate(R.layout.page_home, container, false);
        mIsPrepared = true;
        lazyLoad();
        return rootView;
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            lazyLoad();
        }
    }
    public void lazyLoad() {
        if (getUserVisibleHint() && mIsPrepared && !mIsInit) {
            // 异步初始化，在初始化后显示正常UI
            loadData();
        }
    }
    public void loadData() {
        new Thread() {
            public void run() {
                // 1. 加载数据
                // 2. 更新UI
                // 3. mIsInit = true
               tv_location=(TextView)rootView.findViewById(R.id.tv_location);
               tv_nearby=(TextView)rootView.findViewById(R.id.tv_nearby);
               tv_weather=(TextView)rootView.findViewById(R.id.tv_weather);
                mMapView = (TextureMapView)rootView.findViewById(R.id.mapView);


                bt_normal=(Button)rootView.findViewById(R.id.bt_normal);
                bt_reli=(Button)rootView.findViewById(R.id.bt_reli);
                bt_weixin=(Button)rootView.findViewById(R.id.bt_weixin);
                bt_trafffic=(Button)rootView.findViewById(R.id.bt_traffic);

                bt_function();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message message=Message.obtain();
                        message.obj="1";
                        hh.sendMessage(message);
                    }
                }).start();

                mIsInit = true;
            }
        }.start();
    }
    private void bt_function(){
        bt_normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);//普通
                baiduMap.setIndoorEnable(false);
                baiduMap.setBaiduHeatMapEnabled(false);
                baiduMap.setTrafficEnabled(false);
            }
        });

        bt_reli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                baiduMap.setBaiduHeatMapEnabled(true);
                baiduMap.setIndoorEnable(false);
                baiduMap.setTrafficEnabled(false);
            }
        });//热力图
        bt_weixin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               baiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                baiduMap.setBaiduHeatMapEnabled(false);
                baiduMap.setIndoorEnable(false);
                baiduMap.setTrafficEnabled(false);
            }
        });//卫星
        bt_trafffic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                baiduMap.setTrafficEnabled(true);
                baiduMap.setBaiduHeatMapEnabled(false);
                baiduMap.setIndoorEnable(false);
            }
        });//交通图

    }
Handler hh=new Handler(){
    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        baiduMap = mMapView.getMap();//获取地图控制器
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(hmPos);
        baiduMap.setMapStatus(mapStatusUpdate);
        //设置地图缩放为20
        mapStatusUpdate = MapStatusUpdateFactory.zoomTo(20);
        baiduMap.setMapStatus(mapStatusUpdate);

        myListener=new MLocationListener();
        mLocationClient = new LocationClient(getActivity().getApplicationContext());//声明LocationClient类
        mLocationClient.registerLocationListener(myListener);//注册监听函数
        configure_location=new Configure_location(baiduMap,mLocationClient);
        configure_location.initLocation();
        baiduMap.setMyLocationEnabled(true);//开启定位图层
        MyLocationConfiguration.LocationMode mode = MyLocationConfiguration.LocationMode.COMPASS;
        configure_location.setMyLocationConfigeration(mode);
        mLocationClient.start();
    }
};


    public class   MLocationListener extends  BDAbstractLocationListener{
      @Override
      public void onReceiveLocation(BDLocation bdLocation) {
          if (bdLocation != null) {
              MyLocationData.Builder builder = new MyLocationData.Builder();
              builder.accuracy(bdLocation.getRadius());//设置精度
              builder.direction(bdLocation.getDirection());//设置方向
              builder.latitude(bdLocation.getLatitude());//设置纬度
              builder.longitude(bdLocation.getLongitude());//设置经度
              MyLocationData locationData = builder.build();
              baiduMap.setMyLocationData(locationData);//把定位数据显示到地图上
          }
          //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
          //以下只列举部分获取经纬度相关（常用）的结果信息
          //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

          double latitude = bdLocation.getLatitude();    //获取纬度信息
          double longitude = bdLocation.getLongitude();    //获取经度信息
          float radius = bdLocation.getRadius();    //获取定位精度，默认值为0.0f

          String coorType = bdLocation.getCoorType();
          //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准

          int errorCode = bdLocation.getLocType();
          ll=new LatLng(latitude,longitude);
          //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
          Log.e(TAG, "经度=" + longitude + ",纬度=" + latitude + ",错误码=" + errorCode);

          //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
          //以下只列举部分获取地址相关的结果信息
          //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

         String addr = bdLocation.getAddrStr();    //获取详细地址信息
          String country = bdLocation.getCountry();    //获取国家
          String province = bdLocation.getProvince();    //获取省份
          String city = bdLocation.getCity();    //获取城市
          String district = bdLocation.getDistrict();    //获取区县
          String street = bdLocation.getStreet();    //获取街道信息
          String locationDescribe = bdLocation.getLocationDescribe();    //获取位置描述信息
          //Log.e(TAG, "位置描述：" + locationDescribe);
          tv_location.setText(addr);
          tv_nearby.setText(locationDescribe);


          String msg=city.substring(0,city.length()-1);
          getWeatherDatafromNet(msg);

          List<Poi> poiList = bdLocation.getPoiList();
          //获取周边POI信息
          //POI信息包括POI ID、名称等，具体信息请参照类参考中POI类的相关说明
          for (int i = 0; i < poiList.size(); i++) {
              Log.e(TAG, "兴趣点：" + poiList.get(i).getName() + "\n");
          }
      }

  }
    private void getWeatherDatafromNet(String cityCode)
    {
        //  final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey="+cityCode;
        final String address="http://wthrcdn.etouch.cn/weather_mini?city="+cityCode;
        Log.d("Address:",address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    java.net.URL url = new URL(address);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(8000);
                    urlConnection.setReadTimeout(8000);
                    InputStream in = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuffer sb = new StringBuffer();
                    String str;
                    while((str=reader.readLine())!=null)
                    {
                        sb.append(str);
                        Log.d("date from url",str);
                    }
                    String response = sb.toString();
                    Message msg=new Message();
                    msg.obj=response;
                    handler.sendMessage(msg);
                    Log.d("response",response);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    Handler handler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            String result=(String)msg.obj;
            tv_weather.setText(result);

            System.out.println("llll="+result);
            parsejsondata_object(result);
        }
    };

    public void parsejsondata_object(String jsondata){
        System.out.println("oooo=");
        JSONObject jsonObject= null;
        try {
            jsonObject = new JSONObject(jsondata);
            object_data=jsonObject.getJSONObject("data");
            parsejsonarray_forecast(object_data);
            System.out.println("object_data="+object_data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void parsejsonarray_forecast(JSONObject jsonObject){
        try {
            array_forecast=jsonObject.getJSONArray("forecast");
            System.out.println("arr_forecast"+array_forecast);
            getweatherdata(array_forecast);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void   getweatherdata(JSONArray jsonArray){
        try {
            object_weatherdata=jsonArray.getJSONObject(0);
            String high=object_weatherdata.getString("high");
            String low=object_weatherdata.getString("low");
            String fengli=object_weatherdata.getString("fengli");
            String fengxaing=object_weatherdata.getString("fengxiang");
            String type=object_weatherdata.getString("type");
            StringBuffer stringBuffer=new StringBuffer();
            stringBuffer.append("最低温度："+low.substring(2,low.length())+"\n").append("最高温度："+high.substring(2,high.length())+"\n").append("风力:"+fengli.substring(9,fengli.length()-3)+"\n").append(fengxaing+" "+type+"\n");
            System.out.println("高温"+high+"\n"
                    +"低温"+low+"\n"
                    +"风力"+fengli+"\n"
                    +"风向"+fengxaing+"\n"
                    +"类型"+type+"\n"
            );
            tv_weather.setText(stringBuffer);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    @Override
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
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }
}
