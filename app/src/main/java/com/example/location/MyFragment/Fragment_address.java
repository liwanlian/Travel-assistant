package com.example.location.MyFragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.BikingRouteOverlay;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRouteLine;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.example.location.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

//寻找地址的fragment
public class Fragment_address extends Fragment implements  OnGetGeoCoderResultListener {
    View rootview;

    InputStreamReader inputStreamReader;
    String data_PCA;//省份  地级市  区县
    JSONObject object_total;
    JSONObject Object_total1;
    JSONArray arr_total;
    JSONObject object_fen;
    JSONObject object_province;//省
    JSONObject object_city;

    //找地点界面的控件
    Spinner sp1, sp2;
    EditText et_address;
    TextView tv_sure;
    TextView tv_exchange;
    TextureMapView address_mapview;
    BaiduMap address_baiduMap;
    TextView tv_guideway;

    List<String> data1;
    List<String> data2;


    ArrayAdapter<String> arr1;
    ArrayAdapter<String> arr2;
    ArrayAdapter<String> arr3;

    int flag_province;
    int flag_city;

    protected LatLng hmPos = new LatLng(40.050513, 116.30361);


    private boolean mIsInit = false;//数据是否加载完成
    private boolean mIsPrepared = false;//UI是否准备完成

    //搜索的部分
    private GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用 地理编码查询接口
    String citynamw;
    //POi搜索
    private PoiSearch poiSearch;

    //动态搜索
    String[] detailaddress;
    String[] titleaddress;
    List<String> detail;
    List<String>title;

    double lat;//经度
    double longttitude;//纬度
    int search_flag=0;//搜索标志
    String areaname;
    String placename;

    //导航
    String chooseway="步行规划";
    private int index = -1;
    private int totalLine = 0;// 记录某种搜索出的方案数量
    private RoutePlanSearch routePlanSearch;// 路径规划搜索接口
    private int drivintResultIndex = 0;// 驾车路线方案index
    private  int guideways=0;//线路条数
    private String startnode;//起点记录点
    private  String endnode;//终点记录点
    private int flag_waychoose=0;
    private int choosed=0;
    LatLng lang1;
    LatLng lang2;
    LatLng zhong;
    int flag_zhong=0;
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
//        if (null==rootview){
//            rootview=inflater.inflate(R.layout.page_address,container,false);
//            initView(rootview);
//        }
        rootview = inflater.inflate(R.layout.page_address, container, false);
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
                // 1. 加载数据
                // 2. 更新UI
                // 3. mIsInit = true
                sp1 = (Spinner) rootview.findViewById(R.id.spt);
                sp2 = (Spinner) rootview.findViewById(R.id.spt2);
                et_address = (EditText) rootview.findViewById(R.id.et_address);
                tv_sure = (TextView) rootview.findViewById(R.id.tv_sure);
                tv_exchange = (TextView) rootview.findViewById(R.id.tv_change);
                address_mapview = (TextureMapView) rootview.findViewById(R.id.address_mapview);
                tv_guideway=(TextView)rootview.findViewById(R.id.tv_guideway);

                address_baiduMap = address_mapview.getMap();//获取地图控制器
                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(hmPos);
                address_baiduMap.setMapStatus(mapStatusUpdate);
                //设置地图缩放为15
                mapStatusUpdate = MapStatusUpdateFactory.zoomTo(15);
                address_baiduMap.setMapStatus(mapStatusUpdate);
                parsedata();

                tv_function();

