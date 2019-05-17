package com.mvcoder.hostapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class UpdateService extends Service {

    private final static String TAG = UpdateService.class.getSimpleName();

    private volatile boolean isClose = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG,"service on bind");
        return messenger.getBinder();
    }

    private final static int WHAT_LISTEN_UPDATE = 1;
    private Map<String, Messenger> map = new HashMap<>();

    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case WHAT_LISTEN_UPDATE:
                    String pluginName = (String) msg.obj;
                    Messenger messenger = msg.replyTo;
                    map.put(pluginName, messenger);
                    countTimeTask();
                    break;
            }
        }
    };

    private void countTimeTask() {
        final long time = System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isClose){
                    long now = System.currentTimeMillis();
                    if(now - time >= 10000){
                        notifyUpdate();
                       return;
                    }
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void notifyUpdate(){
        Log.i(TAG, "通知更新");
        if(map != null && map.size() > 0) {
            for (Map.Entry<String, Messenger> entry : map.entrySet()) {
                String plugin = entry.getKey();
                Messenger messenger = entry.getValue();
                Log.i(TAG, "plugin : " + plugin + " is notified to update");
                Message message = new Message();
                message.what = 10;
                try {
                    messenger.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Messenger messenger = new Messenger(handler);

    @Override
    public void onDestroy() {
        super.onDestroy();
        isClose = true;
    }
}
