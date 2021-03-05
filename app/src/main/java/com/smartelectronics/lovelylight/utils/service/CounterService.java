package com.smartelectronics.lovelylight.utils.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.util.Log;


import java.util.Timer;
import java.util.TimerTask;

public class CounterService extends Service {

    private int angel = 1, speed = 0;
    private Timer timer;

    private final String TAG = "service";

    @Override
    public void onCreate() {
        super.onCreate();
        timer = new Timer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        speed = intent.getIntExtra("speed", 50);
        angel = intent.getIntExtra("angel", 1);

        Log.i(TAG, "onStartCommand: " + speed);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                angel++;
                if (angel == 360)
                    angel = 0;

                sendData(angel);
                Log.i(TAG, "run: " + angel);
            }
        };
        timer.scheduleAtFixedRate(task,0, speed);

        return Service.START_NOT_STICKY;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void sendData(int value) {
        Intent counter = new Intent();
        counter.setAction("COUNTER_RECEIVER");
        counter.putExtra( "counter",value);
        sendBroadcast(counter);
    }
}