                mIsInit = true;

            }
        }.start();
    }
    private void tv_function(){
        tv_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 placename = et_address.getText().toString();
                 if (flag_waychoose==1){
                     new AlertDialog.Builder(getActivity())
                             .setTitle("Tips：")
                             .setMessage("先清除当前的导航记录？")
                             .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialogInterface, int i) {
                                     flag_waychoose=0;
                                     startnode=null;
                                     endnode=null;
                                     choosed=0;
                                     chooseway="步行规划";
                                     flag_zhong=0;
                                     lat=0;
                                     longttitude=0;
                                     tv_guideway.setText("");
                                     tv_guideway.setVisibility(View.INVISIBLE);
                                     address_baiduMap.clear();
                                     function_tvsure();
                                 }
                             })
                             .setNegativeButton("取消",null).show();
                 }
              else{
                     function_tvsure();
                 }
            }
        });
        tv_exchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content=tv_exchange.getText().toString();
                if (content.equals("清除")){
                    search_flag=0;
                    poiSearch = PoiSearch.newInstance();
                    poiSearch.setOnGetPoiSearchResultListener(resultListener);
                    PoiCitySearchOption citySearchOption = new PoiCitySearchOption();
                    citySearchOption.city(citynamw);// 城市
                    citySearchOption.keyword(citynamw);// 关键字
                    citySearchOption.pageNum(0);
                    // 为PoiSearch设置搜索方式.
                    poiSearch.searchInCity(citySearchOption);
                    flag_waychoose=0;
                    startnode=null;
                    endnode=null;
                    choosed=0;
                    chooseway="步行规划";
                    flag_zhong=0;

                    tv_guideway.setText("");
                    tv_guideway.setVisibility(View.INVISIBLE);
                    longttitude=0;
                    lat=0;
                    tv_exchange.setText("路线规划");
                    areaname=null;
                }
                else {
                    final View v1 = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.guide_layout, null);
                    final EditText et_startplace=(EditText)v1.findViewById(R.id.et_start);
                    final EditText et_endplace=(EditText)v1.findViewById(R.id.et_endplace);
                    final  RadioButton rb_walk=(RadioButton)v1.findViewById(R.id.bt_walk);
                    final  RadioButton rb_bus=(RadioButton)v1.findViewById(R.id.bt_bus);
                    final RadioButton rb_subway=(RadioButton)v1.findViewById(R.id.bt_subway);
                    final  RadioButton rb_drive=(RadioButton)v1.findViewById(R.id.bt_drive);
                    final   RadioGroup rg=(RadioGroup)v1.findViewById(R.id.group_guide);
                    rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup radioGroup, int i) {
                            chooseway=null;
                           if (i==rb_walk.getId()){
                               chooseway="步行规划";
                           }
                           else if (i==rb_bus.getId()){
                               chooseway="公交规划";
                           }
                           else if (i==rb_subway.getId()){
                               chooseway="踩自行车";
                           }
                           else if (i==rb_drive.getId()){
                               chooseway="自驾规划";
                           }
                        }
                    });
                    if (flag_waychoose==1){
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Tips：")
                                .setMessage("先清除当前的导航记录？")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        flag_waychoose=0;
                                        startnode=null;
                                        endnode=null;
                                        choosed=0;
                                        chooseway="步行规划";
                                        flag_zhong=0;

                                        tv_guideway.setText("");
                                        tv_guideway.setVisibility(View.INVISIBLE);
                                        longttitude=0;
                                        lat=0;
                                        tv_exchange.setText("路线规划");

                                        address_baiduMap.clear();
                                        new AlertDialog.Builder(getActivity())
                                                .setTitle("你当前所在的城市："+citynamw)
                                                .setView(v1)
                                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        String start=et_startplace.getText().toString();//起点
                                                        String end=et_endplace.getText().toString();//终点
                                                        startnode=start;
                                                        endnode=end;
                                                        flag_zhong=1;

                                                        new Thread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                mSearch.geocode(new GeoCodeOption().city(
                                                                        citynamw).address(startnode));
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
                                                                        citynamw).address(endnode));
                                                                flag_zhong=2;
                                                            }
                                                        }).start();


                                                        System.out.println("开始："+lang1+"终点："+lang2);

                                                        flag_waychoose=1;
                                                    }
                                                })
                                                .setNegativeButton("取消",null).show();
                                    }
                                })
                                .setNegativeButton("取消",null).show();
                    }
                    else{
                        new AlertDialog.Builder(getActivity())
                                .setTitle("你当前所在的城市："+citynamw)
                                .setView(v1)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        String start=et_startplace.getText().toString();//起点
                                        String end=et_endplace.getText().toString();//终点
                                        startnode=start;
                                        endnode=end;
                                        flag_zhong=1;
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mSearch.geocode(new GeoCodeOption().city(
                                                        citynamw).address(startnode));
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
                                                        citynamw).address(endnode));
                                                flag_zhong=2;
                                            }
                                        }).start();
