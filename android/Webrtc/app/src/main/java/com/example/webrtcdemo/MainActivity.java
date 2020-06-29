package com.example.webrtcdemo;

import androidx.appcompat.app.AppCompatActivity;
import tcp.TcpClient;
import tcp.TcpClientManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

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
//                TcpClientManager.getSingleInstance().initSocket(ip,portip);
                TcpClient.instance().connect(ip, Integer.parseInt(port), new TcpClient.TCPClientEventListener() {
                    @Override
                    public void recvMsg(ByteArrayOutputStream read) {
                        Log.e("recv","RECV Message:"+read.size());
                        ByteBuffer byteBuffer= ByteBuffer.wrap(read.toByteArray());
                        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

                        byte[] bytes = new byte[12];
                        byteBuffer.get(bytes);
                        int packtype = byteBuffer.getInt();
                        int content_size = byteBuffer.getInt();
                        String data=new String(bytes,0,11, StandardCharsets.UTF_8);
                        Log.e("message", "header:" + data + ",packtype:" + packtype + ",content_size:" + content_size);
                    }
                });
            }
        });


    }
}
