package com.mvcoder.hostapplication;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.qihoo360.loader2.PMF;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.model.PluginInfo;
import com.qihoo360.replugin.packages.PluginManagerProxy;
import com.qihoo360.replugin.packages.PluginManagerServer;
import com.qihoo360.replugin.packages.PluginRunningList;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private final String TAG = MainActivity.class.getSimpleName();

    private Button btStart;
    private Button btUpdate;
    private Button btCheck;
    private Button btCheckReflect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        requestPermissions();
    }

    private PluginInfo hook(String pluginName, String newPath) {
        //当运行插件的进程死亡之后，确保 PluginManagerServer中保存的各进程的 RunningList的map，移除掉插件进程，否则将在 uninstall 中无法通过 插件运行校验，
        //从而导致无法热更

        //hook点，移除 RunningList 中对应的 Plugin
        Field sRunninglistField = null;
        try {
            sRunninglistField = PluginManagerProxy.class.getDeclaredField("sRunningList");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        if(sRunninglistField == null){
            Log.i(TAG, "running list field null");
            return null;
        }
        sRunninglistField.setAccessible(true);
        PluginRunningList pluginRunningList = null;
        try {
            pluginRunningList = (PluginRunningList) sRunninglistField.get(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if(pluginRunningList == null){
            Log.i(TAG,"plugin running list null");
            return null;
        }
        Iterator<String> iterator = pluginRunningList.iterator();
        while (iterator.hasNext()){
            String item = iterator.next();
            if(pluginName.equals(item)){
                Log.i(TAG,"find running plugin");
                iterator.remove();
                break;
            }
        }

        //hook点2
        checkReflect("plugin1");

        //卸载该插件
        boolean uninstall = RePlugin.uninstall(pluginName);
        Log.i(TAG, "uninstall : " + uninstall);

        //当卸载完之后，重新安装
        PluginInfo pi = RePlugin.install(newPath);
        if(pi != null) {
            Log.i(TAG, "pending update == null? " + (pi.getPendingUpdate() == null));
            RePlugin.preload(pi);
        }else {
            Log.i(TAG, "install fail");
        }
        return pi;
    }

    private void checkReflect(String pluginName){
        Field pmbase = null;
        try {
            pmbase = PMF.class.getDeclaredField("sPluginMgr");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.i(TAG,"filed - pmbase is null");
        }
        if(pmbase == null) return;

        Object pmbaseObj = null;
        try {
            pmbase.setAccessible(true);
            pmbaseObj = pmbase.get(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if(pmbaseObj == null) return;

        String pmbaseClassName = "com.qihoo360.loader2.PmBase";
        try {
            Class clazz =  getClassLoader().loadClass(pmbaseClassName);
            if(clazz != null){
                Log.i(TAG, "class not null");
            }
            Field mHostSvcField = clazz.getDeclaredField("mHostSvc");
            mHostSvcField.setAccessible(true);
            Object pmHostSvc = mHostSvcField.get(pmbaseObj);
            if(pmHostSvc != null){
                Log.i(TAG, "pmhostsvc != null");
                String pmHostSvcClassname = "com.qihoo360.loader2.PmHostSvc";
                Class pmHostSvsClazz = getClassLoader().loadClass(pmHostSvcClassname);
                Field pluginManagerField = pmHostSvsClazz.getDeclaredField("mManager");
                pluginManagerField.setAccessible(true);
                Object pluginManager =  pluginManagerField.get(pmHostSvc);
                Field pluginMapField = PluginManagerServer.class.getDeclaredField("mProcess2PluginsMap");
                pluginMapField.setAccessible(true);
                Map<String, PluginRunningList> map = (Map<String, PluginRunningList>) pluginMapField.get(pluginManager);
                Log.i(TAG, "map == null? " + (map == null));
                PluginRunningList pluginRunningList = map.get("com.mvcoder.hostapplication");
                if(pluginRunningList != null){
                    Iterator<String> iterator = pluginRunningList.iterator();
                    while (iterator.hasNext()){
                        String item = iterator.next();
                        if(pluginName.equals(item)){
                            Log.i(TAG,"find running plugin");
                            iterator.remove();
                            break;
                        }
                    }
                }
            }else{
                Log.i(TAG, "pmhostsvc == null");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "class not found!!");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        btStart = findViewById(R.id.btStartPluginActvity);
        btCheck = findViewById(R.id.bt_check_plugin);
        btUpdate = findViewById(R.id.bt_update_plugin);
        btCheckReflect = findViewById(R.id.btCheckReflect);
        btCheckReflect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkReflect("plugin1");
            }
        });
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
                Log.i(TAG, "install plugin sucess , plugin name: " + pluginInfo1.getName() + ", version : " + pluginInfo1.getVersion());
                //RePlugin.preload(pluginInfo1);
                Log.i(TAG, "preload plugin : " + pluginInfo1.getName());
            }else{
                Log.i(TAG, "install plugin fail");
            }
        }else{
            Log.i(TAG, "plugin update");
            String filePath = dirFile.getAbsolutePath() + "/plugin_v2.apk";
            boolean sucess = RePlugin.uninstall("plugin1");
            Log.i(TAG, "uninstall plugin : " + sucess);
            PluginInfo pluginInfo1 = RePlugin.install(filePath);/* hook("plugin1",filePath)*/;
            if(pluginInfo1 != null){
                Log.i(TAG, "update plugin sucess , plugin name: " + pluginInfo1.getName() + ", version : " + pluginInfo1.getVersion());
                //RePlugin.preload(pluginInfo1);
                Log.i(TAG, "preload plugin : " + pluginInfo1.getName());

            }else{
                Log.i(TAG, "plugin plugin fail");
            }
        }
    }

    private void startActvity(){
        //RePlugin.startActivity(this, RePlugin.createIntent("plugin1","com.mvcoder.pluginapplication.MainActivity"));
        Intent intent = new Intent(this, LoadPluginActivity.class);
        startActivity(intent);
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
