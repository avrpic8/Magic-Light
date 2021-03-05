package com.smartelectronics.lovelylight.utils.colorPicker;

import android.graphics.Color;

/**
 * Created by SAEED on 1/28/2019.
 */

public class ColorConverter {

    private int red;
    private int green;
    private int blue;

    private String dataPacket;

    public int getRedColor(int color){
        red = Color.red(color);
        return red;
    }
    public int getGreenColor(int color){
        green = Color.green(color);
        return green;
    }
    public int getBlueColor(int color){
        blue = Color.blue(color);
        return blue;
    }

    public String getDataPacket(String startByte, int color,int pumpPower, String lampsStatus,
                                String pumpStatus, String endByte){

        dataPacket = startByte +
                        String.format("%03d", getRedColor(color)) +
                        String.format("%03d", getGreenColor(color)) +
                        String.format("%03d", getBlueColor(color)) +
                        String.format("%03d",pumpPower) + lampsStatus + pumpStatus +
                    endByte;
        return dataPacket;
    }
}
