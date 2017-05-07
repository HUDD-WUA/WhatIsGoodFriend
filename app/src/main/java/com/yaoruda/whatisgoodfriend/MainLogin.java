package com.yaoruda.whatisgoodfriend;

/**
 * Created by yaoruda on 2017/5/5.
 */
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import java.io.PipedReader;
import java.io.PipedWriter;

public class MainLogin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_main);
        final TextView textView_signup = (TextView)findViewById(R.id.text_sign_up);
        textView_signup.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent_signup = new Intent(MainLogin.this, MainActivity.class);
                startActivity(intent_signup);
            }
        });
        textView_signup.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                textView_signup.setTextColor(getResources().getColor(R.color.signup_selected));
                if (event.getAction() == MotionEvent.ACTION_BUTTON_RELEASE){
                    textView_signup.setTextColor(getResources().getColor(R.color.black));
                }
                return false;
            }
        });
        textView_signup.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus == false){
                    textView_signup.setTextColor(getResources().getColor(R.color.black));
                }
            }
        });
        Button button_login = (Button)findViewById(R.id.button_login);
        final EditText editText_name = (EditText)findViewById(R.id.editText_name);
        final EditText editText_password = (EditText)findViewById(R.id.editText_password);

        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user_name = editText_name.getText().toString();
                String user_password = editText_password.getText().toString();
                String response_password = sendRequest_login(user_name);

                if (!response_password.equals("null")) {
                    if (user_password.equals(response_password)) {
                        Intent it = new Intent(MainLogin.this, MainActivity.class);
                        it.putExtra("user_name", user_name);
                        startActivity(it);
                    } else {
                        Toast.makeText(MainLogin.this, "密码错误！", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(MainLogin.this, "用户名错误！", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private String sendRequest_login(final String user_name) {
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
                    org.apache.http.client.HttpClient httpClient = new org.apache.http.impl.client.DefaultHttpClient();
                    HttpGet httpGet = new HttpGet("http://10.0.2.2:8080/whatIsGoodFriend/Login?name="+user_name);
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode() == 200){
                        HttpEntity entity = httpResponse.getEntity();
                        String response = EntityUtils.toString(entity, "utf-8");

                        Gson gson = new Gson();
                        String password = gson.fromJson(response, String.class);
                        if (password != null) {
                            Log.d("getjson", password);
                            pipedWriter.write(password);
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
            String password = new String(buf, 0, len);
            Log.d("LoginReadBuffer", password);
            return password;

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static void setClickableTextView(Context context, TextView textView, Intent intent) {
        String text = textView.getText().toString();
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        MyClickableSpan myClickableSpan = new MyClickableSpan(context, intent);
        builder.setSpan(myClickableSpan, 0, text.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(builder);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
