package tcp;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

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
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

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


    @Override
    public void run() {
        try {
            mClientChannel = SocketChannel.open();
            mClientChannel.configureBlocking(false);
            mClientChannel.connect(new InetSocketAddress(mHost, mPort));
            mClientChannel.register(mSelector, SelectionKey.OP_CONNECT);
            startSelector(mSelector);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
