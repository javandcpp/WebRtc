package tcp;

import android.os.Build;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import androidx.annotation.RequiresApi;

public class SocketTaskRunnable implements Runnable{

    private WriteTaskRunnable wirte;
    private ReadTaskRunnable read;
    private Handler mHandler;
    private String mHost;
    private int mPort;
    private Selector mSelector;
    private String TAG= SocketTaskRunnable.class.getSimpleName();
    private SocketChannel mClientChannel;

    public SocketTaskRunnable(String host, int port, ReadTaskRunnable read,WriteTaskRunnable write){
        try {
            mSelector = Selector.open();
            this.mHost=host;
            this.mPort=port;
            this.read=read;
            this.wirte=write;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void startSelector(Selector selector) {
        while (true) {
            int n = 0;    // Selector轮询注册来的Channel, 阻塞到至少有一个通道在你注册的事件上就绪了。
            try {
                n = selector.select();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (n == 0) {
                continue;
            }
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();    // 获取SelectionKey

                // 每一步都要判断selectionKey.isValid()，避免断开连接产生的java.nio.channels.CancelledKeyException
                if (selectionKey.isValid() && selectionKey.isConnectable()) {
                    connectServer(selectionKey);
                }
                if (selectionKey.isValid() && selectionKey.isReadable()) {
                    // a channel is ready for reading
//                    mHandler.sendEmptyMessage(100);

                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024); // 从SelectionKey中取出注册时附加上的DataBuffer对象
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    long count = 0;
                    try {
                        count = mClientChannel.read(byteBuffer);
                        if (count > 0) {

                            byte[] bytes = new byte[12];
                            byteBuffer.get(bytes);
                            int packtype = byteBuffer.getInt();
                            int content_size = byteBuffer.getInt();
                            String data=new String(bytes,0,11,StandardCharsets.UTF_8);
                            Log.d("message", "header:" + data + ",packtype:" + packtype + ",content_size:" + content_size);

                            byteBuffer.flip();
                        }



                    } catch (IOException e) {
                        e.printStackTrace();
                        selectionKey.cancel();
                        try {
                            mClientChannel.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }

//                        byte[] bytes = new byte[dataBuffer.remaining()];
//                        dataBuffer.get(bytes);
//                        String data = null;
//
//                            data = new String(bytes, StandardCharsets.UTF_8);
//                            Log.d(TAG,data);






                }
                if (selectionKey.isValid() && selectionKey.isWritable()) {
                    // a channel is ready for writing
                }
                iterator.remove();
            }
        }
    }

    private void connectServer(SelectionKey selectionKey) {
        try {
            mClientChannel.finishConnect();
            selectionKey.interestOps(SelectionKey.OP_READ);
            selectionKey.interestOps(SelectionKey.OP_ACCEPT);
            selectionKey.interestOps(SelectionKey.OP_WRITE);
            Log.i(TAG, "connected to:" + mClientChannel.socket().getInetAddress());
        } catch (IOException e) {
            e.printStackTrace();
            selectionKey.cancel();
            try {
                mClientChannel.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void run() {
        try {
            mClientChannel = SocketChannel.open();
            mClientChannel.configureBlocking(false);
            mClientChannel.connect(new InetSocketAddress(mHost, mPort));
//            mClientChannel.register(mSelector, SelectionKey.OP_CONNECT);
            startSelector(mSelector);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
