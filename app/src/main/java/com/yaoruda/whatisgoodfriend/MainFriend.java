package com.yaoruda.whatisgoodfriend;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.ArrayList;
import java.util.Iterator;


public class MainFriend extends AppCompatActivity {

    String friends[];
    int arraySize = 0;
    ItemData_class[] itemdata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        setContentView(R.layout.friend_main);
        //获取地图控件引用
        //mMapView = (MapView) findViewById(R.id.bmapView);
        //调用接口并解析json数组
        Gson gson = new Gson();
        String response = getFriend("yaoruda");//获取到String类型的json数组
        if (response.equals("")){

        }
        System.out.println(response);
        JsonParser parser = new JsonParser();//parse用于从一个字符串中解析出json对象
        JsonElement element = parser.parse(response);//解析出json对象
        JsonArray jsonArray = null;
        if (element.isJsonArray()) {//还可以是其他的很多种类型，不过这里肯定是JsonArray，以往万一判断了一下
            jsonArray = element.getAsJsonArray();//这里就已经是真正的json数组了
            arraySize = jsonArray.size();//json数组就可以获取数量了
            System.out.println(arraySize);
        }
        friends = new String[arraySize];//根据刚才得到的size创建对象数组
        Iterator it = jsonArray.iterator();//使用迭代器来获取json数组内容
        for (int i=0; it.hasNext()==true; i++) {//读取所有信息，一次是一个大小的flowerDataStyle.class
            JsonElement e = (JsonElement) it.next();//第一次是指向最开始的之前的，所以第一次也会先next一次
            friends[i] = gson.fromJson(e, String.class);//每次获取一个对象保存
            System.out.println("Friend name: "+friends[i]);
        }
        itemdata = new ItemData_class[arraySize];
        for (int i=0; i<arraySize; i++) {
            itemdata[i] = new ItemData_class(friends[i]);
        }
        // 添加ListItem，设置事件响应
        ListView mListView = (ListView) findViewById(R.id.listView_f);
        mListView.setAdapter(new ListAdapter(this));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View v, int index,
                                    long arg3) {
                onListItemClick(index);
            }
        });


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
        intent = new Intent(MainFriend.this, MainMap.class);
        intent.putExtra("name", itemdata[index].name);
        this.startActivity(intent);
    }

    private class ListAdapter extends BaseAdapter {
        public ListAdapter() {
            super();
        }
        private LayoutInflater mInflater; //得到一个LayoutInfalter对象用来导入布局

        public ListAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(final int index, View convertView, ViewGroup parent) {


            ViewHolder holder;
            Log.v("BaseAdapterTest", "getView " + index + " " + convertView);

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.friend_list_item_info, null);
                holder = new ViewHolder();
                /*得到各个控件的对象*/
                holder.title = (TextView) convertView.findViewById(R.id.name);
                holder.button = (Button) convertView.findViewById(R.id.delete);
                convertView.setTag(holder); //绑定ViewHolder对象
            }
            else {
                holder = (ViewHolder) convertView.getTag(); //取出ViewHolder对象
            }

            /*设置TextView显示的内容，即我们存放在动态数组中的数据*/
            View.OnClickListener for_delete_button = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //deleteFriend(itemdata[index].name);
                    //之后刷新页面!
                    Log.d("Button", "is pressed!");
                }
            };
            holder.title.setText(itemdata[index].name);
            holder.button.setOnClickListener(for_delete_button);

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
        private final String name;
        //private final Class<? extends Button> linkButton;

        public ItemData_class(String name) {
            this.name = name;
          //  this.linkButton = demoButton;
        }
    }

    private static class ViewHolder {
        private TextView title;
        private Button button;

        public ViewHolder() {}
    }

    private String getFriend(final String user_name) {
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
                            "10.0.2.2:8080/whatIsGoodFriend/GetFriend?name="+user_name);
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
            String res_friend = new String(buf, 0, len);
            Log.d("LoginReadBuffer", res_friend);
            return res_friend;

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}