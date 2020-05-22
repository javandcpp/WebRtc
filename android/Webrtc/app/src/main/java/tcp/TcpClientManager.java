package tcp;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;

public class TcpClientManager{

    private final ExecutorService executorService;


    private TcpClientManager(){
        executorService = Executors.newFixedThreadPool(2);
    }

    private static TcpClientManager singleInstance=null;

    public static TcpClientManager getSingleInstance(){
        if (null==singleInstance){

            synchronized (TcpClientManager.class){
                if (null==singleInstance){
                    singleInstance=new TcpClientManager();
                }
            }
        }
        return singleInstance;
    }

    public void initSocket(String ip,int port){
        ReadTaskRunnable readTaskRunnable=new ReadTaskRunnable();
        WriteTaskRunnable writeTaskRunnable=new WriteTaskRunnable();
        executorService.execute(new SocketTaskRunnable(ip,port,readTaskRunnable,writeTaskRunnable));



    }

}
