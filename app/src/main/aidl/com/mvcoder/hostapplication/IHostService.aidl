// IHostService.aidl
package com.mvcoder.hostapplication;

// Declare any non-default types here with import statements

interface IHostService {


    boolean updatePlugin(in String path, in String pluginName);

    void killProcess();

}
