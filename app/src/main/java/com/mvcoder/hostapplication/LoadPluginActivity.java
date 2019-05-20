package com.mvcoder.hostapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.qihoo360.replugin.RePlugin;

public class LoadPluginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_plugin);
        joinToPlugin();
    }

    private void joinToPlugin() {
        RePlugin.startActivityForResult(this, RePlugin.createIntent("plugin1","com.mvcoder.pluginapplication.MainActivity"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK){
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Process.killProcess(Process.myPid());
    }
}
