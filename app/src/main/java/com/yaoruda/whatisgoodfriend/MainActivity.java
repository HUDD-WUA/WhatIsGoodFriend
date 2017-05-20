package com.yaoruda.whatisgoodfriend;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;



public class MainActivity extends AppCompatActivity {
    private static final String ThisClass = MainActivity.class.getSimpleName();
    public class SDKReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String IntentAction = intent.getAction();
            Log.d(ThisClass, "action: " + IntentAction);
            TextView text = (TextView) findViewById(R.id.text_Info);
            text.setTextColor(Color.BLACK);
            if (IntentAction.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                text.setText("key 验证出错! 错误码 :" + intent.getIntExtra
                        (SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE, 0)
                        + " ; 请在 AndroidManifest.xml 文件中检查 key 设置");
            } else if (IntentAction.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK)) {
                text.setText("key 验证成功! 功能可以正常使用");
                text.setTextColor(Color.BLACK);
            } else if (IntentAction.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
                text.setText("网络出错, 请检查网络设置!");
            }
        }
    }

    private SDKReceiver mReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        setContentView(R.layout.activity_main);
        //获取地图控件引用
        //mMapView = (MapView) findViewById(R.id.bmapView);
        ListView mListView = (ListView) findViewById(R.id.listView);
        // 添加ListItem，设置事件响应
        mListView.setAdapter(new ListAdapter(this));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View v, int index,
                                    long arg3) {
                onListItemClick(index);
            }
        });

        // 注册 SDK 广播监听者
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        mReceiver = new SDKReceiver();
        registerReceiver(mReceiver, iFilter);

        //开启服务（向后台定时传送自己的坐标)
        Bundle bundle = this.getIntent().getExtras();
        String user_name = bundle.getString("user_name");
        Intent intent = new Intent(getApplicationContext(), AlarmService.class);
        intent.putExtra("user_name", user_name);
        startService(intent);

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
    }

    void onListItemClick(int index) {
        Intent intent;
        intent = new Intent(MainActivity.this, itemdata[index].linkclass);
        this.startActivity(intent);
    }

    private static final ItemData_class[] itemdata = {
            new ItemData_class(R.string.item_title_mainmap, R.string.item_text_mainmap, MainMap.class),
            new ItemData_class(R.string.item_title_mainmap, R.string.item_text_mainmap, MainMap.class),
    };

    private class ListAdapter extends BaseAdapter {
        public ListAdapter() {
            super();
        }
        private LayoutInflater mInflater; //得到一个LayoutInfalter对象用来导入布局

        public ListAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int index, View convertView, ViewGroup parent) {


            ViewHolder holder;
            Log.v("BaseAdapterTest", "getView " + index + " " + convertView);

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.main_list_item_info, null);
                holder = new ViewHolder();
                /*得到各个控件的对象*/
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.text = (TextView) convertView.findViewById(R.id.text);

                convertView.setTag(holder); //绑定ViewHolder对象
            }
            else {
                holder = (ViewHolder) convertView.getTag(); //取出ViewHolder对象
            }

            /*设置TextView显示的内容，即我们存放在动态数组中的数据*/
            holder.title.setText(itemdata[index].title);
            holder.text.setText(itemdata[index].text);

            if (index >= 25) {
                holder.title.setTextColor(Color.YELLOW);
            }
            return convertView;
        }

        @Override
        public int getCount() {
            return itemdata.length;
        }

        @Override
        public Object getItem(int index) {
            return itemdata[index];
        }

        @Override
        public long getItemId(int id) {
            return id;
        }
    }


    private static class ItemData_class {
        private final int title;
        private final int text;
        private final Class<? extends Activity> linkclass;

        public ItemData_class(int title, int desc,
                        Class<? extends Activity> demoClass) {
            this.title = title;
            this.text = desc;
            this.linkclass = demoClass;
        }
    }

    private static class ViewHolder {
        private TextView title;
        private TextView text;
        private Class<? extends Activity> linkclass;

        public ViewHolder() {}
    }

}