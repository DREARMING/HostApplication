package com.mvcoder.hostapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.mvcoder.common.log.Log;
import com.qihoo360.replugin.RePlugin;

public class LoadPluginActivity extends AppCompatActivity {

    private final static int REQUEST_CODE_PLUGIN_ACTIVITY = 8;
    private final static String TAG = LoadPluginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_plugin);
        joinToPlugin();
    }

    private void joinToPlugin() {
        boolean sucesss = RePlugin.startActivityForResult(this, RePlugin.createIntent("plugin1","com.mvcoder.pluginapplication.MainActivity"), REQUEST_CODE_PLUGIN_ACTIVITY);
        Log.i(TAG, "启动 Plugin Activity, 结果：%b", sucesss);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_PLUGIN_ACTIVITY && resultCode == RESULT_OK){
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "on destroy ,then kill process : %d", Process.myPid());
        //关闭日志系统
        Log.appenderFlush(true);
        Log.appenderClose();
        Process.killProcess(Process.myPid());
    }
}
