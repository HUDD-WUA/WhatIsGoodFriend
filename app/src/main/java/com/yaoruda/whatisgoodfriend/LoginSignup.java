package com.yaoruda.whatisgoodfriend;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.io.PipedReader;
import java.io.PipedWriter;

/**
 * Created by yaoruda on 2017/5/8.
 */
public class LoginSignup extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_signup);
        final EditText editText_name = (EditText) findViewById(R.id.editText_name);
        final EditText editText_password = (EditText) findViewById(R.id.editText_password);
                Button button = (Button) findViewById(R.id.button_signup);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = editText_name.getText().toString();
                        String password = editText_password.getText().toString();
                        if (name.equals("")) {
                            Toast.makeText(LoginSignup.this, getResources().getText(R.string.fail_signup_null_name), Toast.LENGTH_SHORT).show();
                            return;
                        } else if (password.equals("")) {
                            Toast.makeText(LoginSignup.this, getResources().getText(R.string.fail_signup_null_password), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        char[] pswd = password.toCharArray();
                        for (int i = 0; i < password.length(); i++) {
                            if (!((pswd[i] > '0' && pswd[i] < '9') || (pswd[i] > 'a' && pswd[i] < 'z') || (pswd[i] > 'A' && pswd[i] < 'Z'))) {
                                Toast.makeText(LoginSignup.this, getResources().getText(R.string.fail_signup_wrong_password), Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        String response = sendRequest_signup(name, password);

                if (response == null) {
                    Log.d("Sign Up Error!", "http response is null");
                    return;
                }
                switch (response) {
                    case "success": {
                        Toast.makeText(LoginSignup.this, getResources().getText(R.string.signup_success), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(LoginSignup.this, MainActivity.class);
                        intent.putExtra("user_name", name);
                        startActivity(intent);
                        break;
                    }
                    case "repetitive": {
                        Toast.makeText(LoginSignup.this, getResources().getText(R.string.fail_signup_repetitive_name), Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case "fail": {
                        Log.d("Sign Up Error!", "http response is unknown");
                        break;
                    }
                }
            }
        });
    }

    private String sendRequest_signup(final String user_name, final String password) {
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
                    HttpGet httpGet = new HttpGet("http://10.0.2.2:8080/whatIsGoodFriend/SignUp?name="+user_name+"&"+"password="+password);
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode() == 200){
                        HttpEntity entity = httpResponse.getEntity();
                        String response = EntityUtils.toString(entity, "utf-8");

                        Gson gson = new Gson();
                        String res = gson.fromJson(response, String.class);
                        if (res != null) {
                            Log.d("getjson(signup)", res);
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
            Log.d("LoginReadBuffer(signup)", response);
            return response;

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}