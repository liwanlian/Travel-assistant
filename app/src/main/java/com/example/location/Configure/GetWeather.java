package com.example.location.Configure;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.location.MyFragment.Fragment_home;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.UnsupportedEncodingException;

public class GetWeather  extends Activity {
    //天气（根据城市 显示相应的天气情况）
    private static final String NAMESPACE = "http://WebXml.com.cn/";
    // WebService地址
    private static String URL ="http://www.webxml.com.cn/webservices/weatherwebservice.asmx";
    private static final String METHOD_NAME = "getWeatherbyCityName";
    private static String SOAP_ACTION ="http://WebXml.com.cn/getWeatherbyCityName";

    private String weatherToday;
    private SoapObject detail;
    private String weatherNow;
    String date;


    String cityname;

    String huitiao;
    int flag=0;
    public GetWeather(final String cityname){
        this.cityname=cityname;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SoapObject rpc = new SoapObject(NAMESPACE, METHOD_NAME);
                    rpc.addProperty("theCityName", cityname);
                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                            SoapEnvelope.VER11);
                    envelope.bodyOut = rpc;
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(rpc);
                    HttpTransportSE ht = new HttpTransportSE(URL);
                    ht.debug = true;
                    ht.call(SOAP_ACTION, envelope);
                    detail = (SoapObject) envelope.getResponse();
//                    parseWeather(detail);
                    Message msg=new Message();
                    msg.obj=detail;
                    handler1.sendMessage(msg);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();


    }
    Handler handler1 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String de = (String)msg.obj;
//           setHuitiao("ggggg");
            cityname="kkkkk";
            flag=1;
            //  System.out.println("dede="+de);
            Intent intent=new Intent();
            intent.putExtra("weather",cityname);
            intent.setAction("com.example.location.getweatherdata");
            sendBroadcast(intent);
        }
    };
    public String setwe(String data){
        String result;
        result=data;
        return result;
    }
    public String getHuitiao() {
        return huitiao;
    }

    public void setHuitiao(String huitiao) {
        this.huitiao = huitiao;
    }

    public void getWeather(String cityName) {
        try {
            SoapObject rpc = new SoapObject(NAMESPACE, METHOD_NAME);
            rpc.addProperty("theCityName", cityName);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.bodyOut = rpc;
            envelope.dotNet = true;
            envelope.setOutputSoapObject(rpc);
            HttpTransportSE ht = new HttpTransportSE(URL);
            ht.debug = true;
            ht.call(SOAP_ACTION, envelope);
            detail = (SoapObject) envelope.getResponse();
            parseWeather(detail);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void parseWeather(SoapObject detail)
            throws UnsupportedEncodingException {
         date = detail.getProperty(6).toString();
        weatherToday = "\n天气：" + date.split(" ")[1];
        weatherToday = weatherToday + "\n气温："
                + detail.getProperty(5).toString();
        weatherToday = weatherToday + "\n风力："
                + detail.getProperty(7).toString() + "\n";
        weatherNow = detail.getProperty(8).toString();

        System.out.println("wea="+weatherNow);

       // tv_weather.setText(weatherNow);
    }
    public String sendweatherdata(String msg_weather){
        String result;
        getWeather("北京");
        result=date;
        return  result;
    }
}
