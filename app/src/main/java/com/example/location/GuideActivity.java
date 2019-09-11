package com.example.location;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.example.location.Adapter.Adapter_myview;
import com.example.location.MyFragment.Fragment_address;
import com.example.location.MyFragment.Fragment_guide;
import com.example.location.MyFragment.Fragment_home;
import com.example.location.MyFragment.Fragment_BuSu;


import java.util.List;

public abstract class GuideActivity extends AppCompatActivity implements View.OnClickListener ,ViewPager.OnPageChangeListener{
    //底部菜单栏的四个布局
    protected LinearLayout ll_home;
    protected LinearLayout ll_address;
    protected  LinearLayout ll_subway;
    protected  LinearLayout ll_bus;
    //菜单栏中的四个imageview
    protected ImageView iv_home;
    protected  ImageView iv_address;
    protected  ImageView iv_subway;
    protected  ImageView iv_bus;
    //菜单栏中的四个标题
    protected TextView tv_home;
    protected TextView tv_address;
    protected TextView tv_subway;
    protected TextView tv_bus;
    //Viewpager内容填充
    protected ViewPager viewPager;
    //viewpager的适配器
    protected Adapter_myview adapter_myview;
    public List<View>views;

    //四个fragment
    Fragment_home fragment_home;
    Fragment_address fragment_address;
    Fragment_guide fragment_guide;
    Fragment_BuSu fragment_buSu;

    //首页的内容
    protected LatLng hmPos = new LatLng(40.050513, 116.30361);
    protected MapView mMapView;
   // protected BaiduMap baiduMap;
    protected TextView tv_location;


    //这里加final是为了不让子类覆盖，原因是为了预防这里的一些类还没初始化的时候就被子类调用
    @Override
    protected  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView();
        iv_home.setOnClickListener(this);
        iv_address.setOnClickListener(this);
        iv_subway.setOnClickListener(this);
        iv_bus.setOnClickListener(this);
        viewPager.setOnPageChangeListener(this);

        initViewPage(0);

       // regester_broadcast();
    }

    /**
     * 子类实现此方法
     */
  //  public abstract void regester_broadcast();
