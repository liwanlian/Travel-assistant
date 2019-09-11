package com.example.location.MyFragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
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
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapBaseIndoorMapInfo;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.navi.BaiduMapAppNotSupportNaviException;
import com.baidu.mapapi.navi.BaiduMapNavigation;
import com.baidu.mapapi.navi.NaviParaOption;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.MassTransitRouteOverlay;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteLine;
import com.baidu.mapapi.search.route.MassTransitRoutePlanOption;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.OpenClientUtil;
import com.baidu.mapapi.walknavi.WalkNavigateHelper;
import com.baidu.mapapi.walknavi.params.WalkNaviLaunchParam;
import com.example.location.Configure.Configure_location;
import com.example.location.R;

import java.util.ArrayList;
import java.util.List;

public class Fragment_guide extends Fragment implements OnGetGeoCoderResultListener {
    private View rootview;
    private boolean mIsInit = false;//数据是否加载完成
    private boolean mIsPrepared = false;//UI是否准备完成

    //导航界面的控件
    Switch sw_indoor;
    TextView guide_indoor;
    TextView  guide_walking;
    TextView guide_bikingone;
    TextView guide_bikingtwo;
    TextView guide_diive;
    TextView plan_mass;
    TextView plam_drive;
    TextureMapView guide_mapView;

    BaiduMap guide_baidumap;
    protected LatLng hmPos = new LatLng(40.050513, 116.30361);

    boolean indoor_onoff=false;//室内地图打开的标志  开true  关 false
    int flag_plan=0;//地图是否有换乘的记录
    int flag_guide=0;//地图是否有导航的记录
    //跨城规划的数据
    String startcity;
    String startplace;
    String endcity;
    String endplace;
    int flag_zhong=0;
    double lat;//经度
    double longttitude;//纬度
    LatLng lang1;
    LatLng lang2;
    int plag_choose=0;

    //搜索的部分
    private GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用 地理编码查询接口
    RoutePlanSearch routePlanSearch = null;    // 搜索模块，也可去掉地图模块独立使用
    //POi搜索
    private PoiSearch poiSearch;

    //导航
    String cityname;
    String gw_start;
    String gw_end;
    int flag_gw=0;
    WalkNaviLaunchParam  walkNaviLaunchParam;
    WalkNavigateHelper walkNavigateHelper;

    //定位的
    private LocationClient mLocationClient = null;
    private MlocationListenerg myListener ;
    private Configure_location configure_location;
    protected  String currentcity;
    protected String currentplace;
    LatLng lat_zhong;

    //室内地图
    LatLng centerpos = new LatLng(39.916958, 116.379278); // 西单大悦城


    View mainview;
    MapBaseIndoorMapInfo mMapBaseIndoorMapInfo = null;


    int nodeIndex = -1;
    private TextView popupText = null; // 泡泡view
    List<String> indoordata;
    ArrayAdapter indooradapter;
    int posi=0;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();//新建地理编码查询
        mSearch.setOnGetGeoCodeResultListener(this);//设置查询结果监听者

    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootview=inflater.inflate(R.layout.page_guide,container,false);
       //  mainview = inflater.inflate(R.layout.page_guide, null);

        mIsPrepared = true;
        lazyLoad();
        return rootview;
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

    private void loadData() {
        new Thread() {
            public void run() {
                sw_indoor=(Switch)rootview.findViewById(R.id.sw_indoor);
                guide_indoor=(TextView)rootview.findViewById(R.id.guide_indoor);
                guide_walking=(TextView)rootview.findViewById(R.id.guide_walking);
                guide_bikingone=(TextView)rootview.findViewById(R.id.guide_bikingone);
                guide_bikingtwo=(TextView)rootview.findViewById(R.id.guide_bikingtwo);


                plan_mass=(TextView)rootview.findViewById(R.id.plan_mass);
                plam_drive=(TextView)rootview.findViewById(R.id.plan_drive);
                guide_mapView=(TextureMapView)rootview.findViewById(R.id.guide_mapview);

                indoordata=new ArrayList<String>();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message message=Message.obtain();
                        message.obj="1";
                        hh.sendMessage(message);
                    }
                }).start();

                tv_function();

