package com.smartelectronics.lovelylight.utils;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.smartelectronics.lovelylight.R;

/**
 * Created by SAEED on 3/2/2019.
 */

public class MyAlertDialog extends Dialog {


    public  Button btnOk;
    private Button btnCancel;
    private TextView txtMessage, txtHeader;

    private RelativeLayout headerColor;
    public LottieAnimationView animationView;

    private Context mContext;

    public MyAlertDialog(@NonNull Context context) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        mContext = context;
        setContentView(R.layout.alert_dialog);

        init();
    }

    private void initButtons(){

        btnOk = findViewById(R.id.button_ok);

        btnCancel= findViewById(R.id.button_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

    }
    private void initViews(){

        initButtons();

        txtHeader   = findViewById(R.id.txt_header);
        txtMessage  = findViewById(R.id.txt_message);
        headerColor = findViewById(R.id.header_layout);
    }
    private void init(){
        initViews();

        animationView = findViewById(R.id.animation_view);
    }

    public void setHeaderColor(int color){

        headerColor.setBackgroundColor(mContext.getResources().getColor(color));
        btnOk.setTextColor(mContext.getResources().getColor(color));
        btnCancel.setTextColor(mContext.getResources().getColor(color));
    }
    public void setTitle(String msg){
        txtHeader.setText(msg);
    }
    public void setMessage(String msg){
        txtMessage.setText(msg);
    }
}
