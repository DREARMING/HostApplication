package com.mvcoder.hostapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.mvcoder.common.log.Log;

public class HostService extends Service {

    private static final String TAG = HostService.class.getSimpleName();

    public HostService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return hostService.asBinder();
    }

    private IHostService.Stub hostService = new IHostService.Stub() {
        @Override
        public boolean updatePlugin(String path, String pluginName) throws RemoteException {
            StateManager.getInstance().setState(StateManager.PluginState.UPDATE);
            Log.i(TAG, "plugin(%s) want to update, filepath : %s", pluginName, path);
            return true;
        }

        @Override
        public void killProcess() throws RemoteException {
            Log.i(TAG, "plugin want to kill process");
            StateManager.getInstance().setState(StateManager.PluginState.KILL);
        }

    };
}
