package com.yaoruda.whatisgoodfriend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by yaoruda on 2017/5/13.
 */

public class AlarmReceive extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //循环启动Service
        Bundle bundle = intent.getExtras();
        final String user_name = bundle.getString("user_name");
        Intent i = new Intent(context, AlarmService.class);
        i.putExtra("user_name", user_name);
        context.startService(i);
    }
}