//                                        mSearch.geocode(new GeoCodeOption().city(
//                                                citynamw).address(start));
//
//                                        mSearch.geocode(new GeoCodeOption().city(
//                                                citynamw).address(end));

                                        System.out.println("开始："+lang1+"终点："+lang2);
                                      //  guidewayschoose(lang1,lang2);
                                        flag_waychoose=1;
                                    }
                                })
                                .setNegativeButton("取消",null).show();
                    }
                    ;

                }//导航
            }
        });

        tv_guideway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText et = new EditText(getActivity());
                et.setInputType(InputType.TYPE_CLASS_NUMBER );//限制输入数字
                final String st_way=String.valueOf(guideways);
                new AlertDialog.Builder(getActivity())
                        .setTitle("搜索结果显示：")
                        .setMessage("从"+startnode+"到"+endnode+"共有"+st_way+"个方式选择")
                        .setView(et)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String result=et.getText().toString();
                                int choice=Integer.valueOf(result);
                                if (choice<=guideways)
                                {
                                    if (choice==0){
                                        Toast.makeText(getActivity().getApplicationContext(),"不能输入0",Toast.LENGTH_LONG).show();
                                    }
                                    else{
                                        guidewayschoose(lang1,lang2);
                                        choosed=choice;
                                    }
                                }
                                else{
                                    StringBuffer sb=new StringBuffer();
                                    sb.append("共有：").append(st_way).append("选择!!!");
                                    String rsb=sb.toString();
                                    Toast.makeText(getActivity().getApplicationContext(),rsb,Toast.LENGTH_LONG).show();
                                }
                            }
                        }).setNegativeButton("取消",null).show();
            }
        });
    }
    private void function_tvsure(){
        if (search_flag==0){
            mSearch.geocode(new GeoCodeOption().city(
                    citynamw).address(placename));
            search_flag=1;
            tv_exchange.setText("清除");
            areaname=placename;
        }
        else if (search_flag==1){
//

//    通过LayoutInflater来加载一个xml的布局文件作为一个View对象
            View v = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.tip_dialog, null);
            final EditText et_ridias = (EditText)v.findViewById(R.id.et_ridias);
            et_ridias.setText("1000");//默认在1000的半径内搜索
            et_ridias.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);//限制输入数字和小数点
            TextView tv_meter=(TextView)v.findViewById(R.id.tv_meter);
            tv_meter.setText("米内搜索"+placename);
            new AlertDialog.Builder(getActivity())
                    .setTitle("当前定位的区域是在："+areaname)
                    .setView(v)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            poiSearch = PoiSearch.newInstance();
                            poiSearch.setOnGetPoiSearchResultListener(resultListener);

                            //定义Maker坐标点,深圳大学经度和纬度113.943062,22.54069
                            //设置的时候经纬度是反的 纬度在前，经度在后
                            System.out.println("经度："+lat+"纬度："+longttitude);
                            LatLng point = new LatLng(lat, longttitude);
                            //周边检索
                            PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption();
                            nearbySearchOption.location(point);
                            nearbySearchOption.keyword(placename);
                            String ridia=et_ridias.getText().toString();
                            int data_ridias=Integer.valueOf(ridia);
                            nearbySearchOption.radius(data_ridias);// 检索半径，单位是米
                            nearbySearchOption.pageNum(1);//搜索一页
                            poiSearch.searchNearby(nearbySearchOption);// 发起附近检索请求
                        }
                    }).setNegativeButton("取消",null).show();
