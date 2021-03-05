package com.smartelectronics.lovelylight;

import android.app.Application;

import com.smartelectronics.lovelylight.utils.bluetooth.BluetoothObject;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        BluetoothObject.initBluetooth(this);
    }
}