//    public abstract void page_Home();

    //初始化控件
    public void initView(){
        //底部菜单栏四个布局
        ll_home=(LinearLayout)findViewById(R.id.ll_home);
        ll_address=(LinearLayout)findViewById(R.id.ll_address);
        ll_subway=(LinearLayout)findViewById(R.id.ll_subway);
        ll_bus=(LinearLayout)findViewById(R.id.ll_bus);
        //菜单栏的四个标题
        tv_home=(TextView)findViewById(R.id.tv_home);
        tv_address=(TextView)findViewById(R.id.tv_address);
        tv_subway=(TextView)findViewById(R.id.tv_subway);
        tv_bus=(TextView)findViewById(R.id.tv_bus);
        //菜单栏的四个logo
        iv_home=(ImageView)findViewById(R.id.iv_home);
        iv_address=(ImageView)findViewById(R.id.iv_address);
        iv_subway=(ImageView)findViewById(R.id.iv_subway);
        iv_bus=(ImageView)findViewById(R.id.iv_bus);
        //viewpager
        viewPager=(ViewPager)findViewById(R.id.vp_content);
        //适配器
//        View page_01 = View.inflate(GuideActivity.this, R.layout.page_home, null);
//        View page_02 = View.inflate(GuideActivity.this, R.layout.page_address, null);
//        View page_03 = View.inflate(GuideActivity.this, R.layout.page_busb, null);
//        View page_04 = View.inflate(GuideActivity.this, R.layout.page_guide, null);
//
//        views = new ArrayList<View>();
//        views.add(page_01);
//        views.add(page_02);
//        views.add(page_03);
//        views.add(page_04);
//
//        this.adapter_myview = new Adapter_myview(views);
//        viewPager.setAdapter(adapter_myview);
    }

    public void onClick(View view) {
        restartbutton();
        switch (view.getId()){
            case R.id.iv_home:
                initViewPage(0);
               viewPager.setCurrentItem(0);
                iv_home.setImageResource(R.drawable.home_press);
                tv_home.setTextColor(0xff32CD32);
                break;
            case R.id.iv_address:
                initViewPage(1);
              viewPager.setCurrentItem(1);
                iv_address.setImageResource(R.drawable.station_press);
                tv_address.setTextColor(0xff32CD32);
                break;
            case R.id.iv_subway:
                initViewPage(2);
               viewPager.setCurrentItem(2);
                iv_subway.setImageResource(R.drawable.subway_press);
              tv_subway.setTextColor(0xff32CD32);
                break;
            case R.id.iv_bus:
                initViewPage(3);
                viewPager.setCurrentItem(3);
                iv_bus.setImageResource(R.drawable.bus_press);
                tv_bus.setTextColor(0xff32CD32);
                break;
            default:
                System.out.println("test5");
                break;
        }
    }

    protected void restartbutton(){
        //imageview设置为灰色
        iv_home.setImageResource(R.drawable.home_normal);
        iv_address.setImageResource(R.drawable.station_normal);
        iv_subway.setImageResource(R.drawable.subway_normal);
        iv_bus.setImageResource(R.drawable.bus_normal);
        //字体颜色设置
        tv_home.setTextColor(0xff000000);
        tv_address.setTextColor(0xff000000);
        tv_subway.setTextColor(0xff000000);
        tv_bus.setTextColor(0xff000000);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        restartbutton();
        switch (position){
            case 0:
                iv_home.setImageResource(R.drawable.home_press);
              //  tv_home.setTextColor(0xff1B940A);
                break;
            case 1:
                iv_address.setImageResource(R.drawable.station_press);
              //  tv_address.setTextColor(0xff1B940A);
                break;
            case 2:
                iv_subway.setImageResource(R.drawable.subway_press);
               // tv_subway.setTextColor(0xff1B940A);
                break;
            case 3:
                iv_bus.setImageResource(R.drawable.bus_press);
               // tv_bus.setTextColor(0xff1B940A);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /*
     * 初始化initViewPage
     */
    protected void initViewPage(int i) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();// 创建一个事务
        hideFragment(transaction);// 先把所有的Fragment隐藏了，然后下面再开始处理具体要显示的Fragment
        switch (i) {
            case 0:
                if (fragment_home == null) {
                    fragment_home = new Fragment_home();
                    transaction.add(R.id.id_content, fragment_home,Fragment_home.class.getName());// 将Fragment添加到Activity中
                } else {
                    transaction.show(fragment_home);
                }
                //transaction.add(R.id.id_content, fragment_home,Fragment_home.class.getName());// 将Fragment添加到Activity中
                break;
            case 1:
                if (fragment_address == null) {
                   fragment_address = new Fragment_address();
                    transaction.add(R.id.id_content, fragment_address,Fragment_address.class.getName());
                } else {
                    transaction.show(fragment_address);
                }
             //   transaction.add(R.id.id_content, fragment_address,Fragment_address.class.getName());
                break;
            case 2:
                if (fragment_buSu == null) {
                   fragment_buSu = new Fragment_BuSu();
                    transaction.add(R.id.id_content, fragment_buSu, Fragment_BuSu.class.getName());
                } else {
                    transaction.show(fragment_buSu);
                }
                //transaction.add(R.id.id_content, fragment_buSu,Fragment_BuSu.class.getName());
                break;
            case 3:
                if (fragment_guide == null) {
                    fragment_guide = new Fragment_guide();
                    transaction.add(R.id.id_content, fragment_guide, Fragment_guide.class.getName());
                } else {
                    transaction.show(fragment_guide);
                }
                //transaction.add(R.id.id_content, fragment_guide,Fragment_guide.class.getName());
                break;
            default:
                break;
        }
        //  transaction.addToBackStack(null);
        transaction.commit();// 提交事务
    }
    /*
     * 隐藏所有的Fragment
     */
    protected void hideFragment(FragmentTransaction transaction) {
        if (fragment_home != null) {
            transaction.hide(fragment_home);
        }
        if (fragment_address != null) {
            transaction.hide(fragment_address);
        }
        if (fragment_guide != null) {
            transaction.hide(fragment_guide);
        }
        if (fragment_buSu != null) {
            transaction.hide(fragment_buSu);
        }
    }

}