                sw_indoor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (b){
                            mLocationClient.stop();
                            MapStatus.Builder builder = new MapStatus.Builder();
                            builder.target(lat_zhong).zoom(19.0f);
                            guide_baidumap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                            guide_baidumap.setIndoorEnable(true);
                          //  RelativeLayout layout = new RelativeLayout(getActivity());

                        }
                        else{
                            mLocationClient.start();
                            guide_baidumap.setIndoorEnable(false);

                        }
                    }
                });
                guide_indoor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (guide_baidumap.isBaseIndoorMapMode()){
                            if (mMapBaseIndoorMapInfo.getFloors().size()==0){
                                Toast.makeText(getActivity().getApplicationContext(),"当前建筑物不支持展示室内地图",Toast.LENGTH_LONG).show();
                            }
                            else{
                                indooradapter=new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, indoordata);
                                final ListView lv = new ListView(getActivity());
                                  lv.setAdapter(indooradapter);
                                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                       posi=i;

                                    }
                                });
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("当前建筑物的楼层如下：");
                                builder.setView(lv);
                                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(final DialogInterface dialog, int which) {
                                         int ch;

                                        String floor = indoordata.get(posi);
                                     //   Toast.makeText(getActivity().getApplicationContext(),floor,Toast.LENGTH_LONG).show();
                                        guide_baidumap.switchBaseIndoorMapFloor(floor, mMapBaseIndoorMapInfo.getID());
                                        indooradapter.notifyDataSetInvalidated();
                                        Toast.makeText(getActivity().getApplicationContext(),"切换楼层成功",Toast.LENGTH_SHORT).show();

                                    }
                                });
                                builder.setNegativeButton("取消",null);
                                builder.create().show();
                            }

                        }
                        else{
                            Toast.makeText(getActivity().getApplicationContext(),"请打开室内图或将室内图移入屏幕内",Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        }.start();
    }

    private void indoor_function(){
        guide_baidumap.setOnBaseIndoorMapListener(new BaiduMap.OnBaseIndoorMapListener() {
            @Override
            public void onBaseIndoorMapMode(boolean b, MapBaseIndoorMapInfo mapBaseIndoorMapInfo) {
                if (b == false || mapBaseIndoorMapInfo == null) {

                    Toast.makeText(getActivity().getApplicationContext(),"当前位置查看不了室内地图",Toast.LENGTH_SHORT).show();
                    return;
                }

              StringBuffer sb=new StringBuffer();
                indoordata=new ArrayList<String>();
               for (int i=0;i<mapBaseIndoorMapInfo.getFloors().size();i++)
                  indoordata.add(mapBaseIndoorMapInfo.getFloors().get(i));

//                mFloorListAdapter.setmFloorList( mapBaseIndoorMapInfo.getFloors());
//                stripListView.setVisibility(View.VISIBLE);
//                stripListView.setAdapter(mFloorListAdapter);
                mMapBaseIndoorMapInfo = mapBaseIndoorMapInfo;
            }
        });

    }
    Handler hh=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            guide_baidumap = guide_mapView.getMap();//获取地图控制器
            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(hmPos);
            guide_baidumap.setMapStatus(mapStatusUpdate);
            //设置地图缩放为20
            mapStatusUpdate = MapStatusUpdateFactory.zoomTo(20);
            guide_baidumap.setMapStatus(mapStatusUpdate);

           myListener=new MlocationListenerg();
            mLocationClient = new LocationClient(getActivity().getApplicationContext());//声明LocationClient类
            mLocationClient.registerLocationListener(myListener);//注册监听函数
            configure_location=new Configure_location(guide_baidumap,mLocationClient);
            configure_location.initLocation();
            guide_baidumap.setMyLocationEnabled(true);//开启定位图层
            MyLocationConfiguration.LocationMode mode = MyLocationConfiguration.LocationMode.COMPASS;
            configure_location.setMyLocationConfigeration(mode);
            mLocationClient.start();
            indoor_function();
        }
    };


   public class MlocationListenerg extends  BDAbstractLocationListener{

       @Override
       public void onReceiveLocation(BDLocation bdLocation) {
           if (bdLocation != null) {
               MyLocationData.Builder builder = new MyLocationData.Builder();
               builder.accuracy(bdLocation.getRadius());//设置精度
               builder.direction(bdLocation.getDirection());//设置方向
               builder.latitude(bdLocation.getLatitude());//设置纬度
               builder.longitude(bdLocation.getLongitude());//设置经度
               MyLocationData locationData = builder.build();
               guide_baidumap.setMyLocationData(locationData);//把定位数据显示到地图上
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
//            ll=new LatLng(latitude,longitude);
//            //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
//            Log.e(TAG, "经度=" + longitude + ",纬度=" + latitude + ",错误码=" + errorCode);

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

            currentcity=city;
            currentplace=locationDescribe;
            lat_zhong=new LatLng(latitude,longitude);
           String msg=city.substring(0,city.length()-1);


           List<Poi> poiList = bdLocation.getPoiList();
           //获取周边POI信息
           //POI信息包括POI ID、名称等，具体信息请参照类参考中POI类的相关说明
           for (int i = 0; i < poiList.size(); i++) {
               //  Log.e(TAG, "兴趣点：" + poiList.get(i).getName() + "\n");
           }
       }
   }
    private void tv_function(){
        //跨城换乘
        plan_mass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sunmmy();
                plag_choose=1;
            }
        });
        plam_drive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sunmmy();
                plag_choose=2;
            }
        });
        //导航部分
        guide_walking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                walk_sum();
                flag_gw=1;
            }
        });
        guide_bikingone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                walk_sum();
                flag_gw=2;
            }
        });
        guide_bikingtwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View v1 = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.dialog_drive, null);
                final TextView et_startcity=(TextView)v1.findViewById(R.id.driveguide_city);
                final TextView et_startplace=(TextView)v1.findViewById(R.id.driveguide_start);
                final EditText et_endcity=(EditText)v1.findViewById(R.id.driveguide_endcity);
                final EditText et_endplace=(EditText)v1.findViewById(R.id.driveguide_endplace);
                et_startcity.setText("当前城市是在："+currentcity);
                et_startplace.setText("当前位置："+currentplace);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(v1);
                builder.setTitle("步行导航前，请输入一些相关信息");
                builder.setPositiveButton("确认",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cityname=et_endcity.getText().toString();

                        gw_end=et_endplace.getText().toString();
                        flag_gw=3;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                mSearch.geocode(new GeoCodeOption().city(
                                        cityname).address(gw_end));

                            }
                        }).start();

                    }
                })
                        .setNegativeButton("取消",null).show();
            }
        });
    }
    private void walk_sum(){
        final View v1 = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.dialog_walkingguide, null);
        final TextView et_startcity=(TextView)v1.findViewById(R.id.walkguide_city);
        final TextView et_startplace=(TextView)v1.findViewById(R.id.walkguide_start);
        final EditText et_endplace=(EditText)v1.findViewById(R.id.walkguide_end);
        et_startcity.setText("当前城市是在："+currentcity);
        et_startplace.setText("当前位置："+currentplace);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v1);
        builder.setTitle("步行导航前，请输入一些相关信息");
        builder.setPositiveButton("确认",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                cityname=et_startcity.getText().toString();
                gw_start=et_startplace.getText().toString();
                gw_end=et_endplace.getText().toString();

                walkNavigateHelper=  WalkNavigateHelper.getInstance();


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mSearch.geocode(new GeoCodeOption().city(
                                currentcity).address(gw_end));
                    }
                }).start();

            }
        })
                .setNegativeButton("取消",null).show();
    }
    private void guide_walk(LatLng lat1,LatLng lat2){
                LatLng pt1 = lat1;
                LatLng pt2 = lat2;

                // 构建 导航参数
                NaviParaOption para = new NaviParaOption()
                        .startPoint(lat_zhong).endPoint(pt2)
                        .startName(currentplace).endName(endplace);

                if (flag_gw==1){
                    try {
                        BaiduMapNavigation.openBaiduMapWalkNavi(para,getActivity());
                    } catch (BaiduMapAppNotSupportNaviException e) {
                        e.printStackTrace();
                        showDialog();
                    }
                    flag_gw=0;
                }
       else if (flag_gw==2){
            try {
                BaiduMapNavigation.openBaiduMapWalkNavi(para,getActivity());
            } catch (BaiduMapAppNotSupportNaviException e) {
                e.printStackTrace();
                showDialog();
            }
            flag_gw=0;
        }
                else if (flag_gw==3){
                    try {
                        BaiduMapNavigation.openBaiduMapBikeNavi(para,getActivity());
                    } catch (BaiduMapAppNotSupportNaviException e) {
                        e.printStackTrace();
                        showDialog();
                    }
                    flag_gw=0;
                }
    }
    /**
     * 提示未安装百度地图app或app版本过低
     */
    public void showDialog() {

        new AlertDialog.Builder(getActivity())
                .setTitle("Tips：")
                .setMessage("您尚未安装百度地图app或app版本过低，点击确认安装？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                          OpenClientUtil.getLatestBaiduMapApp(getActivity());
                    }
                })
                .setNegativeButton("取消",null)
                .show();
