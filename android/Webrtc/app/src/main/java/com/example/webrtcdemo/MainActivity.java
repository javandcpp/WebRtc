package com.example.webrtcdemo;

import androidx.appcompat.app.AppCompatActivity;
import tcp.TcpClientManager;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {

        findViewById(R.id.btnConnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String ip=((EditText) findViewById(R.id.etServer)).getText().toString().trim();
                String port=((EditText) findViewById(R.id.etPort)).getText().toString().trim();
                final int portip=Integer.parseInt(port);
                TcpClientManager.getSingleInstance().initSocket(ip,portip);
            }
        });


    }
}
