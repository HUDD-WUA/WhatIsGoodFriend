package com.yaoruda.whatisgoodfriend;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Toast;

import com.baidu.mapapi.http.HttpClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.StringReader;
import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Exchanger;

/**
 * Created by yaoruda on 2017/4/30.
 */

public class MainMap extends AppCompatActivity {

    /**
     * MapView 是地图主控件
     */
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private Marker mMarkerA;
    private InfoWindow mInfoWindow;
    private LocationManager locationManager;
    private String provider;
    private boolean isFirstLocate = true;

    String friend_name;//朋友名称
    String user_name;//本用户
    // 初始化全局 bitmap 信息，不用时及时 recycle
    BitmapDescriptor bdA = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_gcoding);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_map);
        //加载地图
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(13.0f);
        mBaiduMap.setMapStatus(msu);
        //设置点击mark的默认动作
        /*mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            public boolean onMarkerClick(final Marker marker) {
                /*LatLng ll = marker.getPosition();
                float mapZoom = mMapView.getMapLevel();
                MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(mapZoom);

                Button button = new Button(getApplicationContext());
                button.setBackgroundResource(R.drawable.popup);
                InfoWindow.OnInfoWindowClickListener listener = null;
                if (marker == mMarkerA) {
                    button.setText("更改位置");
                    button.setTextColor(Color.BLACK);
                    button.setWidth(300);
                    MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(18.0f);
                    mBaiduMap.setMapStatus(msu);
                    listener = new InfoWindow.OnInfoWindowClickListener() {
                        public void onInfoWindowClick() {
                            LatLng ll = marker.getPosition();
                            LatLng llNew = new LatLng(ll.latitude + 0.005,
                                    ll.longitude + 0.005);
                            marker.setPosition(llNew);
                            mBaiduMap.hideInfoWindow();
                        }
                    };
                    LatLng ll = marker.getPosition();
                    mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(button), ll, -47, listener);
                    mBaiduMap.showInfoWindow(mInfoWindow);
                } else if (marker == mMarkerB) {
                    button.setText("更改图标");
                    button.setTextColor(Color.BLACK);
                    button.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            marker.setIcon(bd);
                            mBaiduMap.hideInfoWindow();
                        }
                    });
                    LatLng ll = marker.getPosition();
                    mInfoWindow = new InfoWindow(button, ll, -47);
                    mBaiduMap.showInfoWindow(mInfoWindow);
                } else if (marker == mMarkerC) {
                    button.setText("删除");
                    button.setTextColor(Color.BLACK);
                    button.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            marker.remove();
                            mBaiduMap.hideInfoWindow();
                        }
                    });
                    LatLng ll = marker.getPosition();
                    mInfoWindow = new InfoWindow(button, ll, -47);
                    mBaiduMap.showInfoWindow(mInfoWindow);
                }
                return true;
            }
        });*/
        //获取参数并判断具体展示功能
        Bundle bundle = this.getIntent().getExtras();
        friend_name = bundle.getString("friend_name");//从friend界面获取具体某个朋友的名称
        user_name = bundle.getString("user_name");
        if (friend_name == null && user_name != null) {//显示自己位置
            //定位并显示自己的位置
            if (Build.VERSION.SDK_INT >= 23)
                if(MainMap.this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "No Location permission!", Toast.LENGTH_SHORT).show();
                }

            locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            //获取位置提供器
            List<String> providerList = locationManager.getProviders(true);
            if (providerList.contains(LocationManager.GPS_PROVIDER)){
                provider = LocationManager.GPS_PROVIDER;
            } else if (providerList.contains((LocationManager.NETWORK_PROVIDER))) {
                provider = LocationManager.NETWORK_PROVIDER;
            }else {
                Toast.makeText(this, "No Location provider to use!", Toast.LENGTH_SHORT).show();
                return;
            }
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                setLocation(location);
                setLocationInBaiduMap(location);
            }
            locationManager.requestLocationUpdates(provider, 1000, 0, locationListener);
        }else if (friend_name != null && user_name == null) {//显示某个朋友的位置
            Double[] friend_location = getFriendLocation(friend_name);
            if (friend_location[0] == 0.0){
                new AlertDialog.Builder(MainMap.this).setTitle("没有\""+friend_name+"\"的位置信息！")
                        .setMessage("请让\""+friend_name+"\"也运行此应用，您们就可以相互看到彼此的位置了！")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).show();
            }
            initOverlay(friend_location);
        }


    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            setLocation(location);
            setLocationInBaiduMap(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void setLocation(Location location){
        Log.d("Location", location.getLatitude() + "," + location.getLongitude());
    }

    private void setLocationInBaiduMap(Location location){
        if (isFirstLocate){
            //获取自己位置并移动到自己位置
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(update);
            //update = MapStatusUpdateFactory.zoomTo(16f);
            //mBaiduMap.animateMapStatus(update);

            //MyLocation方法(小圆点)
            MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
            locationBuilder.latitude(location.getLatitude());
            locationBuilder.longitude(location.getLongitude());
            MyLocationData locationData = locationBuilder.build();
            mBaiduMap.setMyLocationData(locationData);

            isFirstLocate = false;
        }
    }

    public void initOverlay(Double[] location) {
        // add marker overlay
        LatLng llA = new LatLng(location[0], location[1]);
        Log.d("la",location[0].toString());
        Log.d("lo",location[1].toString());
        LatLng llB = new LatLng(39.942821, 116.369199);
        LatLng llC = new LatLng(39.939723, 116.425541);
        LatLng llD = new LatLng(39.906965, 116.401394);

        //sendRequestWithHttpClient();

        MarkerOptions ooA = new MarkerOptions().position(llA).icon(bdA)
                .zIndex(9).draggable(true);
        /*if (animationBox.isChecked()) {
            // 掉下动画
            ooA.animateType(MarkerOptions.MarkerAnimateType.drop);
        }*/
        mMarkerA = (Marker) (mBaiduMap.addOverlay(ooA));
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngZoom(llA,16.0f);
        mBaiduMap.setMapStatus(msu);
        /*MarkerOptions ooB = new MarkerOptions().position(llB).icon(bdB)
                .zIndex(5);
        if (animationBox.isChecked()) {
            // 掉下动画
            ooB.animateType(MarkerOptions.MarkerAnimateType.drop);
        }
        mMarkerB = (Marker) (mBaiduMap.addOverlay(ooB));
        MarkerOptions ooC = new MarkerOptions().position(llC).icon(bdC)
                .perspective(false).anchor(0.5f, 0.5f).rotate(30).zIndex(7);
        if (animationBox.isChecked()) {
            // 生长动画
            ooC.animateType(MarkerOptions.MarkerAnimateType.grow);
        }
        mMarkerC = (Marker) (mBaiduMap.addOverlay(ooC));
        ArrayList<BitmapDescriptor> giflist = new ArrayList<BitmapDescriptor>();
        giflist.add(bdA);
        giflist.add(bdB);
        giflist.add(bdC);
        MarkerOptions ooD = new MarkerOptions().position(llD).icons(giflist)
                .zIndex(0).period(10);
        if (animationBox.isChecked()) {
            // 生长动画
            ooD.animateType(MarkerOptions.MarkerAnimateType.grow);
        }
        mMarkerD = (Marker) (mBaiduMap.addOverlay(ooD));

        // add ground overlay
        LatLng southwest = new LatLng(39.92235, 116.380338);
        LatLng northeast = new LatLng(39.947246, 116.414977);
        LatLngBounds bounds = new LatLngBounds.Builder().include(northeast)
                .include(southwest).build();

        OverlayOptions ooGround = new GroundOverlayOptions()
                .positionFromBounds(bounds).image(bdGround).transparency(0.8f);
        mBaiduMap.addOverlay(ooGround);

        MapStatusUpdate u = MapStatusUpdateFactory
                .newLatLng(bounds.getCenter());
        mBaiduMap.setMapStatus(u);

        mBaiduMap.setOnMarkerDragListener(new BaiduMap.OnMarkerDragListener() {
            public void onMarkerDrag(Marker marker) {
            }

            public void onMarkerDragEnd(Marker marker) {
                Toast.makeText(
                        MainMap.this,
                        "拖拽结束，新位置：" + marker.getPosition().latitude + ", "
                                + marker.getPosition().longitude,
                        Toast.LENGTH_LONG).show();
            }

            public void onMarkerDragStart(Marker marker) {
            }
        });
    }
*/
    /*
    public void clearOverlay(View view) {
        mBaiduMap.clear();
        mMarkerA = null;
        mMarkerB = null;
        mMarkerC = null;
        mMarkerD = null;
    }

    public void resetOverlay(View view) {
        clearOverlay(null);
        initOverlay();
    }

    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            // TODO Auto-generated method stub
            float alpha = ((float) seekBar.getProgress()) / 10;
            if (mMarkerA != null) {
                mMarkerA.setAlpha(alpha);
            }
            if (mMarkerB != null) {
                mMarkerB.setAlpha(alpha);
            }
            if (mMarkerC != null) {
                mMarkerC.setAlpha(alpha);
            }
            if (mMarkerD != null) {
                mMarkerD.setAlpha(alpha);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub
        }

    }
*/
    }

    @Override
    protected void onPause() {
        // MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        // MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
        mMapView.onDestroy();
        super.onDestroy();
        // 回收 bitmap 资源
        bdA.recycle();
        if (Build.VERSION.SDK_INT >= 23) {
            if (MainMap.this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "No Location permission!", Toast.LENGTH_SHORT).show();
            }
        }
        mBaiduMap.setMyLocationEnabled(false);
        if (locationManager != null){
            locationManager.removeUpdates(locationListener);
        }
        /*bdB.recycle();
        bdC.recycle();
        bdD.recycle();
        bd.recycle();
        bdGround.recycle();*/
    }
    public Double[] getFriendLocation(final String friend_name) {
        final PipedWriter pipedWriter = new PipedWriter();
        PipedReader pipedReader = new PipedReader();
        try {
            pipedWriter.connect(pipedReader);

        }catch (Exception e){
            e.printStackTrace();
        }

        //建立连接管道
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    org.apache.http.client.HttpClient httpClient = new org.apache.http.impl.client.DefaultHttpClient();
                    HttpGet httpGet = new HttpGet("http://" +
                            "10.0.2.2:8080/whatIsGoodFriend/GetFriendLocation?friend="+friend_name);
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode() == 200){
                        HttpEntity entity = httpResponse.getEntity();
                        String res_friend = EntityUtils.toString(entity, "utf-8");
                        if (res_friend != null) {
                            Log.d("getjson", res_friend);
                            pipedWriter.write(res_friend);
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
            Double[] friend_location = new Double[2];
            //解析json数组
            Gson gson = new Gson();
            int arraySize;
            if(response.equals(""))
            {
                return null;
            }
            System.out.println(response);

            JsonParser parser = new JsonParser();//parse用于从一个字符串中解析出json对象
            JsonElement element = parser.parse(response);//解析出json对象
            JsonArray jsonArray = null;
            if (element.isJsonArray())

            {//还可以是其他的很多种类型，不过这里肯定是JsonArray，以往万一判断了一下
                jsonArray = element.getAsJsonArray();//这里就已经是真正的json数组了
                arraySize = jsonArray.size();//json数组就可以获取数量了
                System.out.println(arraySize);
            }

            Iterator it = jsonArray.iterator();//使用迭代器来获取json数组内容
            for (
                    int i = 0;
                    it.hasNext() == true; i++)

            {//读取所有信息
                JsonElement e = (JsonElement) it.next();//第一次是指向最开始的之前的，所以第一次也会先next一次
                friend_location[i] = gson.fromJson(e, Double.class);//每次获取一个对象保存
                System.out.println("Location name: " + friend_location[i]);
            }
            return friend_location;

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
