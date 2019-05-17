package com.mvcoder.hostapplication;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.model.PluginInfo;
import com.qihoo360.replugin.packages.PluginRunningList;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private final String TAG = MainActivity.class.getSimpleName();

    private Button btStart;
    private Button btUpdate;
    private Button btCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        requestPermissions();
    }

    private void initView() {
        btStart = findViewById(R.id.btStartPluginActvity);
        btCheck = findViewById(R.id.bt_check_plugin);
        btUpdate = findViewById(R.id.bt_update_plugin);
        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActvity();
            }
        });

        btCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PluginRunningList pluginRunningList = RePlugin.getRunningPlugins();
                if(pluginRunningList != null){
                    Iterator<String> iterator = pluginRunningList.iterator();
                    while (iterator.hasNext()){
                        String pluginName = iterator.next();
                        Log.i(TAG, "plugin : " + pluginName + " running!!");
                    }
                }
            }
        });

        btUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePlugin();
            }
        });
    }

    private void updatePlugin(){
        PluginInfo pluginInfo = RePlugin.getPluginInfo("plugin1");
        String pluginDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/plugintest/";
        File dirFile = new File(pluginDir);
        if(!dirFile.exists()){
            dirFile.mkdirs();
        }
        if(pluginInfo == null){
            Log.i(TAG, "plugin not install");
            String filePath = dirFile.getAbsolutePath() + "/plugin_v1.apk";
            PluginInfo pluginInfo1 = RePlugin.install(filePath);
            if(pluginInfo1 != null){
                Log.i(TAG, "install plugin sucess , plugin name: " + pluginInfo1.getName());
                RePlugin.preload(pluginInfo1);
                Log.i(TAG, "preload plugin : " + pluginInfo1.getName());
            }else{
                Log.i(TAG, "install plugin fail");
            }
        }else{
            Log.i(TAG, "plugin update");
            String filePath = dirFile.getAbsolutePath() + "/plugin_v2.apk";
            PluginInfo pluginInfo1 = RePlugin.install(filePath);
            if(pluginInfo1 != null){
                Log.i(TAG, "update plugin sucess , plugin name: " + pluginInfo1.getName());
                RePlugin.preload(pluginInfo1);
                Log.i(TAG, "preload plugin : " + pluginInfo1.getName());

            }else{
                Log.i(TAG, "plugin plugin fail");
            }
        }
    }

    private void startActvity(){
        RePlugin.startActivity(this, RePlugin.createIntent("plugin1","com.mvcoder.pluginapplication.MainActivity"));
    }

    @AfterPermissionGranted(1)
    private void requestPermissions(){
        String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if(!EasyPermissions.hasPermissions(this, permissions)){
            EasyPermissions.requestPermissions(this, "", 1, permissions);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }
}
