package com.smartelectronics.lovelylight.interfaces;

/**
 * Created by SAEED on 1/15/2019.
 */

public interface BluetoothStateChange {

    public void onBluetoothStateUpdate(int state, String deviceName);
}
