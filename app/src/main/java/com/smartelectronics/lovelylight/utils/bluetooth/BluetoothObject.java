package com.smartelectronics.lovelylight.utils.bluetooth;

import android.content.Context;


public class BluetoothObject {

    private static BluetoothSPP bluetooth;

    public static void initBluetooth(Context context){
        bluetooth = new BluetoothSPP(context);
    }

    public static BluetoothSPP get(){
        return bluetooth;
    }
}
