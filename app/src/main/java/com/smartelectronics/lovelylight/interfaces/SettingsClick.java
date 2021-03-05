package com.smartelectronics.lovelylight.interfaces;

/**
 * Created by SAEED on 2/3/2019.
 */

public interface SettingsClick {

    public void onTimerModeClick(boolean timerFlag);
    public void onAccelerometerModeClick(boolean sensorFlag);
    public void onLightModeClick(boolean lightFlag);
    public void onMicrophoneModeClick(boolean micFlag);
    public void onBtNameSend(String newBluetoothName);
}
