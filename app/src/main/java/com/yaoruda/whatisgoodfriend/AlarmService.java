package com.yaoruda.whatisgoodfriend;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.List;

/**
 * Created by yaoruda on 2017/5/13.
 */

/**
 * 一个定时任务
 */

public class AlarmService extends Service {

    /**
     * 每1分钟更新一次数据
     */
    private static final int ONE_Miniute = 5 * 1000;
    private static final int PENDING_REQUEST = 0;//标识PendingIntent

    private LocationManager locationManager;
    private String provider;

    public AlarmService() {
    }

    /**
     * 调用Service都会执行到该方法
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        final String user_name = bundle.getString("user_name"); //用于接收字符串
        //这里模拟后台操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("AlarmService", "循环执行" + System.currentTimeMillis());
                Location location = getLocation();
                double la = location.getLatitude();
                double lo = location.getLongitude();
                Log.d("LocationInService", "La:"+la);
                Log.d("LocationInService", "Lo:"+lo);
                String res = sendLocation(location, user_name);
                if (res.equals("null")){
                    Log.d("SetLocationError", "Return null");
                }
            }
        }).start();

        //通过AlarmManager定时启动广播
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtTime = SystemClock.elapsedRealtime() + ONE_Miniute;//从开机到现在的毫秒书（手机睡眠(sleep)的时间也包括在内
        Intent i = new Intent(this, AlarmReceive.class);//设置广播
        i.putExtra("user_name", user_name);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, PENDING_REQUEST, i, PENDING_REQUEST);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pIntent);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public Location getLocation() {
        //定位部分
        if (Build.VERSION.SDK_INT >= 23)
            if (AlarmService.this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "No Location permission!", Toast.LENGTH_SHORT).show();
            }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //获取位置提供器
        List<String> providerList = locationManager.getProviders(true);
        if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (providerList.contains((LocationManager.NETWORK_PROVIDER))) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            Toast.makeText(this, "No Location provider to use!", Toast.LENGTH_SHORT).show();
            return null;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            return location;
        }
        return null;
    }

    public String sendLocation(final Location location, final String user_name){
        final PipedWriter pipedWriter = new PipedWriter();
        PipedReader pipedReader = new PipedReader();
        try {
            pipedWriter.connect(pipedReader);

        }catch (Exception e){
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Double location_la = location.getLatitude();
                    Double location_lo = location.getLongitude();
                    org.apache.http.client.HttpClient httpClient = new org.apache.http.impl.client.DefaultHttpClient();
                    HttpGet httpGet = new HttpGet("http://10.0.2.2:8080/whatIsGoodFriend/SetLocation?name="+user_name+"&location_la="+location_la+"&location_lo="+location_lo);
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode() == 200){
                        HttpEntity entity = httpResponse.getEntity();
                        String response = EntityUtils.toString(entity, "utf-8");

                        Gson gson = new Gson();
                        String res = gson.fromJson(response, String.class);
                        if (!res.equals("null")) {
                            Log.d("SetLocation_response", res);
                            pipedWriter.write(res);
                        }else{
                            pipedWriter.write("null");
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

        try {
            char[] buf = new char[2048];
            int len = pipedReader.read(buf);
            String response = new String(buf, 0, len);
            return response;

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}

