package com.smartelectronics.lovelylight.utils;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.smartelectronics.lovelylight.R;

/**
 * Created by SAEED on 2/15/2019.
 */

public class CustomToast {

    private Context mContext;
    private Activity activity;
    private View toastLayout;
    private TextView text;

    public CustomToast(Context context) {

        this.mContext = context;
        this.activity = (Activity) mContext;

        LayoutInflater inflater = this.activity.getLayoutInflater();
        toastLayout = inflater.inflate(R.layout.custom_toast, (ViewGroup) this.activity.findViewById(R.id.toastLayout));
    }

    public void showMessage(String msg){

        text = toastLayout.findViewById(R.id.txtToast);
        text.setText(msg);

        Toast toast = new Toast(mContext);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);
        toast.setGravity(Gravity.CENTER,0,50);
        toast.show();
    }


}
