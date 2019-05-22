package com.mvcoder.hostapplication;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;

import com.mvcoder.common.log.Log;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.model.PluginInfo;

import java.io.File;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class HostActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    private static final String TAG = HostActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //当该Activity处于OnResume时，
        checkState();
    }


    @AfterPermissionGranted(1)
    private void checkState(){
        String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if(!EasyPermissions.hasPermissions(this, permissions)){
            EasyPermissions.requestPermissions(this, "", 1, permissions);
        }else{
            StateManager.PluginState state = StateManager.getInstance().getState();
            Log.i(TAG, "HostActivity on resume, then check state : %s", state);
            if(state == StateManager.PluginState.KILL){
                finish();
            }else if(state == StateManager.PluginState.UPDATE){
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        boolean running =  RePlugin.isPluginRunning("plugin1");
                        Log.i(TAG, "pllugin running %b" , running);
                        try {
                            Thread.sleep(5000);
                            PluginInfo pluginInfo = RePlugin.getPluginInfo("plugin1");
                            int ver = 1;
                            if(pluginInfo != null){
                                ver = pluginInfo.getVersion();
                            }
                            pluginInfo = null;
                            boolean uninstallSuccess = RePlugin.uninstall("plugin1");
                            Log.i(TAG, "卸载 plugin %b", uninstallSuccess);
                            String apkFilePath = getPluginDir() + "plugin_v" + (ver + 1) + ".apk";
                            File file = new File(apkFilePath);
                            if(!file.exists()){
                                Log.i(TAG, "apk : %s not exist", apkFilePath);
                                return;
                            }
                            PluginInfo info = RePlugin.install(apkFilePath);
                            if(info != null){
                                Log.i(TAG, "安装成功，版本：%d", info.getVersion());
                            }else{
                                Log.i(TAG, "插件安装失败");
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        joinToLoadPluginActivity();
                    }
                },2000);
            } else {
                joinToLoadPluginActivity();
            }
            StateManager.getInstance().reset();
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private void joinToLoadPluginActivity(){
        PluginInfo pluginInfo = RePlugin.getPluginInfo("plugin1");
        if(pluginInfo == null){
            String pluginDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/plugintest/";
            String filePath = pluginDir + "plugin_v1.apk";
            File apkFile = new File(filePath);
            if(!apkFile.exists()){
                Log.i(TAG, "plugin_v1.apk doesn't exist");
                return;
            }
            PluginInfo pluginInfo1 = RePlugin.install(filePath);
            if(pluginInfo1 == null) return;
            Log.i(TAG, "plugin install success");
        }else{
            Log.i(TAG, "plugin info : " + pluginInfo.toString());
        }
        Intent intent = new Intent(this, LoadPluginActivity.class);
        startActivity(intent);
    }

    private String getPluginDir(){
        String pluginDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/plugintest/";
        return pluginDir;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "host app on destroy, then process( %d ) was killed", Process.myPid());
        //关闭日志库
        Log.appenderFlush(true);
        Log.appenderClose();
        Process.killProcess(Process.myPid());
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

}
