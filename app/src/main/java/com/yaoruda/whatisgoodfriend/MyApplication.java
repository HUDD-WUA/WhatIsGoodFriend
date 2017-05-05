package com.yaoruda.whatisgoodfriend;

import android.app.Application;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;

/**
 * Created by yaoruda on 2017/4/28.
 */

public class MyApplication extends Application {


    @Override
    public void onCreate() {
//初始化百度地图SDK
        super.onCreate();
        SDKInitializer.initialize(this);
        SDKInitializer.setCoordType(CoordType.BD09LL);

    }
}