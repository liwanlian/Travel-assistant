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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.BusLineOverlay;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
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

public class Fragment_BuSu extends Fragment {
    private View rootview;
    InputStreamReader inputStreamReader;
    String data_PCA;//省份  地级市  区县
    JSONObject object_total;
    JSONObject Object_total1;
    JSONArray arr_total;
    JSONObject object_fen;

    //找地点界面的控件
    Spinner sp1, sp2;
    EditText bs_lines;
    TextView bs_bus;
    TextView bs_subway;
    TextureMapView bs_mapview;
    BaiduMap bs_baiduMap;
    TextView tv_guideway;


    List<String> data1;
    List<String> data2;

    ArrayAdapter<String> arr1;
    ArrayAdapter<String> arr2;
    int flag_province;

    protected LatLng hmPos = new LatLng(40.050513, 116.30361);


    private boolean mIsInit = false;//数据是否加载完成
    private boolean mIsPrepared = false;//UI是否准备完成

    String citynamw;
    //POi搜索
    private PoiSearch poiSearch;

    PoiSearch   busPoiSearch = PoiSearch.newInstance();//Poi检索对象
    BusLineSearch busLineSearch = BusLineSearch.newInstance();//公交检索对象
    private List<String> buslineIdList=null;
    private int buslineIndex = 0;
    int flag=0;//公交车flag=1， 地铁 flag=2
    int max=0;
    int onflag=0;//地图上是否有检索的痕迹
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootview = inflater.inflate(R.layout.page_busb, container, false);
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
                sp1 = (Spinner) rootview.findViewById(R.id.bs_spt);
                sp2 = (Spinner) rootview.findViewById(R.id.bs_spt2);
                bs_lines = (EditText) rootview.findViewById(R.id.bs_lines);
                bs_subway = (TextView) rootview.findViewById(R.id.bs_subwayline);
                bs_bus = (TextView) rootview.findViewById(R.id.bs_busline);
                bs_mapview = (TextureMapView) rootview.findViewById(R.id.bs_mapview);
                tv_guideway=(TextView)rootview.findViewById(R.id.tv_guideline);

                bs_baiduMap = bs_mapview.getMap();//获取地图控制器
                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(hmPos);
                bs_baiduMap.setMapStatus(mapStatusUpdate);
                //设置地图缩放为15
                mapStatusUpdate = MapStatusUpdateFactory.zoomTo(15);
                bs_baiduMap.setMapStatus(mapStatusUpdate);
                parsedata();