Toast.makeText(getActivity().getApplicationContext(),"尚未安卓百度app",Toast.LENGTH_LONG).show();
    }
    private void guideways(LatLng lat1,LatLng lat2){
        guide_baidumap.clear();
        // 初始化搜索模块，注册事件监听
        routePlanSearch = RoutePlanSearch.newInstance();
        routePlanSearch.setOnGetRoutePlanResultListener(routePlanResultListener);
        if (plag_choose==1){
            PlanNode stMassNode = PlanNode.withLocation(lat1);
            PlanNode enMassNode = PlanNode.withLocation(lat2);
            routePlanSearch.masstransitSearch(new MassTransitRoutePlanOption().tacticsIntercity(MassTransitRoutePlanOption.TacticsIntercity.ETRANS_LEAST_PRICE).from(stMassNode).to(enMassNode));
            //跨城换乘策略(价格低、时间短、出发早)
            plag_choose=0;
        }
        else if (plag_choose==2){
            PlanNode stMassNode = PlanNode.withLocation(lat1);
            PlanNode enMassNode = PlanNode.withLocation(lat2);
            DrivingRoutePlanOption option3 = new DrivingRoutePlanOption();

            option3.from(stMassNode);
            option3.to(enMassNode);
            if (startcity.equals(endcity))
            routePlanSearch.drivingSearch(option3);
            else{
                Toast.makeText(getContext().getApplicationContext(),"系统只能为同城进行自驾规划路线！！！",Toast.LENGTH_LONG).show();
            }
        }
    }
    private void sunmmy(){
        final View v1 = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.dialog_mass, null);
        final EditText et_startcity=(EditText)v1.findViewById(R.id.et_massstartcity);
        final EditText et_startplace=(EditText)v1.findViewById(R.id.et_massstartplace);
        final EditText et_endcity=(EditText)v1.findViewById(R.id.et_massendcity);
        final EditText et_endplace=(EditText)v1.findViewById(R.id.et_massendplace);
        if (flag_guide==1 || flag_plan==1 || indoor_onoff==true){
            new AlertDialog.Builder(getActivity())
                    .setTitle("Tips：")
                    .setMessage("先清除当前的地图上的记录？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            flag_guide=0;
                            flag_plan=0;
                            indoor_onoff=false;
                            startcity=null;
                            startplace=null;
                            endcity=null;
                            endplace=null;
                            plag_choose=0;
                            new AlertDialog.Builder(getActivity())
                                    .setTitle("请输入换乘的起点和终点")
                                    .setView(v1)
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            startcity=et_startcity.getText().toString().trim();
                                            startplace=et_startplace.getText().toString().trim();
                                            endcity=et_endcity.getText().toString().trim();
                                            endplace=et_endplace.getText().toString().trim();
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mSearch.geocode(new GeoCodeOption().city(
                                                            startcity).address(startplace));
                                                    flag_zhong=1;
                                                }
                                            }).start();

                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        Thread.currentThread().sleep(5000);
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                    mSearch.geocode(new GeoCodeOption().city(
                                                            endcity).address(endplace));
                                                    flag_zhong=2;
                                                    flag_plan=1;
                                                }
                                            }).start();
                                        }
                                    })
                                    .setNegativeButton("取消",null).show();
                        }
                    })
                    .setNegativeButton("取消",null).show();
        }
        else{
            new AlertDialog.Builder(getActivity())
                    .setTitle("请输入换乘的起点和终点")
                    .setView(v1)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startcity=et_startcity.getText().toString().trim();
                            startplace=et_startplace.getText().toString().trim();
                            endcity=et_endcity.getText().toString().trim();
                            endplace=et_endplace.getText().toString().trim();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    mSearch.geocode(new GeoCodeOption().city(
                                            startcity).address(startplace));
                                    flag_zhong=1;
                                }
                            }).start();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.currentThread().sleep(5000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    mSearch.geocode(new GeoCodeOption().city(
                                            endcity).address(endplace));
                                    flag_zhong=2;
                                    flag_plan=1;
                                }
                            }).start();
                        }
                    })
                    .setNegativeButton("取消",null).show();
        }
    }
    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
        if(geoCodeResult == null || geoCodeResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND){
            flag_plan=0;
            guide_baidumap.clear();
            flag_zhong=0;
            flag_guide=0;
            startcity=null;
            startplace=null;
            endcity=null;
             endplace=null;
            longttitude=0;
            lat=0;
            Toast.makeText(getActivity().getApplicationContext(),"未找到结果",Toast.LENGTH_LONG).show();
            return;
        }
        guide_baidumap.clear();
        guide_baidumap.addOverlay(new MarkerOptions()
                .position(geoCodeResult.getLocation())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marka)));

        MapStatusUpdate status=MapStatusUpdateFactory.newLatLng(geoCodeResult.getLocation());
        guide_baidumap.setMapStatus(status);

        lat=geoCodeResult.getLocation().latitude;
        longttitude=geoCodeResult.getLocation().longitude;

        if (flag_zhong==1){
            lang1=new LatLng(lat,longttitude);
            flag_zhong=2;
        }
        else if (flag_zhong==2){
            flag_zhong=0;
            lang2=new LatLng(lat,longttitude);
            guideways(lang1,lang2);
        }
        else{

        }

        if (flag_gw==1){
            lang1=new LatLng(lat,longttitude);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    guide_walk(lat_zhong,lang1);
                }
            }).start();
        }
       else if (flag_gw==2){
            lang1=new LatLng(lat,longttitude);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    guide_walk(lat_zhong,lang1);
                }
            }).start();
        }
        else if (flag_gw==3){
            lang1=new LatLng(lat,longttitude);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    guide_walk(lat_zhong,lang1);
                }
            }).start();
        }

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {

    }
    OnGetRoutePlanResultListener routePlanResultListener = new OnGetRoutePlanResultListener() {

        @Override
        public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

        }

        @Override
        public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

        }

        @Override
        public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {
            if (massTransitRouteResult == null || massTransitRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(getActivity().getApplicationContext(), "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            }
            if (massTransitRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                // 起终点模糊，获取建议列表
                massTransitRouteResult.getSuggestAddrInfo();
                Toast.makeText(getActivity().getApplicationContext(), "起终点模糊，请重新输入", Toast.LENGTH_SHORT).show();
                return;
            }
            if (massTransitRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                MassTransitRouteOverlay overlay = new MassTransitRouteOverlay(guide_baidumap);
                guide_baidumap.setOnMarkerClickListener(overlay);
                overlay.setData(massTransitRouteResult.getRouteLines().get(0));
                MassTransitRouteLine line = massTransitRouteResult.getRouteLines().get(0);
                overlay.setData(line);
                MassTransitRouteLine transitRouteLine=massTransitRouteResult.getRouteLines().get(0);
                if (massTransitRouteResult.getOrigin().getCityId() == massTransitRouteResult.getDestination().getCityId()) {
                    // 同城
                    overlay.setSameCity(true);

                    StringBuffer sb=new StringBuffer();
                    String rr1=transitRouteLine.getArriveTime();//预估到达时间
                    String rr2=String.valueOf(transitRouteLine.getPrice());//价格
                    StringBuffer s1=new StringBuffer();
                    List<List<MassTransitRouteLine.TransitStep>> steps = transitRouteLine.getNewSteps();
                    for (int i = 0; i < steps.size(); i++) {
                        List<MassTransitRouteLine.TransitStep> subSteps = steps.get(i);
                        MassTransitRouteLine.TransitStep transitStep = subSteps.get(0);
                        s1.append(transitStep.getInstructions());
                    }
                    sb.append("总路程：").append(transitRouteLine.getDistance()/ 1000.f).append(" 千米").append("\n")
                            .append("大约耗时：").append(transitRouteLine.getDuration()/60).append(" 分").append("\n")
                            .append("预估到达的时间：").append(rr1).append("\n").append("价格预算是：").append(rr2).append("\n").append("元")
                            .append("换乘说明：").append(s1);
                    String r1=sb.toString();
                    new AlertDialog.Builder(getActivity())
                            .setTitle("从"+startcity+"的"+startplace+"到"+endcity+"的"+endplace)
                            .setMessage("系统已经为你选出一条价格低，时间短的线路，如下"+"\n"+r1)
                            .setPositiveButton("确定", null).show();
                } else {
                    // 跨城
                    overlay.setSameCity(false);

                    StringBuffer sb=new StringBuffer();
                    String rr1=transitRouteLine.getArriveTime();//预估到达时间
                    String rr2=String.valueOf(transitRouteLine.getPrice());//价格


                    List<List<MassTransitRouteLine.TransitStep>> steps = transitRouteLine.getNewSteps();
                    StringBuffer s1=new StringBuffer();
                    for (int i = 0; i < steps.size(); i++) {
                        List<MassTransitRouteLine.TransitStep> subSteps = steps.get(i);
                        for (int j = 0; j < subSteps.size(); j++) {
                            MassTransitRouteLine.TransitStep transitStep = subSteps.get(j);
                            s1.append(transitStep.getInstructions());
                        }
                    }
                    sb.append("总路程：").append(transitRouteLine.getDistance()/ 1000.f).append(" 千米").append("\n")
                            .append("大约耗时：").append(transitRouteLine.getDuration()/60).append(" 分").append("\n")
                            .append("预估到达的时间：").append(rr1).append("\n").append("价格预算是：").append(rr2).append("元").append("\n")
                            .append("换乘说明：").append(s1);
                    String r1=sb.toString();
                    new AlertDialog.Builder(getActivity())
                            .setTitle("从"+startcity+"的"+startplace+"到"+endcity+"的"+endplace)
                            .setMessage("系统已经为你选出一条价格低，时间短的线路，如下"+"\n"+r1)
                            .setPositiveButton("确定", null).show();

                }
                guide_baidumap.clear();
                overlay.addToMap();
                overlay.zoomToSpan();

            }
        }

        @Override
        public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

            if (drivingRouteResult == null || drivingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(getActivity().getApplicationContext(), "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            }
            if (drivingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                // result.getSuggestAddrInfo()
                Toast.makeText(getActivity().getApplicationContext(), "输入地址有歧义，请重新输入", Toast.LENGTH_SHORT).show();
                return;
            }
            if (drivingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                DrivingRouteOverlay overlay = new DrivingRouteOverlay(guide_baidumap);
                guide_baidumap.setOnMarkerClickListener(overlay);
                overlay.setData(drivingRouteResult.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
                List<String> datas_driving = new ArrayList<String>();
                for(DrivingRouteLine drivingRouteLine : drivingRouteResult.getRouteLines()){
                    StringBuffer sb=new StringBuffer();
                    for (int j=0;j<drivingRouteLine.getAllStep().size();j++){
                        sb.append(drivingRouteLine.getAllStep().get(j).getExitInstructions()).append(", ");
                    }
                    String result=sb.toString();
                    datas_driving.add( "路线规划如下："+"\n"+result+"\n"+ "全程距离："
                            + drivingRouteLine.getDistance() / 1000.f + "千米，大约用时:" + drivingRouteLine.getDuration() / 60 + "分");
                }
                new AlertDialog.Builder(getActivity())
                        .setTitle("从"+startcity+"的"+startplace+"到"+endcity+"的"+endplace)
                        .setMessage("系统已经为你选出一条用时最短的线路，如下"+datas_driving.get(0))
                        .setPositiveButton("确定",null)
                        .show();
                if (datas_driving.size()==0){
                    Toast.makeText(getActivity().getApplicationContext(),"搜索不到相应线路",Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

        }

        @Override
        public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

        }
    };
}