//
        }
    }
    private void guidewayschoose(LatLng start,LatLng end){
        System.out.println("start="+start+"   "+"end"+end);
        if(chooseway.equals("步行规划")){
            address_baiduMap.clear();
            routePlanSearch = RoutePlanSearch.newInstance();
            routePlanSearch.setOnGetRoutePlanResultListener(routePlanResultListener);

            WalkingRoutePlanOption walkOption = new WalkingRoutePlanOption();
//            walkOption.from(PlanNode.withCityNameAndPlaceName(citynamw, start));
//            walkOption.to(PlanNode.withCityNameAndPlaceName(citynamw, end));
            walkOption.from(PlanNode.withLocation(start));
             walkOption.to(PlanNode.withLocation(end));
            routePlanSearch.walkingSearch(walkOption);
//                                        PlanNode.withLocation()
        }
        else if (chooseway.equals("公交规划")){
            address_baiduMap.clear();
            routePlanSearch = RoutePlanSearch.newInstance();
            routePlanSearch.setOnGetRoutePlanResultListener(routePlanResultListener);

            TransitRoutePlanOption option1 = new TransitRoutePlanOption();
//            PlanNode from = PlanNode.withCityNameAndPlaceName(citynamw, start);
////            PlanNode to = PlanNode.withCityNameAndPlaceName(citynamw, end);

            option1.city(citynamw);
            option1.from(PlanNode.withLocation(start));
            option1.to(PlanNode.withLocation(end))
//            PlanNode from = PlanNode.withLocation( start);
//            PlanNode to = PlanNode.withLocation(end);
            ;//必须加city，不然就会崩溃
            routePlanSearch.transitSearch(option1);
        }
        else if (chooseway.equals("踩自行车")){
            address_baiduMap.clear();
            routePlanSearch = RoutePlanSearch.newInstance();
            routePlanSearch.setOnGetRoutePlanResultListener(routePlanResultListener);

            BikingRoutePlanOption option2 = new BikingRoutePlanOption();
//            PlanNode from = PlanNode.withCityNameAndPlaceName(citynamw, start);
//            PlanNode to = PlanNode.withCityNameAndPlaceName(citynamw, end);
//            PlanNode from = PlanNode.withLocation( start);
//            PlanNode to = PlanNode.withLocation(end);
//            option2.from(from).to(to);
            option2.from(PlanNode.withLocation(start));
            option2.to(PlanNode.withLocation(end));
          //  option2.ridingType(0);
            routePlanSearch.bikingSearch(option2);

        }
        else if (chooseway.equals("自驾规划")){
            address_baiduMap.clear();
            routePlanSearch = RoutePlanSearch.newInstance();
            routePlanSearch.setOnGetRoutePlanResultListener(routePlanResultListener);

            DrivingRoutePlanOption option3 = new DrivingRoutePlanOption();
//            PlanNode from = PlanNode.withCityNameAndPlaceName(citynamw, start);
////            PlanNode to = PlanNode.withCityNameAndPlaceName(citynamw, end);
//            PlanNode from = PlanNode.withLocation(start);
//            PlanNode to = PlanNode.withLocation(end);
//            option3.currentCity(citynamw).from(from).to(to);
            option3.currentCity(citynamw);
            option3.from(PlanNode.withLocation(start));
            option3.to(PlanNode.withLocation(end));
            routePlanSearch.drivingSearch(option3);

        }
    }
    OnGetRoutePlanResultListener routePlanResultListener = new OnGetRoutePlanResultListener() {
        @Override
        public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
            address_baiduMap.clear();
            if (null == walkingRouteResult) {
                return;
            }
            if (walkingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                walkingRouteResult.getSuggestAddrInfo();
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("提示");
                builder.setMessage("检索地址有歧义，请重新设置。\n");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return;
            }
            if (walkingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(getActivity().getApplicationContext(), "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            }
            else{
                guideways=walkingRouteResult.getRouteLines().size();

                WalkingRouteOverlay overlay = new WalkingRouteOverlay(address_baiduMap);
                address_baiduMap.setOnMarkerClickListener(overlay);
                overlay.setData(walkingRouteResult.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
                List<String> datas_driving = new ArrayList<String>();
                for(WalkingRouteLine walkingRouteLine : walkingRouteResult.getRouteLines()){
                    StringBuffer sb=new StringBuffer();
                    for (int j=0;j<walkingRouteLine.getAllStep().size();j++){
                        sb.append(walkingRouteLine.getAllStep().get(j).getExitInstructions()).append(", ");
                    }
                    String result=sb.toString();
                    datas_driving.add("路线规划如下："+"\n"+result +"\n"+ "全程距离距离：  "
                            + walkingRouteLine.getDistance() / 1000.f + "千米，大约用时:" + walkingRouteLine.getDuration() / 60 + "分");
                }
                new AlertDialog.Builder(getActivity())
                        .setTitle("当前的线路分析如下：")
                        .setMessage(datas_driving.get(choosed))
                        .show();
            }

        }//步行导航

        @Override
        public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
            address_baiduMap.clear();
            List<TransitRouteLine> routeLines = transitRouteResult.getRouteLines();
            if (transitRouteResult == null || transitRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(getActivity().getApplicationContext(), "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            }
            if (transitRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                // result.getSuggestAddrInfo()
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("提示");
                builder.setMessage("检索地址有歧义，请重新设置。\n");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return;
            }
            if (transitRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
            //    guideways=transitRouteResult.getRouteLines().size();
//                if (routeLines.size()>1){
//                    tv_guideway.setVisibility(View.VISIBLE);
//                    String rr=String.valueOf(transitRouteResult.getRouteLines().size());
//                    tv_guideway.setText("当前可选的线路条数有："+rr);
//                   if (flag_waychoose==1){
//                       Toast.makeText(getActivity().getApplicationContext(),rr,Toast.LENGTH_LONG).show();
//                   }
//                    TransitRouteOverlay overlay = new TransitRouteOverlay(address_baiduMap);
//                    address_baiduMap.setOnMarkerClickListener(overlay);
//                    if (choosed==0)
//                        choosed=0;
//                    else
//                        choosed=choosed-1;
//                    overlay.setData(transitRouteResult.getRouteLines().get(choosed));
//                    overlay.addToMap();
//                    overlay.zoomToSpan();
//                    List<String> datas_driving = new ArrayList<String>();
//                    for(TransitRouteLine transitRouteLine : transitRouteResult.getRouteLines()){
//                        datas_driving.add("全程距离："
//                                + transitRouteLine.getDistance() / 1000.f + "千米，大约用时:" + transitRouteLine.getDuration() / 60 + "分");
//                    }
//                    new AlertDialog.Builder(getActivity())
//                            .setTitle("当前选择的线路为线路"+String.valueOf(choosed)+"：")
//                            .setMessage(datas_driving.get(choosed))
//                            .show();
//                }
//                 if (routeLines.size()==1){
//
//                }
//                else{
//                    Log.d("route result", "结果数<0");
//                }



                TransitRouteOverlay overlay = new TransitRouteOverlay(address_baiduMap);
                address_baiduMap.setOnMarkerClickListener(overlay);
                overlay.setData(transitRouteResult.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
                List<String> datas_driving = new ArrayList<String>();
                for(TransitRouteLine transitRouteLine : transitRouteResult.getRouteLines()){
                    datas_driving.add("全程距离： "
                            + transitRouteLine.getDistance() / 1000.f + "千米，大约用时:" + transitRouteLine.getDuration() / 60 + "分");
                }
                new AlertDialog.Builder(getActivity())
                        .setTitle("线路分析如下：:")
                        .setMessage(datas_driving.get(choosed))
                        .show();
            }

        }//换乘 市内公交车换乘

        @Override
        public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

        }

        @Override
        public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
            if (drivingRouteResult == null || drivingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(getActivity().getApplicationContext(), "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            }
            if (drivingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                // result.getSuggestAddrInfo()
                return;
            }
            if (drivingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
//                if (drivingRouteResult.getRouteLines().size() > 1) {
//                    tv_guideway.setVisibility(View.VISIBLE);
//                    String rr=String.valueOf(drivingRouteResult.getRouteLines().size());
//                    tv_guideway.setText("当前可选的线路条数有："+rr);
//                    if (flag_waychoose==1){
//                        Toast.makeText(getActivity().getApplicationContext(),rr,Toast.LENGTH_LONG).show();
//                    }
//                    DrivingRouteOverlay overlay = new DrivingRouteOverlay(address_baiduMap);
//                    address_baiduMap.setOnMarkerClickListener(overlay);
//                    if (choosed==0)
//                        choosed=0;
//                    else
//                        choosed=choosed-1;
//                    overlay.setData(drivingRouteResult.getRouteLines().get(choosed));
//                    overlay.addToMap();
//                    overlay.zoomToSpan();
//                    List<String> datas_driving = new ArrayList<String>();
//                    for(DrivingRouteLine drivingRouteLine : drivingRouteResult.getRouteLines()){
//                        StringBuffer sb=new StringBuffer();
//                        for (int j=0;j<drivingRouteLine.getAllStep().size();j++){
//                            sb.append(drivingRouteLine.getAllStep().get(j).getExitInstructions()).append(", ");
//                        }
//                        String result=sb.toString();
//                      datas_driving.add( "路线规划如下："+result+ "全程距离："
//                              + drivingRouteLine.getDistance() / 1000.f + "千米，大约用时:" + drivingRouteLine.getDuration() / 60 + "分");
//                    }
//                    new AlertDialog.Builder(getActivity())
//                            .setTitle("当前选择的线路为线路"+String.valueOf(choosed)+"：")
//                            .setMessage(datas_driving.get(choosed))
//                            .show();
//                }
//                 if (drivingRouteResult.getRouteLines().size()==1){
//
//                }
//                else{
//                    Log.d("route result", "结果数<0");
//                }
                DrivingRouteOverlay overlay = new DrivingRouteOverlay(address_baiduMap);
                address_baiduMap.setOnMarkerClickListener(overlay);
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
                        .setTitle("当前的线路分析如下：")
                        .setMessage(datas_driving.get(0))
                        .show();
            }
        }//自己开车导航

        @Override
        public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

        }

        @Override
        public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
            if (bikingRouteResult == null || bikingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(getActivity().getApplicationContext(), "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            }
            if (bikingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                // result.getSuggestAddrInfo()

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("提示");
                builder.setMessage("检索地址有歧义，请重新设置。");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return;
            }
            if (bikingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
//                if (bikingRouteResult.getRouteLines().size() > 1) {
//                    tv_guideway.setVisibility(View.VISIBLE);
//                    String rr=String.valueOf(bikingRouteResult.getRouteLines().size());
//                    tv_guideway.setText("当前可选的线路条数有："+rr);
//                    if (flag_waychoose==0){
//                        Toast.makeText(getActivity().getApplicationContext(),rr,Toast.LENGTH_LONG).show();
//                    }
//                    BikingRouteOverlay overlay = new BikingRouteOverlay(address_baiduMap);
//                    address_baiduMap.setOnMarkerClickListener(overlay);
//                    if (choosed==0)
//                        choosed=0;
//                    else
//                        choosed=choosed-1;
//                    overlay.setData(bikingRouteResult.getRouteLines().get(choosed));
//                    overlay.addToMap();
//                    overlay.zoomToSpan();
//                    List<String> datas_driving = new ArrayList<String>();
//                    for(BikingRouteLine bikingRouteLine : bikingRouteResult.getRouteLines()){
//                        StringBuffer sb=new StringBuffer();
//                        for (int j=0;j<bikingRouteLine.getAllStep().size();j++){
//                            sb.append(bikingRouteLine.getAllStep().get(j).getExitInstructions()).append(", ");
//                        }
//                        String result=sb.toString();
//                        datas_driving.add("路线规划描述如下：" +result+ "全程距离："
//                                + bikingRouteLine.getDistance() / 1000.f + "千米，大约用时:" + bikingRouteLine.getDuration() / 60 + "分");
//                    }
//                    new AlertDialog.Builder(getActivity())
//                            .setTitle("当前选择的线路为线路"+String.valueOf(choosed)+"：")
//                            .setMessage(datas_driving.get(choosed))
//                            .show();
//                }
//                 if (bikingRouteResult.getRouteLines().size()==1){
//
//                }
//                else{
//                    Log.d("route result", "结果数<0");
//                }
                BikingRouteOverlay overlay = new BikingRouteOverlay(address_baiduMap);
                address_baiduMap.setOnMarkerClickListener(overlay);
                overlay.setData(bikingRouteResult.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
                List<String> datas_driving = new ArrayList<String>();
                for(BikingRouteLine bikingRouteLine : bikingRouteResult.getRouteLines()){
                    StringBuffer sb=new StringBuffer();
                    for (int j=0;j<bikingRouteLine.getAllStep().size();j++){
                        sb.append(bikingRouteLine.getAllStep().get(j).getExitInstructions()).append(", ");
                    }
                    String result=sb.toString();
                    datas_driving.add("路线规划描述如下：" +"\n"+result+"\n"+ "全程距离："
                            + bikingRouteLine.getDistance() / 1000.f + "千米，大约用时:" + bikingRouteLine.getDuration() / 60 + "分");
                }
                new AlertDialog.Builder(getActivity())
                        .setTitle("当前的线路分析如下：")
                        .setMessage(datas_driving.get(0))
                        .show();
            }
        }
    };

        OnGetPoiSearchResultListener resultListener = new OnGetPoiSearchResultListener() {

        //获得POI的检索结果，一般检索数据都是在这里获取
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            address_baiduMap.clear();
            //如果搜索到的结果不为空，并且没有错误
            if (poiResult != null && poiResult.error == PoiResult.ERRORNO.NO_ERROR) {
                List<PoiInfo> allPoi = poiResult.getAllPoi();
                detailaddress=new String[allPoi.size()];
                titleaddress=new String[allPoi.size()];
                detail=new ArrayList<String>();
                title=new ArrayList<String>();
                for (int i=0;i<allPoi.size();i++){
                    title.add(allPoi.get(i).name);
                    detail.add(allPoi.get(i).address);
                }
                StringBuffer sb=new StringBuffer();
                for (int y=0;y<allPoi.size();y++){
                    sb.append("标题：").append(title.get(y)).append("  详细地址：").append(detail.get(y)).append("\n");
                }
                String rr=sb.toString();
                System.out.println("poi搜索结果"+rr);
                MyOverLay overlay = new MyOverLay(address_baiduMap, poiSearch);//这传入search对象，因为一般搜索到后，点击时方便发出详细搜索
                //设置数据,这里只需要一步，
                overlay.setData(poiResult);
                //添加到地图
                overlay.addToMap();
                //将显示视图拉倒正好可以看到所有POI兴趣点的缩放等级
                overlay.zoomToSpan();//计算工具
                //设置标记物的点击监听事件
                address_baiduMap.setOnMarkerClickListener(overlay);
                Toast.makeText(getActivity().getApplicationContext(),"定位成功",Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "搜索不到你需要的信息！", Toast.LENGTH_SHORT).show();
            }
        }
        //获得POI的详细检索结果，如果发起的是详细检索，这个方法会得到回调(需要uid)
        //详细检索一般用于单个地点的搜索，比如搜索一大堆信息后，选择其中一个地点再使用详细检索
        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
            address_baiduMap.clear();
            if (poiDetailResult.error != SearchResult.ERRORNO.NO_ERROR) {
                search_flag=0;
                flag_zhong=0;
                choosed=0;
                startnode=null;
                endnode=null;

                tv_exchange.setText("路线规划");
                address_baiduMap.clear();
                Toast.makeText(getActivity().getApplicationContext(), "抱歉，未找到结果",
                        Toast.LENGTH_SHORT).show();
            } else {// 正常返回结果的时候，此处可以获得很多相关信息
                Toast.makeText(getActivity().getApplicationContext(), poiDetailResult.getName() + ": "
                                + poiDetailResult.getAddress(),
                        Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

        }

        //获得POI室内检索结果
        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
        }
    };
    public class MyOverLay extends PoiOverlay {
        /**
         * 构造函数
         */
        PoiSearch poiSearch1;

        public MyOverLay(BaiduMap baiduMap, PoiSearch poiSearch) {
            super(baiduMap);
            this.poiSearch1 = poiSearch;
        }

        /**
         * 覆盖物被点击时
         */
        @Override
        public boolean onPoiClick(int i) {
            //获取点击的标记物的数据
            PoiInfo poiInfo = getPoiResult().getAllPoi().get(i);
            Log.e("TAG", poiInfo.name + "   " + poiInfo.address + "   " + poiInfo.phoneNum);
            StringBuffer sb=new StringBuffer();
            sb.append("你点击的位置是：").append(poiInfo.name).append("(").append(poiInfo.address).append(")");
            String rr=sb.toString();
            Toast.makeText(getActivity().getApplicationContext(),rr,Toast.LENGTH_LONG).show();
            //  发起一个详细检索,要使用uid
           // poiSearch1.searchPoiDetail(new PoiDetailSearchOption().poiUid(poiInfo.uid));
            return true;
        }
    }


    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
        if(geoCodeResult == null || geoCodeResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND){
            search_flag=0;
            tv_exchange.setText("路线规划");
            address_baiduMap.clear();

            flag_zhong=0;
            flag_waychoose=0;
            startnode=null;
            endnode=null;
            choosed=0;
            chooseway="步行规划";

            tv_guideway.setText("");
            tv_guideway.setVisibility(View.INVISIBLE);
            longttitude=0;
            lat=0;

            Toast.makeText(getActivity().getApplicationContext(),"未找到结果",Toast.LENGTH_LONG).show();
            return;
        }
        address_baiduMap.clear();
        address_baiduMap.addOverlay(new MarkerOptions()
                .position(geoCodeResult.getLocation())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marka)));

        MapStatusUpdate status=MapStatusUpdateFactory.newLatLng(geoCodeResult.getLocation());
        address_baiduMap.setMapStatus(status);

         lat=geoCodeResult.getLocation().latitude;
         longttitude=geoCodeResult.getLocation().longitude;

         zhong=geoCodeResult.getLocation();
//        t1.setText("Lat:"+String.valueOf(geoCodeResult.getLocation().latitude)+"\n"+
//                "Lon:"+String.valueOf(geoCodeResult.getLocation().longitude));
        System.out.println("Lat:"+geoCodeResult.getLocation().latitude+"   "+
                "Lon:"+geoCodeResult.getLocation().longitude);
        if (flag_zhong==1){
            lang1=new LatLng(lat,longttitude);
            flag_zhong=2;
        }
        else if (flag_zhong==2){
            flag_zhong=0;
            lang2=new LatLng(lat,longttitude);
            guidewayschoose(lang1,lang2);
        }
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
            search_flag=0;
            tv_exchange.setText("路线规划");
            address_baiduMap.clear();
            flag_waychoose=0;
            startnode=null;
            endnode=null;
            choosed=0;
            chooseway="步行规划";
            flag_zhong=0;

            tv_guideway.setText("");
            tv_guideway.setVisibility(View.INVISIBLE);
            longttitude=0;
            lat=0;

            Toast.makeText(getActivity().getApplicationContext(),"未找到结果",Toast.LENGTH_LONG).show();
            return;
        }
        address_baiduMap.clear();
        //获取地址并且标注
        address_baiduMap.addOverlay(new MarkerOptions().position(reverseGeoCodeResult.getLocation())
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_markb)));
        address_baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(reverseGeoCodeResult
                .getLocation()));

        lat=reverseGeoCodeResult.getLocation().latitude;
        longttitude=reverseGeoCodeResult.getLocation().longitude;

        if (flag_zhong==1){
            lang1=new LatLng(lat,longttitude);
            System.out.println("lang1="+lang1);

        }
         if (flag_zhong==2){

            lang2=new LatLng(lat,longttitude);
            System.out.println("lang1="+lang1);
            System.out.println("lang2="+lang1);
            guidewayschoose(lang1,lang2);
        }

    }
    private void parsedata(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    inputStreamReader = new InputStreamReader(getResources().openRawResource(R.raw.pcadata), "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    inputStreamReader.close();
                    bufferedReader.close();
                    data_PCA = stringBuilder.toString();
                    //   Log.i("TAG", stringBuilder.toString());
                    //textView.setText(data_PCA);
                    // parseprovince(data_PCA);
                    Message msg=Message.obtain();
                    msg.obj=data_PCA;
                    mhandler.sendMessage(msg);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    Handler mhandler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            String rr=(String)msg.obj;
            data1=new ArrayList<String>();
            data2=new ArrayList<String>();
            parseprovince(rr);
            spinnerfunction();
        }
    };
    private void parseprovince(String object){
        try {
            object_total=new JSONObject(object);
            Object_total1=object_total.getJSONObject("data");
            arr_total=Object_total1.getJSONArray("ppdata");
            for (int i=0;i<arr_total.length();i++){
                String jj=null;
                object_fen=arr_total.getJSONObject(i);
                jj=object_fen.getString("name");
                data1.add(jj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //适配器
        arr1= new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, data1);
        //设置样式
        arr1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //加载适配器
        sp1.setAdapter(arr1);
    }
    private JSONObject parsecity(String provincename){
        JSONObject  object_zpr=null;
        for (int i=0;i<arr_total.length();i++){
            try {
                JSONObject object_pr=arr_total.getJSONObject(i);
                String rr=object_pr.getString("name");
                if (rr.equals(provincename)){
                    object_zpr=object_pr;
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return object_zpr;
    }
    private void spinnerfunction(){

        sp1.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                JSONObject object;
                JSONArray jsonArray;
                String content=data1.get(i);
                flag_province=i;
                data2=new ArrayList<String>();
                try {
                    object= parsecity(content);
                    jsonArray=object.getJSONArray("cityList");
                    for (int y=0;y<jsonArray.length();y++){
                        JSONObject jb1=jsonArray.getJSONObject(y);
                        String rr=jb1.getString("name");
                        data2.add(rr);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //适配器
                arr2= new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, data2);
                //设置样式
                arr2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //加载适配器
                sp2.setAdapter(arr2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        sp2.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                citynamw=data2.get(i);
                poiSearch = PoiSearch.newInstance();
                poiSearch.setOnGetPoiSearchResultListener(resultListener);
                PoiCitySearchOption citySearchOption = new PoiCitySearchOption();
                citySearchOption.city(citynamw);// 城市
                citySearchOption.keyword(citynamw);// 关键字
                citySearchOption.pageNum(0);
                // 为PoiSearch设置搜索方式.
                poiSearch.searchInCity(citySearchOption);

                search_flag=0;
                longttitude=0;
                lat=0;
                tv_exchange.setText("路线规划");
                areaname=null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }
    @Override
    public void onResume() {
        super.onResume();
        address_mapview.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        address_mapview.onPause();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        address_mapview.onDestroy();
        mSearch.destroy();
        poiSearch.destroy();
    }
}