                busLineSearch=BusLineSearch.newInstance();
                busPoiSearch = PoiSearch.newInstance();
                bs_lines.setInputType(InputType.TYPE_CLASS_NUMBER );//限制输入数字
                tv_function();
            }
        }.start();
    }
    private  void tv_function(){

        bs_bus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onflag==0)
                {
                    buslineIdList = new ArrayList<String>();
                    busLineSearch.setOnGetBusLineSearchResultListener(busLineSearchResultListener);
                    busPoiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener);
                    String key=bs_lines.getText().toString().trim();
                    busPoiSearch.searchInCity(new PoiCitySearchOption().city(citynamw).keyword(key).scope(2));
                    flag=1;
                    onflag=1;
                }
                else{
                    new AlertDialog.Builder(getActivity())
                            .setMessage("先清除掉地图上的检索记录？")
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    onflag=0;
                                    bs_baiduMap.clear();
                                    tv_guideway.setText(" ");
                                    tv_guideway.setVisibility(View.INVISIBLE);
                                    buslineIdList = new ArrayList<String>();
                                    busLineSearch.setOnGetBusLineSearchResultListener(busLineSearchResultListener);
                                    busPoiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener);
                                    String key=bs_lines.getText().toString().trim();
                                    busPoiSearch.searchInCity(new PoiCitySearchOption().city(citynamw).keyword(key).scope(2));
                                    flag=1;
                                    onflag=1;
                                    max=0;
                                    buslineIndex=0;
                                }
                            })
                            .show();
                }
            }
        });//公交检索

        bs_subway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onflag==0)
                {
                    buslineIdList = new ArrayList<String>();
                    busLineSearch.setOnGetBusLineSearchResultListener(busLineSearchResultListener);
                    busPoiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener);
                    String key=bs_lines.getText().toString().trim();
                    busPoiSearch.searchInCity(new PoiCitySearchOption().city(citynamw).keyword("地铁"+key).scope(2));
                    flag=2;
                    onflag=1;
                }
                else{
                    new AlertDialog.Builder(getActivity())
                            .setMessage("先清除掉地图上的检索记录？")
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    onflag=0;
                                    bs_baiduMap.clear();
                                    tv_guideway.setText(" ");
                                    tv_guideway.setVisibility(View.INVISIBLE);
                                    buslineIdList = new ArrayList<String>();
                                    busLineSearch.setOnGetBusLineSearchResultListener(busLineSearchResultListener);
                                    busPoiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener);
                                    String key=bs_lines.getText().toString().trim();
                                    busPoiSearch.searchInCity(new PoiCitySearchOption().city(citynamw).keyword("地铁"+key).scope(2));
                                    flag=2;
                                    onflag=1;
                                    max=0;
                                    buslineIndex=0;
                                }
                            })
                            .show();
                }
            }
        });

        tv_guideway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (max-1>buslineIndex){
                    buslineIndex++;
                    busLineSearch.searchBusLine((new BusLineSearchOption()
                            .city(citynamw)
                            .uid(buslineIdList.get(buslineIndex))

                    ));
                }
                else if (max-1==buslineIndex){
                    buslineIndex=0;
                    busLineSearch.searchBusLine((new BusLineSearchOption()
                            .city(citynamw)
                            .uid(buslineIdList.get(buslineIndex))));
                }
            }
        });
    }
    public class MyBusOverLay extends BusLineOverlay {
        /**
         * 构造函数
         */
        PoiSearch busPoiSearch;
        BusLineResult mBusLineResult;
        public MyBusOverLay(BaiduMap baiduMap, PoiSearch busPoiSearch,BusLineResult busLineResult) {
            super(baiduMap);
            this.busPoiSearch = busPoiSearch;
            this.mBusLineResult=busLineResult;
        }
        @Override
        public boolean onBusStationClick(int index) {
            //获取点击的标记物的数据
            Log.e("TAG", "站点：" + mBusLineResult.getBusLineName());
            Log.e("TAG", "运营时间：" + mBusLineResult.getStartTime() + "---" + mBusLineResult.getEndTime());
            Log.e("TAG", "费用：" + mBusLineResult.getBasePrice() + "---" + mBusLineResult.getMaxPrice());
            //  发起一个详细检索,要使用uid
            busPoiSearch.searchPoiDetail(new PoiDetailSearchOption().poiUid(mBusLineResult.getUid()));
            return true;
        }
    }
    OnGetBusLineSearchResultListener busLineSearchResultListener = new OnGetBusLineSearchResultListener() {
        @Override
        public void onGetBusLineResult(BusLineResult busLineResult) {

            if (busLineResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(getActivity().getApplicationContext(), "抱歉，未找到结果",
                        Toast.LENGTH_SHORT).show();
            }
            else {
                bs_baiduMap.clear();
                BusLineOverlay busLineOverlay = new MyBusOverLay(bs_baiduMap,busPoiSearch,busLineResult);
                busLineOverlay.setData(busLineResult);
                busLineOverlay.addToMap();
                busLineOverlay.zoomToSpan();

                StringBuffer sb1=new StringBuffer();
               for (int i=0;i<busLineResult.getStations().size();i++){
                   sb1.append(busLineResult.getStations().get(i).getTitle()).append("、");
               }

                StringBuffer sb=new StringBuffer();
                sb.append("线路名称:").append(busLineResult.getBusLineName()).append("\n")
                        .append("价格范围为：").append(busLineResult.getBasePrice()).append("-----").append(busLineResult.getMaxPrice()).append("元").append("\n")
                        .append("途径的站点有：").append(sb1).append("\n")
                        .append("工作时间：").append(busLineResult.getStartTime()).append("------").append(busLineResult.getEndTime());

                String rr=sb.toString();

                new AlertDialog.Builder(getActivity())
                        .setTitle("当前的线路分析如下：")
                        .setMessage(rr)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
            }
        }
    };

    OnGetPoiSearchResultListener poiSearchResultListener = new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {


            buslineIdList.clear();
            if (flag==1){
                 for (PoiInfo poi : poiResult.getAllPoi()) {
                     System.out.println("type1="+poi.getPoiDetailInfo().tag);
                     if (poi.getPoiDetailInfo().tag.equals("公交线路")||poi.getPoiDetailInfo().tag.equals("公交站")
                             ) {
                         buslineIdList.add(poi.uid);
                     }
                 }

            }
             else if (flag==2){
                 for (PoiInfo poi : poiResult.getAllPoi()) {
                     System.out.println("type2="+poi.getPoiDetailInfo().tag);
                     if ( poi.getPoiDetailInfo().tag.equals("地铁线路")) {
                         buslineIdList.add(poi.uid);
                     }
                 }

             }
             else{

             }
             max=buslineIdList.size();
            buslineIndex=0;
            //如下代码为发起检索代码，定义监听者和设置监听器的方法与POI中的类似
            if (buslineIdList.size()==1)
            {
                busLineSearch.searchBusLine((new BusLineSearchOption()
                        .city(citynamw)
                        .uid(buslineIdList.get(buslineIndex))));
            }
            else if (buslineIdList.size()>1){
                tv_guideway.setVisibility(View.VISIBLE);
                tv_guideway.setText("当前的检索出来的线路共有"+String.valueOf(buslineIdList.size()+"条   ")+"下一条");
                busLineSearch.searchBusLine((new BusLineSearchOption()
                        .city(citynamw)
                        .uid(buslineIdList.get(buslineIndex))));
            }
            else {
                Toast.makeText(getActivity().getApplicationContext(),"搜索不到你要的线路",Toast.LENGTH_LONG).show();
                return;
            }
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult arg0) {

        }

        @Override
        public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }
    };
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
    OnGetPoiSearchResultListener resultListener = new OnGetPoiSearchResultListener() {

        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            bs_baiduMap.clear();
            //如果搜索到的结果不为空，并且没有错误
            if (poiResult != null && poiResult.error == PoiResult.ERRORNO.NO_ERROR) {
                List<PoiInfo> allPoi = poiResult.getAllPoi();
                MyOverLay overlay = new MyOverLay(bs_baiduMap, poiSearch);//这传入search对象，因为一般搜索到后，点击时方便发出详细搜索
                //设置数据,这里只需要一步，
                overlay.setData(poiResult);
                //添加到地图
                overlay.addToMap();
                //将显示视图拉倒正好可以看到所有POI兴趣点的缩放等级
                overlay.zoomToSpan();//计算工具
                //设置标记物的点击监听事件
                bs_baiduMap.setOnMarkerClickListener(overlay);
                Toast.makeText(getActivity().getApplicationContext(),"定位成功",Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "搜索不到你需要的信息！", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
            bs_baiduMap.clear();
            if (poiDetailResult.error != SearchResult.ERRORNO.NO_ERROR) {

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

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }
    };
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
                Toast.makeText(getActivity().getApplicationContext(),citynamw,Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }
    //公交路线的点的POI检索的监听对象，和上面的接口对象其实是一个类的，但是里面的处理是不一样的
    //在里面判断是否是搜索到公交路线的Poi点


    @Override
    public void onResume() {
        super.onResume();
        bs_mapview.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        bs_mapview.onPause();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        bs_mapview.onDestroy();
       busLineSearch.destroy();
        poiSearch.destroy();
    }

}
