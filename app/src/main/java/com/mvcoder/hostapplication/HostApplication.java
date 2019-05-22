package com.mvcoder.hostapplication;

import android.os.Environment;
import android.os.Process;

import com.mvcoder.common.log.Log;
import com.mvcoder.common.utils.ProcessUtil;
import com.qihoo360.replugin.RePluginApplication;
import com.tencent.mars.xlog.Xlog;

public class HostApplication extends RePluginApplication {

    private static final String TAG = HostApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        initXLog();
    }

    private void initXLog() {
        String packageName = getPackageName();
        String label = getPackageManager().getApplicationLabel(getApplicationInfo()).toString();

        final String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
        final String logPath = SDCARD + "/" + packageName + "/log/";

        // this is necessary, or may cash for SIGBUS
        final String cachePath = this.getFilesDir() + "/xlog";
        //获取进程名, 因为 xlog 只支持1个进程1个日志文件，这里用进程区分，是为了支持多进程
        String processName = ProcessUtil.getProcessName(Process.myPid());
        if(processName != null){
            int index =  processName.lastIndexOf(".");
            if(index != -1 && processName.length() - 1 > index){
                processName =  processName.substring(index + 1);
            }
            //私有进程模式，可以直接拿后缀名
            if(processName.contains(":")){
                int index2 = processName.indexOf(":");
                processName = processName.substring(index2 + 1);
            }
        }
        //init xlog
        if (BuildConfig.DEBUG) {
            //第二个参数是日志库最低输入level，低于该level的日志全部不输出
            Xlog.open(true, Xlog.LEVEL_DEBUG, Xlog.AppednerModeAsync, cachePath, logPath + processName, label,  null);
            Xlog.setConsoleLogOpen(true);
        } else {
            Xlog.open(true, Xlog.LEVEL_INFO, Xlog.AppednerModeAsync, cachePath, logPath + processName, label, null);
            Xlog.setConsoleLogOpen(false);
        }
        Log.setLogImp(new Xlog());
        Log.i(TAG, "process name : %s", processName);
    }
}
