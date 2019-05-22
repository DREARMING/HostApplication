package com.mvcoder.hostapplication;

public class StateManager {

    public enum PluginState {
        NORMAL,
        UPDATE,
        KILL
    }

    private StateManager(){

    }

    private PluginState currentState = PluginState.NORMAL;

    private static volatile StateManager stateManager;

    public static StateManager getInstance(){
        if(stateManager == null){
            synchronized (StateManager.class){
                if(stateManager == null){
                    stateManager = new StateManager();
                }
            }
        }
        return stateManager;
    }

    public synchronized void reset(){
        currentState = PluginState.NORMAL;
    }

    public synchronized void setState(PluginState state){
        currentState = state;
    }

    public synchronized PluginState getState(){
        return currentState;
    }

}
