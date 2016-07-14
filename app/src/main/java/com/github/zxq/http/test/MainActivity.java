package com.github.zxq.http.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.zxq.httpclient.BaseZXQTask;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView txt = (TextView) findViewById(R.id.txt);

        new BaseZXQTask.Builder(this)
                .setHttpType("POST")
                .addReuqestData("auth_id", "15900212212")
                .addReuqestData("activation_code", "123231")
                .addRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                .addRequestProperty("Accept", "application/json")
                .setIsShowDialog(true)
                .setMessage("正在加载数据")
                .setRequestTimeout(60 * 1000)
                .setCallback(new BaseZXQTask.HttpResult() {
                    @Override
                    public void onResultSuccess(String s) {
                        txt.setText(s);
                    }

                    @Override
                    public void onResultFailure(int code, String msg) {
                        txt.setText("code:" + code + "\n msg:" + msg);
                    }
                })
                .request("http://10.7.7.100:3000/api/activation");
    }
}
