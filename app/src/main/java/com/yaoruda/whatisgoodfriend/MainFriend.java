package com.yaoruda.whatisgoodfriend;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.EventLog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
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
import java.util.Iterator;
import java.util.StringTokenizer;


public class MainFriend extends AppCompatActivity {

    String friends[];
    String user_name;
    int arraySize = 0;
    ItemData_class[] itemdata;
    ListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle bundle = this.getIntent().getExtras();
        user_name = bundle.getString("user_name");
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        setContentView(R.layout.friend_main);
        setCustomActionBar();
        //获取地图控件引用
        //mMapView = (MapView) findViewById(R.id.bmapView);
        Button button_back = (Button)findViewById(R.id.button_back);
        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Button add_friend_button = (Button) findViewById(R.id.button_addFriend);
        add_friend_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog();
            }
        });
        // 添加ListItem，设置事件响应
        ListView mListView = (ListView) findViewById(R.id.listView_f);
        mListView.setDivider(new ColorDrawable(getResources().getColor(R.color.item_divider)));
        mListView.setDividerHeight(1);
        listAdapter = new ListAdapter(this);
        mListView.setAdapter(listAdapter);
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

    void onListItemClick(int index) { //点击某个item激活的方法
        Intent intent;
        intent = new Intent(MainFriend.this, MainMap.class);
        intent.putExtra("friend_name", itemdata[index].name);
        this.startActivity(intent);
    }

    private class ListAdapter extends BaseSwipeAdapter {
        public ListAdapter() {
            super();
        }
        private LayoutInflater mInflater; //得到一个LayoutInfalter对象用来导入布局

        public ListAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
            getFriendItem();
        }

        @Override
        public void fillValues(int index, View convertView) {

            /*得到各个控件的对象*/
            TextView title = (TextView) convertView.findViewById(R.id.name);
            title.setText(itemdata[index].name);
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

        @Override
        public View generateView(final int position, ViewGroup parent) {
            // TODO Auto-generated method stub
            View v = mInflater.inflate(R.layout.friend_list_item_info, null);
            final SwipeLayout swipeLayout = (SwipeLayout) v.findViewById(R.id.message_center_item_swipe);
            v.findViewById(R.id.message_center_item_delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteFriend(user_name, itemdata[position].name);
                    String name2 = itemdata[position].name;
                    swipeLayout.close();
                    getFriendItem();
                    listAdapter.notifyDataSetChanged();
                    new AlertDialog.Builder(MainFriend.this).setTitle("删除\""+name2+"\"成功")
                            .setMessage("现在\'"+name2+"\"与您停止共享位置，并且您的名字会从\""+name2+"\"的好友列表中消失。")
                            .setPositiveButton("确定", null).show();
                }
            });
            return v;
        }
        @Override
        public int getSwipeLayoutResourceId(int position) {
            // TODO Auto-generated method stub
            return R.id.message_center_item_swipe;
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

    private String deleteFriend(final String user_name, final String friend_name) {
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
                            "10.0.2.2:8080/whatIsGoodFriend/DeleteFriend?name="+user_name+"&friend="+friend_name);
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
            return res_friend;

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private String addFriend(final String user_name, final String friend_name) {
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
                            "10.0.2.2:8080/whatIsGoodFriend/AddFriend?name="+user_name+"&friend="+friend_name);
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
            return res_friend;

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void getFriendItem() {
        //调用接口并解析json数组
        Gson gson = new Gson();
        String response = getFriend("yaoruda");//获取到String类型的json数组
        if(response.equals(""))
        {
            return;
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

        friends = new String[arraySize];//根据刚才得到的size创建对象数组
        Iterator it = jsonArray.iterator();//使用迭代器来获取json数组内容
        for (
                int i = 0;
                it.hasNext() == true; i++)

        {//读取所有信息
            JsonElement e = (JsonElement) it.next();//第一次是指向最开始的之前的，所以第一次也会先next一次
            friends[i] = gson.fromJson(e, String.class);//每次获取一个对象保存
            System.out.println("Friend name: " + friends[i]);
        }

        itemdata = new ItemData_class[arraySize];
        for (
                int i = 0;
                i < arraySize; i++)

        {
            itemdata[i] = new ItemData_class(friends[i]);
        }
    }
    private void setCustomActionBar() {
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        View mActionBarView = LayoutInflater.from(this).inflate(R.layout.friend_action_bar, null);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(mActionBarView, lp);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
    }
    protected void dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainFriend.this);
        builder.setMessage("请输入朋友的账号");

        builder.setTitle("添加朋友");
        final EditText editText = new EditText(this);
        builder.setView(editText);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String friend_name = editText.getText().toString();
                if (friend_name.equals("")){
                    dialog();
                    Toast.makeText(MainFriend.this, "没有这个朋友账号，请重新输入！", Toast.LENGTH_SHORT).show();
                }else{
                    Gson gson = new Gson();
                    String response = addFriend(user_name, friend_name);
                    String result = gson.fromJson(response, String.class);//每次获取一个对象保存
                    Log.d("addFriend",result);
                    if ("success".equals(result)){
                        getFriendItem();
                        listAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                        new AlertDialog.Builder(MainFriend.this).setTitle("添加\""+friend_name+"\"成功")
                                .setMessage("注意只有\""+friend_name+"\"也添加您为他的好友，您们才可以在朋友列表中看到彼此！")
                                .setPositiveButton("确定", null).show();
                    }else if(result.equals("No Found")){
                        dialog();
                        Toast.makeText(MainFriend.this, "没有查询到\""+friend_name+"\"，请重新输入！", Toast.LENGTH_LONG).show();
                    }else if(result.equals("repetitive")){
                        dialog();
                        new AlertDialog.Builder(MainFriend.this).setTitle("您早已添加过\""+friend_name+"\"！")
                                .setMessage("请让\""+friend_name+"\"也添加您为朋友，您们就可以在朋友列表中看到彼此了！")
                                .setPositiveButton("确定", null).show();
                    }else{
                        dialog();
                        Toast.makeText(MainFriend.this, "因为某种未知原因，添加失败，请重新尝试！", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }
}