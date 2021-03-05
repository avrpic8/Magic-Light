package com.smartelectronics.lovelylight.utils.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by SAEED on 1/13/2019.
 */

public class BluetoothCommunication {

    public static final int CONNECTED = 2;
    public static final int CONNECTING = 1;
    public static final int NOT_CONNECTED = 0;
    public static final String STATE_KEY = "bt_state";

    private BluetoothControl btControl;
    private ConnectThread connectThread;
    private final Handler handler;
    private OutputStream  oStream;
    private int state;

    public BluetoothCommunication(Handler handler, BluetoothControl btControl) {

        this.handler   = handler;
        this.btControl = btControl;
        setState(NOT_CONNECTED);
    }


    private class ConnectThread extends Thread{

        private final BluetoothDevice mDevice;
        private final BluetoothSocket mSocket;

        private ConnectThread(BluetoothDevice device) {

            BluetoothSocket tmp = null;
            mDevice = device;

            try {

                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {}

            this.mSocket = tmp;
        }

        public void run(){

            btControl.cancelDiscovery();
            setState(BluetoothCommunication.CONNECTING);

            try {

                mSocket.connect();
                synchronized (BluetoothCommunication.this){
                    connectThread = null;
                }

                setState(BluetoothCommunication.CONNECTED);
                connected(this.mSocket, this.mDevice);
            } catch (IOException e) {
                setState(BluetoothCommunication.NOT_CONNECTED);

                try {
                    this.mSocket.close();
                } catch (IOException e1) {}
            }

        }
        public void cancel(){

            try {
                mSocket.close();
            } catch (IOException e) {}
        }
    }


    private synchronized void setState(int state){

        this.state = state;
        Message    msg = this.handler.obtainMessage();
        Bundle  bundle = new Bundle();
        bundle.putInt(STATE_KEY, state);
        msg.setData(bundle);
        msg.sendToTarget();
    }
    public int getState(){
        return this.state;
    }

    public void connect(String device_mac){

        disconnect();
        BluetoothDevice target = btControl.getDeviceByMac(device_mac);
        if(target!= null){
            connectThread = new ConnectThread(target);
            connectThread.start();
        }
    }
    public void disconnect(){

        if (connectThread!= null) {
            connectThread.cancel();
            connectThread = null;
        }

        if(oStream!= null){

            try {
                oStream.close();
            } catch (IOException e) {}

            oStream = null;
        }
        setState(NOT_CONNECTED);
    }

    private synchronized void connected(BluetoothSocket mSocket, BluetoothDevice mDevice){

        if(connectThread!= null){
            connectThread.cancel();
            connectThread = null;
        }

        if(oStream!= null){

            try {
                oStream.close();
            } catch (IOException e) {}

            oStream = null;
        }

        try {
            oStream = mSocket.getOutputStream();
        } catch (IOException e) {setState(NOT_CONNECTED);}
    }
    public boolean sendData(byte[] buffer){

        if(this.state!= CONNECTED)
            return false;

        try {
            this.oStream.write(buffer);
            return true;
        } catch (IOException e) {
            setState(NOT_CONNECTED);
            disconnect();
            return false;
        }

    }
    public boolean isConnected(){

        if(this.state!= CONNECTED) {
            return true;
        }
        return false;
    }


}
